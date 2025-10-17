package com.player2.playerengine.commands;

import com.player2.playerengine.PlayerEngineController;
import com.player2.playerengine.commands.base.ArgParser;
import com.player2.playerengine.commands.base.Command;
import com.player2.playerengine.commands.base.CommandException;
import com.player2.playerengine.tasks.entity.UseItemOnEntityTask;

public class UseItemOnEntityCommand extends Command {
    public UseItemOnEntityCommand() throws CommandException {
        super("useItemOnEntity",
                "Uses an item on an entity. Use 'hand' as item if you want empty use (get in vehicle, etc.).  Examples: `useItemOnEntity shears sheep`, `useItemOnEntity saddle pig` `useITemOnEntity hand minecart` ",
                new Arg<>(String.class, "itemName"),
                new Arg<>(String.class, "entityName"));
    }

    @Override
    protected void call(PlayerEngineController mod, ArgParser parser) throws CommandException {
        String itemName = parser.get(String.class);
        String entityName = parser.get(String.class);
        mod.runUserTask(new UseItemOnEntityTask(itemName, entityName), () -> {
            this.finish();
        });
    }

}