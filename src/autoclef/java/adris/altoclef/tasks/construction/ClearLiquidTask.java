package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;

public class ClearLiquidTask extends Task {
  private final BlockPos _liquidPos;
  
  public ClearLiquidTask(BlockPos liquidPos) {
    this._liquidPos = liquidPos;
  }
  
  protected void onStart() {}
  
  protected Task onTick() {
    if (controller.getItemStorage().hasItem(new Item[] { Items.BUCKET })) {
      controller.getBehaviour().setRayTracingFluidHandling(RaycastContext.FluidHandling.SOURCE_ONLY);
      return (Task)new InteractWithBlockTask(new ItemTarget(Items.BUCKET, 1), this._liquidPos, false);
    } 
    return (Task)new PlaceStructureBlockTask(this._liquidPos);
  }
  
  protected void onStop(Task interruptTask) {}
  
  public boolean isFinished() {
    if (controller.getChunkTracker().isChunkLoaded(this._liquidPos))
      return controller.getWorld().getBlockState(this._liquidPos).getFluidState().isEmpty();
    return false;
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.construction.ClearLiquidTask) {
      adris.altoclef.tasks.construction.ClearLiquidTask task = (adris.altoclef.tasks.construction.ClearLiquidTask)other;
      return task._liquidPos.equals(this._liquidPos);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Clear liquid at " + String.valueOf(this._liquidPos);
  }
}
