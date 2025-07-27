package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.RecipeTarget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

public class CollectHayBlockTask extends ResourceTask {
  private final int count;
  
  public CollectHayBlockTask(int count) {
    super(Items.HAY_BLOCK, count);
    this.count = count;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    if (mod.getBlockScanner().anyFound(new Block[] { Blocks.HAY_BLOCK }))
      return (Task)new MineAndCollectTask(Items.HAY_BLOCK, this.count, new Block[] { Blocks.HAY_BLOCK }, MiningRequirement.HAND); 
    ItemTarget w = new ItemTarget(Items.WHEAT, 1);
    return (Task)new CraftInTableTask(new RecipeTarget(Items.HAY_BLOCK, this.count, CraftingRecipe.newShapedRecipe("hay_block", new ItemTarget[] { w, w, w, w, w, w, w, w, w }, 1)));
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectHayBlockTask;
  }
  
  protected String toDebugStringName() {
    return "Collecting " + this.count + " hay blocks.";
  }
}
