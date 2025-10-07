package adris.altoclef.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import adris.altoclef.AltoClefController;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.misc.UseItemOnBlockTask;
import adris.altoclef.util.helpers.FuzzySearchHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class UseItemOnBlockCommand extends Command {
    public UseItemOnBlockCommand() throws CommandException {
        super("useItemOnBlock",
                "Uses an item on a block. Use 'hand' as item if you want empty use (activating lever, etc.).  Examples: `useItemOnBlock flintandsteel tnt`, `useItemOnBlock wooden_axe oak_log` `useITemOnEntity hand lever` ",
                new Arg<>(String.class, "itemName"),
                new Arg<>(String.class, "blockName"));
    }

    @Override
    protected void call(AltoClefController mod, ArgParser parser) throws CommandException {
        String itemName = parser.get(String.class);
        String blockName = parser.get(String.class);
        List<String> allBlockNames = new ArrayList<>();
        Block block = null;
        for (Block b : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(b); // minecraft:stone, ...
            allBlockNames.add(id.toString());
            if (id.toString().contains(blockName)) {
                block = b;
            }
        }
        if (block == null) {
            String closest = FuzzySearchHelper.getClosestMatchMinecraftItems(blockName, allBlockNames);
            String errmsg = ("Block named: \"" + blockName + "\" not a valid block. Perhaps the user meant \"" + closest
                    + "\"?"
                    + (blockName.contains("log") ? " Can try 'log' as well" : ""));
            mod.logSignificantError(errmsg);
            this.finish();
        } else {
            mod.runUserTask(new UseItemOnBlockTask(itemName, block), () -> {
                this.finish();
            });
        }
    }

}