package adris.altoclef.commands;

import adris.altoclef.AltoClefController;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.movement.BodyLanguageTask;

public class DanceCommand extends Command{
    public DanceCommand() throws CommandException {
        super("dance", "Perform some sort of dance/body language action. Action must be either `greeting`, `nod_head`, `shake_head`, `victory` Examples: `dance greeting` to do a greeting dance. `dance shake_head` shakes head, like saying no.", new Arg<>(String.class, "bodyLanguage"));
    }
    @Override
    protected void call(AltoClefController mod, ArgParser parser) throws CommandException {
        String bodyLanguage = parser.get(String.class);

        mod.runUserTask(new BodyLanguageTask(bodyLanguage));
    }

}