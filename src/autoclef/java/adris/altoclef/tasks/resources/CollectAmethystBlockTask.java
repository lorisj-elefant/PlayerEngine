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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class CollectAmethystBlockTask extends ResourceTask {
  private final int _count;
  
  public CollectAmethystBlockTask(int targetCount) {
    super(Items.AMETHYST_BLOCK, targetCount);
    this._count = targetCount;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {
    mod.getBehaviour().push();
    mod.getBehaviour().avoidBlockBreaking(blockPos -> {
          BlockState s = mod.getWorld().getBlockState(blockPos);
          return (s.getBlock() == Blocks.BUDDING_AMETHYST);
        });
  }
  
  protected Task onResourceTick(AltoClefController mod) {
    if (mod.getItemStorage().getItemCount(new Item[] { Items.AMETHYST_SHARD }) >= 4) {
      int target = mod.getItemStorage().getItemCount(new Item[] { Items.AMETHYST_BLOCK }) + 1;
      ItemTarget s = new ItemTarget(Items.AMETHYST_SHARD, 1);
      return (Task)new CraftInInventoryTask(new RecipeTarget(Items.AMETHYST_BLOCK, target, CraftingRecipe.newShapedRecipe("amethyst_block", new ItemTarget[] { s, s, s, s }, 1)));
    } 
    return (Task)(new MineAndCollectTask(new ItemTarget(Items.AMETHYST_BLOCK, Items.AMETHYST_SHARD), new Block[] { Blocks.AMETHYST_BLOCK, Blocks.AMETHYST_CLUSTER }, MiningRequirement.WOOD)).forceDimension(Dimension.OVERWORLD);
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    mod.getBehaviour().pop();
  }
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectAmethystBlockTask;
  }
  
  protected String toDebugStringName() {
    return "Collecting " + this._count + " Amethyst Blocks.";
  }
}
