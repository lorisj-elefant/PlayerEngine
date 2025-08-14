package adris.altoclef.player2api;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

public class EventQueueManager {
    public static final Logger LOGGER = LogManager.getLogger();

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

    private static List<LLMCompleter> llmCompleters = List.of(new LLMCompleter());

    // ## Utils
    public static EventQueueData createEventQueueData(AltoClefController mod, Character character) {
        return queueData.computeIfAbsent(mod.getPlayer().getUuid(), k -> {
            LOGGER.info(
                    "EventQueueManager/getOrCreateEventQueueData: creating new queue data for entId={} character={}",
                    mod.getPlayer().getUuidAsString(),
                    character.toString());
            return new EventQueueData(mod, character);
        });
    }

    private static Stream<EventQueueData> getCloseData(String senderUserName) {
        return queueData.values().stream()
                .filter(data -> data.getDistanceToUserName(senderUserName) < messagePassingMaxDistance);
    }

    // ## Callbacks (need to register these externally)

    // register when a user sends a chat message
    public static void onUserChatMessage(Event.UserMessage msg) {
        // will add to entities close to the user:
        getCloseData(msg.userName()).forEach(data -> {
            data.onUserMessage(msg);
        });
    }

    // public static void onAltoclefLogMessage(EventQueueData data, String message){
    // data.addAltoclefLogMessage(null);
    // }

    // register when an AI character messages
    public static void onAICharacterMessage(Event.CharacterMessage msg, UUID senderId) {
        String sendingCharacterUsername = msg.sendingCharacterData().getUsername();
        getCloseData(sendingCharacterUsername).filter(data -> !(data.getUsername().equals(sendingCharacterUsername)))
                .forEach(data -> {
                    data.onAICharacterMessage(msg);
                });
    }

    // side effects are here:
    public static void injectOnTick() {
        Optional<EventQueueData> dataToProcess = queueData.values().stream().filter(data -> {
            return data.getPriority() != 0;
        }).max(Comparator.comparingLong(EventQueueData::getPriority));
        llmCompleters.stream().filter(LLMCompleter::isAvailible).forEach(completer -> {
            dataToProcess.ifPresent(data -> {
                data.process(AgentSideEffects::onEntityMessage, AgentSideEffects::onError, completer);
            });
        });
    }
    public static void sendGreeting(EventQueueData data){
        data.onGreeting();
    }
}
