package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalRunAway;
import java.util.Arrays;
import net.minecraft.util.math.BlockPos;

public class RunAwayFromPositionTask extends CustomBaritoneGoalTask {
  private final BlockPos[] _dangerBlocks;
  
  private final double _distance;
  
  private final Integer _maintainY;
  
  public RunAwayFromPositionTask(double distance, BlockPos... toRunAwayFrom) {
    this(distance, null, toRunAwayFrom);
  }
  
  public RunAwayFromPositionTask(double distance, Integer maintainY, BlockPos... toRunAwayFrom) {
    this._distance = distance;
    this._dangerBlocks = toRunAwayFrom;
    this._maintainY = maintainY;
  }
  
  protected Goal newGoal(AltoClefController mod) {
    return (Goal)new GoalRunAway(this._distance, this._maintainY, this._dangerBlocks);
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.RunAwayFromPositionTask) {
      adris.altoclef.tasks.movement.RunAwayFromPositionTask task = (adris.altoclef.tasks.movement.RunAwayFromPositionTask)other;
      return Arrays.equals(task._dangerBlocks, this._dangerBlocks);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Running away from " + Arrays.toString(this._dangerBlocks);
  }
}
