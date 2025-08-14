package adris.altoclef.player2api;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import adris.altoclef.AltoClefController;
import adris.altoclef.player2api.utils.Utils;
import net.minecraft.text.Text;

public class EventQueueManager {
    public static final Logger LOGGER = LogManager.getLogger("Automatone");

    public static ConcurrentHashMap<UUID, EventQueueData> queueData = new ConcurrentHashMap<>();

    private static float messagePassingMaxDistance = 200; // let messages between entities pass iff <= this maximum

    public static class LLMCompleter {
        private boolean isProcessing = false;

        private static final ExecutorService llmThread = Executors.newSingleThreadExecutor();

        public void process(ConversationHistory history, Consumer<JsonObject> extOnLLMResponse,
                Consumer<String> extOnErrMsg) {
            if (isProcessing) {
                LOGGER.warn("Called llmcompleter.process when it was already processing! This should not happen.");
                return;
            }
            Consumer<JsonObject> onLLMResponse = resp -> {
                try {
                    extOnLLMResponse.accept(resp);
                } catch (Exception e) {
                    LOGGER.error(
                            "[EventQueueManager/LLMCompleter/process/onLLMResponse]: Error in external llm resp, errMsg={} llmResp={}",
                            e.getMessage(), resp.toString());
                } finally {
                    isProcessing = false;
                }
            };
            Consumer<String> onErrMsg = errMsg -> {
                try {
                    extOnErrMsg.accept(errMsg);
                } catch (Exception e) {
                    LOGGER.error(
                            "[EventQueueManager/LLMCompleter/process/onErrMsg]: Error in external onErrmsg, errMsgFromException={} errMsg={}",
                            e.getMessage(), errMsg);
                } finally {
                    isProcessing = false;
                }
            };
            isProcessing = true;
            llmThread.submit(() -> {
                try {
                    JsonObject response = Player2APIService.completeConversation(history);
                    LOGGER.info("LLMCompleter returned json={}", response);
                    onLLMResponse.accept(response);
                } catch (Exception e) {
                    onErrMsg.accept(
                            e.getMessage() == null ? "Unknown error from CompleteConversation API" : e.getMessage());
                }
            });
        }

        public boolean isAvailible() {
            return !isProcessing;
        }
    }

    // ## Utils
    private static EventQueueData getOrCreateEventQueueData(AltoClefController mod) {
        return queueData.computeIfAbsent(mod.getPlayer().getUuid(), k -> {
            LOGGER.info("EventQueueManager/getOrCreateEventQueueData: creating new queue data for entId={}",
                    mod.getPlayer().getUuidAsString());
            return new EventQueueData(mod,  );
        });
    }

    private static Stream<EventQueueData> getCloseData(String senderUserName) {
        return queueData.values().stream()
                .filter(data -> data.getDistanceToUserName(senderUserName) < messagePassingMaxDistance);
    }

    // ## Callbacks (need to register these externally)

    // register when a user sends a chat message
    public void onUserChatMessage(Event.UserMessage msg) {
        // will add to entities close to the user:
        getCloseData(msg.userName()).forEach(data -> {
            data.onUserMessage(msg);
        });
    }

    // register when an AI character messages
    public void onAICharacterMessage(Event.CharacterMessage msg, UUID senderId) {
        String sendingCharacterUsername = msg.sendingCharacterData().getUsername();
        getCloseData(sendingCharacterUsername).filter(data -> !(data.getUsername().equals(sendingCharacterUsername)));
    }

}
