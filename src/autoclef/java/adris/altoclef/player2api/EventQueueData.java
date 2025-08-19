package adris.altoclef.player2api;

import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import adris.altoclef.AltoClefController;
import adris.altoclef.player2api.AgentSideEffects.CommandExecutionStopReason;
import adris.altoclef.player2api.Event.InfoMessage;
import adris.altoclef.player2api.status.AgentStatus;
import adris.altoclef.player2api.status.StatusUtils;
import adris.altoclef.player2api.status.WorldStatus;
import adris.altoclef.player2api.utils.Utils;

public class EventQueueData {

    private static short MAX_EVENT_QUEUE_SIZE = 10;

    public static final Logger LOGGER = LogManager.getLogger();

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
        this.conversationHistory = new ConversationHistory(systemPrompt, character.name(), character.shortName());
    }

    // ## Processing

    // 0 => should not process,
    // otherwise gives a number that increases based on higher priority
    // (for now it is #ns from last processing time)
    public long getPriority() {
        if (isProcessing || eventQueue.isEmpty()) {
            return 0;
        }
        return System.nanoTime() - lastProcessTime;
    }

    // get LLM response and add to conversation history
    public void process(
            Consumer<Event.CharacterMessage> onCharacterEvent,
            Consumer<String> extOnErrMsg,
            EventQueueManager.LLMCompleter completer) {

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
                if (llmMessage != null || command != null) {
                    conversationHistory.addAssistantMessage(llmMessage);
                    onCharacterEvent.accept(new Event.CharacterMessage(llmMessage, command, this));
                } else {
                    LOGGER.warn(
                            "[AICommandBridge/processChatWithAPI/onLLMResponse]: Generated null llm message and command");
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

    private boolean isEventDuplicateOfLastMessage(Event evt) {
        boolean isDuplicate = eventQueue.peekLast() != null && eventQueue.peekLast().equals(evt);

        if (isDuplicate) {
            LOGGER.warn("[EventQueueData]: evt={} was added twice!", evt.toString());
            return true;
        }
        return false;
    }

    private void addEventToQueue(Event event){
        if (isEventDuplicateOfLastMessage(event)) {
            return; // skip
        }
        if(eventQueue.size() > MAX_EVENT_QUEUE_SIZE){
            eventQueue.removeFirst(); 
        }
        eventQueue.add(event);
    }

    // ## Callbacks:
    public void addAltoclefLogMessage(String message) {
        System.out.printf("ADDING Altoclef System Message: %s", new Object[] { message });
        this.altoClefMsgBuffer.addMsg(message);
    }

    public void onEvent(Event event){
        addEventToQueue(event);
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

    public void onGreeting() {
        // queues up greeting:
        String suffix = " IMPORTANT: SINCE THIS IS THE FIRST MESSAGE, DO NOT SEND A COMMAND!!";
        if (conversationHistory.isLoadedFromFile()) {
            addEventToQueue(new InfoMessage("You want to welcome user back." + suffix));
        } else {
            addEventToQueue(new InfoMessage(character.greetingInfo() + suffix));
        }
    }

    public void onCommandFinish(AgentSideEffects.CommandExecutionStopReason stopReason) {
        if (stopReason instanceof CommandExecutionStopReason.Finished) {
            if (eventQueue.isEmpty()) {
                addEventToQueue(new InfoMessage(String.format(
                        "Command feedback: %s finished running. What shall we do next? If no new action is needed to finish user's request, generate empty command `\"\"`.",
                        stopReason.commandName())));
            }
        } else if (stopReason instanceof CommandExecutionStopReason.Error) {
            addEventToQueue(new InfoMessage(String.format(
                    "Command feedback: %s FAILED. The error was %s.",
                    stopReason.commandName(),
                    ((CommandExecutionStopReason.Error) stopReason).errMsg())));
        }
        // do nothing otherwise (if canceled)
    }

    // Utils:
    public String getUsername() {
        return mod.getPlayer().getName().getString();
    }

    public float getDistanceToUserName(String userName) {
        return StatusUtils.getUserNameDistance(mod, userName);
    }


    public UUID getUUID() {
        return mod.getPlayer().getUUID();
    }

    public Character getCharacter() {
        return character;
    }

    public AltoClefController getMod() {
        return mod;
    }

    public void clearHistory() {
        conversationHistory.clear();
    }
}
