package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.RecipeTarget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class CollectWheatTask extends ResourceTask {
  private final int count;
  
  public CollectWheatTask(int targetCount) {
    super(Items.WHEAT, targetCount);
    this .count = targetCount;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    int potentialCount = mod.getItemStorage().getItemCount(new Item[] { Items.WHEAT }) + 9 * mod.getItemStorage().getItemCount(new Item[] { Items.HAY_BLOCK });
    if (potentialCount >= this .count) {
      setDebugState("Crafting wheat");
      return (Task)new CraftInInventoryTask(new RecipeTarget(Items.WHEAT, this .count, CraftingRecipe.newShapedRecipe("wheat", new ItemTarget[] { new ItemTarget(Items.HAY_BLOCK, 1), null, null, null },9)));
    } 
    if (mod.getBlockScanner().anyFound(new Block[] { Blocks.HAY_BLOCK }) || mod.getEntityTracker().itemDropped(new Item[] { Items.HAY_BLOCK }))
      return (Task)new MineAndCollectTask(Items.HAY_BLOCK, 99999999, new Block[] { Blocks.HAY_BLOCK }, MiningRequirement.HAND); 
    return (Task)new CollectCropTask(new ItemTarget(Items.WHEAT, this .count), new Block[] { Blocks.WHEAT }, new Item[] { Items.WHEAT_SEEDS });
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectWheatTask;
  }
  
  protected String toDebugStringName() {
    return "Collecting " + this .count + " wheat.";
  }
}
