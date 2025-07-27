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

public class CollectDripstoneBlockTask extends ResourceTask {
  private final int _count;
  
  public CollectDripstoneBlockTask(int targetCount) {
    super(Items.DRIPSTONE_BLOCK, targetCount);
    this._count = targetCount;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    if (mod.getItemStorage().getItemCount(new Item[] { Items.POINTED_DRIPSTONE }) >= 4) {
      int target = mod.getItemStorage().getItemCount(new Item[] { Items.DRIPSTONE_BLOCK }) + 1;
      ItemTarget s = new ItemTarget(Items.POINTED_DRIPSTONE, 1);
      return (Task)new CraftInInventoryTask(new RecipeTarget(Items.DRIPSTONE_BLOCK, target, CraftingRecipe.newShapedRecipe("dri", new ItemTarget[] { s, s, s, s }, 1)));
    } 
    return (Task)(new MineAndCollectTask(new ItemTarget(Items.DRIPSTONE_BLOCK, Items.POINTED_DRIPSTONE), new Block[] { Blocks.DRIPSTONE_BLOCK, Blocks.POINTED_DRIPSTONE }, MiningRequirement.WOOD)).forceDimension(Dimension.OVERWORLD);
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectDripstoneBlockTask;
  }
  
  protected String toDebugStringName() {
    return "Collecting " + this._count + " Dripstone Blocks.";
  }
}
