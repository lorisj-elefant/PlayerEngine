package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalYLevel;

public class GetToYTask extends CustomBaritoneGoalTask {
  private final int _yLevel;
  
  private final Dimension _dimension;
  
  public GetToYTask(int ylevel, Dimension dimension) {
    this._yLevel = ylevel;
    this._dimension = dimension;
  }
  
  public GetToYTask(int ylevel) {
    this(ylevel, null);
  }
  
  protected Task onTick() {
    if (this._dimension != null && WorldHelper.getCurrentDimension(controller) != this._dimension)
      return (Task)new DefaultGoToDimensionTask(this._dimension); 
    return super.onTick();
  }
  
  protected Goal newGoal(AltoClefController mod) {
    return (Goal)new GoalYLevel(this._yLevel);
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.GetToYTask) {
      adris.altoclef.tasks.movement.GetToYTask task = (adris.altoclef.tasks.movement.GetToYTask)other;
      return (task._yLevel == this._yLevel);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Going to y=" + this._yLevel + ((this._dimension != null) ? ("in dimension" + String.valueOf(this._dimension)) : "");
  }
}
