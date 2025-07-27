package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalDodgeProjectiles;
import baritone.api.pathing.goals.Goal;

public class DodgeProjectilesTask extends CustomBaritoneGoalTask {
  private final double _distanceHorizontal;
  
  private final double _distanceVertical;
  
  public DodgeProjectilesTask(double distanceHorizontal, double distanceVertical) {
    this._distanceHorizontal = distanceHorizontal;
    this._distanceVertical = distanceVertical;
  }

  protected Task onTick() {
    if (this.cachedGoal != null) {
      GoalDodgeProjectiles goalDodgeProjectiles = (GoalDodgeProjectiles) this.cachedGoal;
    }
    return super.onTick();
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.DodgeProjectilesTask) {
      adris.altoclef.tasks.movement.DodgeProjectilesTask task = (adris.altoclef.tasks.movement.DodgeProjectilesTask)other;
      if (Math.abs(task._distanceHorizontal - this._distanceHorizontal) > 1.0D)
        return false; 
      if (Math.abs(task._distanceVertical - this._distanceVertical) > 1.0D)
        return false; 
      return true;
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Dodge arrows at " + this._distanceHorizontal + " blocks away";
  }
  
  protected Goal newGoal(AltoClefController mod) {
    return (Goal)new GoalDodgeProjectiles(mod, this._distanceHorizontal, this._distanceVertical);
  }
}
