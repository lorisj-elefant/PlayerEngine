package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class GetCloseToBlockTask extends Task {
  private final BlockPos toApproach;
  
  private int currentRange;
  
  public GetCloseToBlockTask(BlockPos toApproach) {
    this .toApproach = toApproach;
  }
  
  protected void onStart() {
    this .currentRange = Integer.MAX_VALUE;
  }
  
  protected Task onTick() {
    if (inRange())
      this .currentRange = getCurrentDistance() - 1; 
    return (Task)new GetWithinRangeOfBlockTask(this .toApproach, this .currentRange);
  }
  
  protected void onStop(Task interruptTask) {}
  
  private int getCurrentDistance() {
    return (int)Math.sqrt(controller.getPlayer().getBlockPos().getSquaredDistance((Vec3i)this .toApproach));
  }
  
  private boolean inRange() {
    return (controller.getPlayer().getBlockPos().getSquaredDistance((Vec3i)this .toApproach) <= (this .currentRange * this .currentRange));
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.GetCloseToBlockTask) {
      adris.altoclef.tasks.movement.GetCloseToBlockTask task = (adris.altoclef.tasks.movement.GetCloseToBlockTask)other;
      return task .toApproach.equals(this .toApproach);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Approaching " + this .toApproach.toShortString();
  }
}
