package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalDodgeProjectiles;
import baritone.api.pathing.goals.Goal;

public class DodgeProjectilesTask extends CustomBaritoneGoalTask {
  private final double distanceHorizontal;
  
  private final double distanceVertical;
  
  public DodgeProjectilesTask(double distanceHorizontal, double distanceVertical) {
    this .distanceHorizontal = distanceHorizontal;
    this .distanceVertical = distanceVertical;
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
      if (Math.abs(task .distanceHorizontal - this .distanceHorizontal) > 1.0D)
        return false; 
      if (Math.abs(task .distanceVertical - this .distanceVertical) > 1.0D)
        return false; 
      return true;
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Dodge arrows at " + this .distanceHorizontal + " blocks away";
  }
  
  protected Goal newGoal(AltoClefController mod) {
    return (Goal)new GoalDodgeProjectiles(mod, this .distanceHorizontal, this .distanceVertical);
  }
}
