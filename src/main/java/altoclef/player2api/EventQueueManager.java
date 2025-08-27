package altoclef.player2api;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import altoclef.AltoClefController;
import altoclef.player2api.status.StatusUtils;
import net.minecraft.server.MinecraftServer;

public class EventQueueManager {
    public static final Logger LOGGER = LogManager.getLogger();

    public static ConcurrentHashMap<UUID, EventQueueData> queueData = new ConcurrentHashMap<>();
    private static float messagePassingMaxDistance = 25; // let messages between entities pass iff <= this maximum
    private static boolean hasInit = false;
    public static void init(){
        if(!hasInit){
            hasInit = true;
            ForgeEventHandler handler = new ForgeEventHandler();
        }
    }

    public static class LLMCompleter {
        private boolean isProcessing = false;

        private static final ExecutorService llmThread = Executors.newSingleThreadExecutor();

        public void process(
                Player2APIService player2apiService,
                ConversationHistory history,
                Consumer<JsonObject> extOnLLMResponse,
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
                    JsonObject response = player2apiService.completeConversation(history);
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
    public static EventQueueData getOrCreateEventQueueData(AltoClefController mod) {
        return queueData.computeIfAbsent(mod.getPlayer().getUUID(), k -> {
            LOGGER.info(
                    "EventQueueManager/getOrCreateEventQueueData: creating new queue data for entId={}",
                    mod.getPlayer().getStringUUID());
            return new EventQueueData(mod);
        });
    }

    private static Stream<EventQueueData> filterQueueData(Predicate<EventQueueData> pred){
        return queueData.values().stream().filter(pred);
    }

    private static Stream<EventQueueData> getCloseDataByUUID(UUID sender) {
        return filterQueueData(data -> data.getDistance(sender) < messagePassingMaxDistance);
    }

    // ## Callbacks (need to register these externally)

    // register when a user sends a chat message
    public static void onUserChatMessage(Event.UserMessage msg) {
        LOGGER.info("User message event={}", msg);
        // will add to entities close to the user:
        filterQueueData(d -> isCloseToPlayer(d, msg.userName())).forEach(data -> {
            data.onEvent(msg);
        });
    }

    // register when an AI character messages
    public static void onAICharacterMessage(Event.CharacterMessage msg, UUID senderId) {
        UUID sendingUUID = msg.sendingCharacterData().getUUID();
        getCloseDataByUUID(sendingUUID).filter(data -> !(data.getUUID().equals(senderId)))
                .forEach(data -> {
                    LOGGER.info("onCharMsg/ msg={}, sender={}, running onCharMsg for ={}", msg.message(), senderId, data.getName());
                    data.onAICharacterMessage(msg);
                });
    }

    private static void process(Consumer<Event.CharacterMessage> onCharacterEvent, Consumer<String> onErrEvent) {
        Optional<EventQueueData> dataToProcess = queueData.values().stream().filter(data -> {
            return data.getPriority() != 0;
        }).max(Comparator.comparingLong(EventQueueData::getPriority));
        llmCompleters.stream().filter(LLMCompleter::isAvailible).forEach(completer -> {
            dataToProcess.ifPresent(data -> {
                data.process(onCharacterEvent, onErrEvent, completer);
            });
        });
    }

    // side effects are here:
    public static void injectOnTick(MinecraftServer server) {
        if(!hasInit){
            init();
        }

        Consumer<Event.CharacterMessage> onCharacterEvent = (data) -> {
            AgentSideEffects.onEntityMessage(server, data);
        };
        Consumer<String> onErrEvent = (errMsg) -> {
            AgentSideEffects.onError(server, errMsg);
        };
        if (!TTSManager.isLocked()) {
            process(onCharacterEvent, onErrEvent);
        }
        TTSManager.injectOnTick(server);
    }

    public static void sendGreeting(AltoClefController mod, Character character) {
        LOGGER.info("Sending greeting character={}", character);
        EventQueueData data = getOrCreateEventQueueData(mod);
        data.onGreeting();
    }

    public static void resetMemory(AltoClefController mod) {
        mod.getAIPersistantData().clearHistory();
    }


    private static boolean isCloseToPlayer(EventQueueData data, String userName){
        return StatusUtils.getDistanceToUsername(data.getMod(), userName) < messagePassingMaxDistance;
    }
}