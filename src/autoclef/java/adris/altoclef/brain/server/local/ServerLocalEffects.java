package adris.altoclef.brain.server.local;

import java.util.function.Consumer;

import adris.altoclef.AltoClefController;
import adris.altoclef.brain.server.local.AgentSideEffects.CommandExecutionStopReason;
import adris.altoclef.commandsystem.CommandExecutor;

public class ServerLocalEffects {
    // What effects local state:

    public static void onCommandListGenerated(AltoClefController mod, String command,
            Consumer<CommandExecutionStopReason> onStop) {
        CommandExecutor cmdExecutor = mod.getCommandExecutor();
        String commandWithPrefix = cmdExecutor.isClientCommand(command) ? command
                : (cmdExecutor.getCommandPrefix() + command);
        if (commandWithPrefix.equals("@stop")) {
            mod.isStopping = true;
        } else {
            mod.isStopping = false;
        }
        cmdExecutor.execute(commandWithPrefix, () -> {
            if (mod.isStopping) {
                System.out.printf(
                        "[AgentSideEffects/AgentSideEffects]: (%s) was cancelled. Not adding finish event to queue.",
                        commandWithPrefix);
                // Other canceled logic here
                onStop.accept(new CommandExecutionStopReason.Cancelled(commandWithPrefix));
            } else {
                onStop.accept(new CommandExecutionStopReason.Finished(commandWithPrefix));
            }
        }, (err) -> {
            onStop.accept(new CommandExecutionStopReason.Error(commandWithPrefix, err.getMessage()));
        });
    }
}
