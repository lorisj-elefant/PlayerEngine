package adris.altoclef.commands;

import adris.altoclef.AltoClefController;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.entity.UseItemOnEntityTask;

public class UseItemOnEntityCommand extends Command {
    public UseItemOnEntityCommand() throws CommandException {
        super("useItemOnEntity",
                "Uses an item on an entity. Use 'hand' as item if you want empty use (get in vehicle, etc.).  Examples: `useItemOnEntity shears sheep`, `useItemOnEntity saddle pig` `useITemOnEntity hand minecart` ",
                new Arg<>(String.class, "itemName"),
                new Arg<>(String.class, "entityName"));
    }

    @Override
    protected void call(AltoClefController mod, ArgParser parser) throws CommandException {
        String itemName = parser.get(String.class);
        String entityName = parser.get(String.class);
        mod.runUserTask(new UseItemOnEntityTask(itemName, entityName), () -> {
            this.finish();
        });
    }

}