package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class GetCloseToBlockTask extends Task {
  private final BlockPos _toApproach;
  
  private int _currentRange;
  
  public GetCloseToBlockTask(BlockPos toApproach) {
    this._toApproach = toApproach;
  }
  
  protected void onStart() {
    this._currentRange = Integer.MAX_VALUE;
  }
  
  protected Task onTick() {
    if (inRange())
      this._currentRange = getCurrentDistance() - 1; 
    return (Task)new GetWithinRangeOfBlockTask(this._toApproach, this._currentRange);
  }
  
  protected void onStop(Task interruptTask) {}
  
  private int getCurrentDistance() {
    return (int)Math.sqrt(controller.getPlayer().getBlockPos().getSquaredDistance((Vec3i)this._toApproach));
  }
  
  private boolean inRange() {
    return (controller.getPlayer().getBlockPos().getSquaredDistance((Vec3i)this._toApproach) <= (this._currentRange * this._currentRange));
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.GetCloseToBlockTask) {
      adris.altoclef.tasks.movement.GetCloseToBlockTask task = (adris.altoclef.tasks.movement.GetCloseToBlockTask)other;
      return task._toApproach.equals(this._toApproach);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Approaching " + this._toApproach.toShortString();
  }
}
