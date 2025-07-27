package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;

public class CollectFlintTask extends ResourceTask {
  private static final float CLOSE_ENOUGH_FLINT = 10.0F;
  
  private final int _count;
  
  public CollectFlintTask(int targetCount) {
    super(Items.FLINT, targetCount);
    this._count = targetCount;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    Optional<BlockPos> closest = mod.getBlockScanner().getNearestBlock(mod.getPlayer().getPos(), validGravel -> (WorldHelper.fallingBlockSafeToBreak(controller, validGravel) && WorldHelper.canBreak(controller, validGravel)), new Block[] { Blocks.GRAVEL });
    if (closest.isPresent() && ((BlockPos)closest.get()).isCenterWithinDistance((Position)mod.getPlayer().getPos(), 10.0D))
      return (Task)new DoToClosestBlockTask(adris.altoclef.tasks.construction.DestroyBlockTask::new, new Block[] { Blocks.GRAVEL }); 
    if (mod.getItemStorage().hasItem(new Item[] { Items.GRAVEL }))
      return (Task)new PlaceBlockNearbyTask(new Block[] { Blocks.GRAVEL }); 
    return (Task)TaskCatalogue.getItemTask(Items.GRAVEL, 1);
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.resources.CollectFlintTask) {
      adris.altoclef.tasks.resources.CollectFlintTask task = (adris.altoclef.tasks.resources.CollectFlintTask)other;
      return (task._count == this._count);
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Collect " + this._count + " flint";
  }
}
