package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.util.math.BlockPos;

public class GetToXZTask extends CustomBaritoneGoalTask {
  private final int x;
  
  private final int z;
  
  private final Dimension dimension;
  
  public GetToXZTask(int x, int z) {
    this(x, z, null);
  }
  
  public GetToXZTask(int x, int z, Dimension dimension) {
    this.x = x;
    this.z = z;
    this.dimension = dimension;
  }
  
  protected Task onTick() {
    if (this.dimension != null && WorldHelper.getCurrentDimension(controller) != this.dimension)
      return (Task)new DefaultGoToDimensionTask(this.dimension); 
    return super.onTick();
  }
  
  protected Goal newGoal(AltoClefController mod) {
    return (Goal)new GoalXZ(this.x, this.z);
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.GetToXZTask) {
      adris.altoclef.tasks.movement.GetToXZTask task = (adris.altoclef.tasks.movement.GetToXZTask)other;
      return (task.x == this.x && task.z == this.z && task.dimension == this.dimension);
    } 
    return false;
  }
  
  public boolean isFinished() {
    BlockPos cur = controller.getPlayer().getBlockPos();
    return (cur.getX() == this.x && cur.getZ() == this.z && (this.dimension == null || this.dimension == WorldHelper.getCurrentDimension(controller)));
  }
  
  protected String toDebugString() {
    return "Getting to (" + this.x + "," + this.z + ")" + ((this.dimension != null) ? (" in dimension " + String.valueOf(this.dimension)) : "");
  }
}
