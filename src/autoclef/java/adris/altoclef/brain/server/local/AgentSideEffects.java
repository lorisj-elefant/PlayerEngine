package adris.altoclef.brain.server.local;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.AltoClefController;
import adris.altoclef.brain.client.TTSQueue;
import adris.altoclef.brain.server.Event;
import adris.altoclef.brain.server.EventQueueManager;
import adris.altoclef.commandsystem.CommandExecutor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class AgentSideEffects {
    private static final Logger LOGGER = LogManager.getLogger();

    public sealed interface CommandExecutionStopReason
            permits CommandExecutionStopReason.Cancelled,
            CommandExecutionStopReason.Finished,
            CommandExecutionStopReason.Error {
        String commandName();

        record Cancelled(String commandName) implements CommandExecutionStopReason {
        }

        record Finished(String commandName) implements CommandExecutionStopReason {
        }

        record Error(String commandName, String errMsg) implements CommandExecutionStopReason {
        }
    }

    public static void onEntityMessage(Event.CharacterMessage characterMessage) {
        // message part:
        if (characterMessage.message() != null) {
            String message = String.format("<%s> %s", characterMessage.sendingCharacterData().getUsername(), characterMessage.message());
            broadcastChatMessage(message, null);
            TTSQueue.TTS(characterMessage.message(),
                    characterMessage.sendingCharacterData().getCharacter());
            EventQueueManager.onAICharacterMessage(characterMessage, characterMessage.sendingCharacterData().getUUID());
        }

        // command part:
        if (characterMessage.command() != null) {
            onCommandListGenerated(characterMessage.sendingCharacterData().getMod(), characterMessage.command(),
                    characterMessage.sendingCharacterData()::onCommandFinish);
        }
    }

    private static void broadcastChatMessage(String message, MinecraftServer server){
        for(ServerPlayer player : server.getPlayerList().getPlayers()){
            player.displayClientMessage(Component.literal(message), false);
        }
    }

    public static void onError(String errMsg) {
        LOGGER.error(errMsg);
    }
}
