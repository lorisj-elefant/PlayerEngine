package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.ItemHelper;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;

public class CollectSticksTask extends ResourceTask {
  private final int targetCount;
  
  public CollectSticksTask(int targetCount) {
    super(Items.STICK, targetCount);
    this .targetCount = targetCount;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {
    mod.getBehaviour().push();
  }
  
  protected double getPickupRange(AltoClefController mod) {
    ItemStorageTracker storage = mod.getItemStorage();
    if (storage.getItemCount(ItemHelper.PLANKS) * 4 + storage.getItemCount(ItemHelper.LOG) * 4 * 4 > this .targetCount)
      return 10.0D; 
    return 35.0D;
  }
  
  protected Task onResourceTick(AltoClefController mod) {
    if (mod.getItemStorage().getItemCount(new Item[] { Items.BAMBOO }) >= 2)
      return (Task)new CraftInInventoryTask(new RecipeTarget(Items.STICK, Math.min(mod.getItemStorage().getItemCount(Items.BAMBOO) / 2, this .targetCount), CraftingRecipe.newShapedRecipe("sticks", new ItemTarget[] { new ItemTarget("bamboo"), null, new ItemTarget("bamboo"), null }, 1)));
    Optional<BlockPos> nearestBush = mod.getBlockScanner().getNearestBlock(new Block[] { Blocks.DEAD_BUSH });
    if (nearestBush.isPresent() && ((BlockPos)nearestBush.get()).isCenterWithinDistance((Position)mod.getPlayer().getPos(), 20.0D)) {
      MineAndCollectTask mineAndCollectTask = new MineAndCollectTask(Items.DEAD_BUSH, 1, new Block[] { Blocks.DEAD_BUSH }, MiningRequirement.HAND);
      return (Task)mineAndCollectTask;
    } 
    return (Task)new CraftInInventoryTask(new RecipeTarget(Items.STICK, this .targetCount, CraftingRecipe.newShapedRecipe("sticks", new ItemTarget[] { new ItemTarget("planks"), null, new ItemTarget("planks"), null }, 4)));
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    mod.getBehaviour().pop();
  }
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectSticksTask;
  }
  
  protected String toDebugStringName() {
    return "Crafting " + this .targetCount + " sticks";
  }
}
