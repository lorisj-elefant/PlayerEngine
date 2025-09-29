package adris.altoclef.commands;

import adris.altoclef.AltoClefController;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.movement.SimpleExploreTask;

public class SimpleExploreCommand extends Command {
    public SimpleExploreCommand() throws CommandException {
        super("explore",
                "explores surrrounding area");
    }

    @Override
    protected void call(AltoClefController mod, ArgParser parser) throws CommandException {
        mod.runUserTask(new SimpleExploreTask(), () -> {
            System.out.println("Simple explore task done");
            this.finish();
        });
    }

}