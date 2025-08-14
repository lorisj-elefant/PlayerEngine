package adris.altoclef.player2api;

import java.util.Deque;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import adris.altoclef.AltoClefController;
import adris.altoclef.player2api.status.AgentStatus;
import adris.altoclef.player2api.status.StatusUtils;
import adris.altoclef.player2api.status.WorldStatus;
import adris.altoclef.player2api.utils.Utils;

public class EventQueueData {
    public static final Logger LOGGER = LogManager.getLogger("Automatone");

    private final Character character;
    private final AltoClefController mod;

    private ConversationHistory conversationHistory;

    private final Deque<Event> eventQueue = new ConcurrentLinkedDeque<>();
    private long lastProcessTime = 0L;
    private boolean isProcessing = false;

    private MessageBuffer altoClefMsgBuffer = new MessageBuffer(10);

    public EventQueueData(AltoClefController mod, Character character) {
        this.character = character;
        this.mod = mod;

        String systemPrompt = Prompts.getAINPCSystemPrompt(this.character, mod.getCommandExecutor().allCommands());
        this.conversationHistory = new ConversationHistory(systemPrompt, character);
    }

    // ## Processing

    // returns 0 if should not process, otherwise gives a number that increases
    // based on higher priority
    // (for now it is #ns from last processing time)
    public long getPriority() {
        if (isProcessing || eventQueue.isEmpty()) {
            return 0;
        }
        return System.nanoTime() - lastProcessTime;
    }

    public void process(Consumer<String> onEntityMessage, Consumer<String> onCommandString,
            Consumer<String> extOnErrMsg, EventQueueManager.LLMCompleter completer) {
        if (isProcessing) {
            LOGGER.warn("Called queueData.process even though it was already processing! this should not happen");
            return;
        }
        if (eventQueue.isEmpty()) {
            LOGGER.warn("queueData.process called on empty event queue! this should not happen");
            return;
        }

        Consumer<String> onErrMsg = errMsg -> {
            this.isProcessing = false;
            extOnErrMsg.accept(errMsg);
        };

        this.lastProcessTime = System.nanoTime();
        this.isProcessing = true;

        // prepare conversation history for LLM call
        moveFromQueueToConversationHistory();
        String agentStatus = AgentStatus.fromMod(this.mod).toString();
        String worldStatus = WorldStatus.fromMod(this.mod).toString();
        String altoClefDebugMsgs = this.altoClefMsgBuffer.dumpAndGetString();

        ConversationHistory historyWithWrappedStatus = this.conversationHistory
                .copyThenWrapLatestWithStatus(worldStatus, agentStatus, altoClefDebugMsgs);

        LOGGER.info("[AICommandBridge/processChatWithAPI]: Calling LLM: history={}",
                new Object[] { historyWithWrappedStatus.toString() });
        Consumer<JsonObject> onLLMResponse = jsonResp -> {
            String llmMessage = Utils.getStringJsonSafely(jsonResp, "message");
            String command = Utils.getStringJsonSafely(jsonResp, "command");
            LOGGER.info("[AICommandBridge/processCharWithAPI]: Processed LLM repsonse: message={} command={}",
                    llmMessage, command);
            try {
                if (llmMessage != null) {
                    onEntityMessage.accept(llmMessage);
                }
                if (command != null) {
                    onCommandString.accept(command);
                }
            } catch (Exception e) {
                LOGGER.error("[AICommandBridge/processChatWithAPI/onLLMRepsonse: ERROR RUNNING SIDE EFFECTS, errMsg={}",
                        e.getMessage());
            } finally {
                this.isProcessing = false;
            }
        };
        completer.process(historyWithWrappedStatus, onLLMResponse, onErrMsg);
    }

    private void moveFromQueueToConversationHistory() {
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            conversationHistory.addUserMessage(event.toString());
        }
    }

    // ## Callbacks:
    public void addAltoclefLogMessage(String message) {
        System.out.printf("ADDING Altoclef System Message: %s", new Object[] { message });
        this.altoClefMsgBuffer.addMsg(message);
    }

    public void onUserMessage(Event.UserMessage msg) {
        // is duplicate <=> same as most recent msg
        boolean isDuplicate = eventQueue.peekLast() != null && eventQueue.peekLast().equals(msg);
        if (isDuplicate) {
            return; // skip
        }
        eventQueue.add(msg);
    }

    public void onAICharacterMessage(Event.CharacterMessage msg) {
        boolean comingFromThisCharacter = msg.sendingCharacterData().getUsername().equals(getUsername());
        // is our character <=> dont add because we will already have added assistant
        // msg
        if (comingFromThisCharacter) {
            return;
        }
        eventQueue.add(msg);
    }

    // Utils:
    public String getUsername() {
        return mod.getPlayer().getName().getString();
    }

    public float getDistanceToUserName(String userName) {
        return StatusUtils.getUserNameDistance(mod, userName);
    }

}
