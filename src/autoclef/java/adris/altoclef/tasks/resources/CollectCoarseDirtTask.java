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
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;

public class CollectCoarseDirtTask extends ResourceTask {
  private static final float CLOSE_ENOUGH_COARSE_DIRT = 128.0F;
  
  private final int count;
  
  public CollectCoarseDirtTask(int targetCount) {
    super(Items.COARSE_DIRT, targetCount);
    this .count = targetCount;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    double c = Math.ceil((this .count - mod.getItemStorage().getItemCount(new Item[] { Items.COARSE_DIRT })) / 4.0D) * 2.0D;
    Optional<BlockPos> closest = mod.getBlockScanner().getNearestBlock(new Block[] { Blocks.COARSE_DIRT });
    if ((mod.getItemStorage().getItemCount(new Item[] { Items.DIRT }) < c || mod
      .getItemStorage().getItemCount(new Item[] { Items.GRAVEL }) < c) && closest
      .isPresent() && ((BlockPos)closest.get()).isCenterWithinDistance((Position)mod.getPlayer().getPos(), 128.0D))
      return (Task)(new MineAndCollectTask(new ItemTarget(Items.COARSE_DIRT), new Block[] { Blocks.COARSE_DIRT }, MiningRequirement.HAND)).forceDimension(Dimension.OVERWORLD); 
    int target = this .count;
    ItemTarget d = new ItemTarget(Items.DIRT, 1);
    ItemTarget g = new ItemTarget(Items.GRAVEL, 1);
    return (Task)new CraftInInventoryTask(new RecipeTarget(Items.COARSE_DIRT, target, CraftingRecipe.newShapedRecipe("coarse_dirt", new ItemTarget[] { d, g, g, d }, 4)));
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectCoarseDirtTask;
  }
  
  protected String toDebugStringName() {
    return "Collecting " + this .count + " Coarse Dirt.";
  }
}
