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
import net.minecraft.item.Items;

public class CollectNetherBricksTask extends ResourceTask {
  private final int count;
  
  public CollectNetherBricksTask(int count) {
    super(Items.NETHER_BRICKS, count);
    this .count = count;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    if (mod.getBlockScanner().anyFound(new Block[] { Blocks.NETHER_BRICKS }))
      return (Task)new MineAndCollectTask(Items.NETHER_BRICKS, this .count, new Block[] { Blocks.NETHER_BRICKS }, MiningRequirement.WOOD); 
    ItemTarget b = new ItemTarget(Items.NETHER_BRICK, 1);
    return (Task)new CraftInInventoryTask(new RecipeTarget(Items.NETHER_BRICK, this .count, CraftingRecipe.newShapedRecipe("nether_brick", new ItemTarget[] { b, b, b, b }, 1)));
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectNetherBricksTask;
  }
  
  protected String toDebugStringName() {
    return "Collecting " + this .count + " nether bricks.";
  }
}
