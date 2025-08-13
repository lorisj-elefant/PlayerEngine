package adris.altoclef.player2api;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.AltoClefController;

public class EventQueueManager {
    public static final Logger LOGGER = LogManager.getLogger("Automatone");

    public static ConcurrentHashMap<UUID, EventQueueData> queueData = new ConcurrentHashMap<>();

    private static float messagePassingMaxDistance = 200; // let messages between entities pass iff <= this maximum

    private static class LLMCompleter {
        private boolean isProcessing = false;

        public boolean isAvailible() {
            return !isProcessing;
        }

        public void process(UUID idToProcess, Consumer<String> onMessageResponse, Consumer<String> onErrorMessage) {
            LOGGER.info("EventQueueManager/LLMCompleter/process id={}", idToProcess.toString());
            isProcessing = true;
            queueData.get(idToProcess).process(
                (resp) -> {
                    onMessageResponse.accept(resp);
                    isProcessing = false;
                },
                (errMsg) -> {
                    onErrorMessage.accept(errMsg);
                    isProcessing = false;
                }
            );

        }
    }
    

    // ## Utils
    private static EventQueueData getOrCreateEventQueueData(AltoClefController mod) {
        return queueData.computeIfAbsent(mod.getPlayer().getUuid(), k -> {
            LOGGER.info("EventQueueManager/getOrCreateEventQueueData: creating new queue data for entId={}",
                    mod.getPlayer().getUuidAsString());
            return new EventQueueData(mod);
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
