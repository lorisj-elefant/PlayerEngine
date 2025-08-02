package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.RecipeTarget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class CollectSandstoneTask extends ResourceTask {
    private final int count;

    public CollectSandstoneTask(int targetCount) {
        super(Items.SANDSTONE, targetCount);
        this.count = targetCount;
    }

    protected boolean shouldAvoidPickingUp(AltoClefController mod) {
        return false;
    }

    protected void onResourceStart(AltoClefController mod) {
    }

    protected Task onResourceTick(AltoClefController mod) {
        if (mod.getItemStorage().getItemCount(new Item[]{Items.SAND}) >= 4) {
            int target = mod.getItemStorage().getItemCount(new Item[]{Items.SANDSTONE}) + 1;
            ItemTarget s = new ItemTarget(Items.SAND, 1);
            return (Task) new CraftInInventoryTask(new RecipeTarget(Items.SANDSTONE, target, CraftingRecipe.newShapedRecipe("sandstone", new ItemTarget[]{s, s, s, s}, 1)));
        }
        return (Task) (new MineAndCollectTask(new ItemTarget(new Item[]{Items.SANDSTONE, Items.SAND}), new Block[]{Blocks.SANDSTONE, Blocks.SAND}, MiningRequirement.WOOD)).forceDimension(Dimension.OVERWORLD);
    }

    protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    }

    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof adris.altoclef.tasks.resources.CollectSandstoneTask;
    }

    protected String toDebugStringName() {
        return "Collecting " + this.count + " sandstone.";
    }
}
