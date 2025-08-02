package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalYLevel;

public class GetToYTask extends CustomBaritoneGoalTask {
  private final int yLevel;
  
  private final Dimension dimension;
  
  public GetToYTask(int ylevel, Dimension dimension) {
    this .yLevel = ylevel;
    this .dimension = dimension;
  }
  
  public GetToYTask(int ylevel) {
    this(ylevel, null);
  }
  
  protected Task onTick() {
    if (this .dimension != null && WorldHelper.getCurrentDimension(controller) != this .dimension)
      return (Task)new DefaultGoToDimensionTask(this .dimension); 
    return super.onTick();
  }
  
  protected Goal newGoal(AltoClefController mod) {
    return (Goal)new GoalYLevel(this .yLevel);
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.GetToYTask) {
      adris.altoclef.tasks.movement.GetToYTask task = (adris.altoclef.tasks.movement.GetToYTask)other;
      return (task .yLevel == this .yLevel);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Going to y=" + this .yLevel + ((this .dimension != null) ? ("in dimension" + String.valueOf(this .dimension)) : "");
  }
}
