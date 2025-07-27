package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalDirectionXZ;
import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.Vec3d;

public class GoInDirectionXZTask extends CustomBaritoneGoalTask {
  private final Vec3d _origin;
  
  private final Vec3d _delta;
  
  private final double _sidePenalty;
  
  public GoInDirectionXZTask(Vec3d origin, Vec3d delta, double sidePenalty) {
    this._origin = origin;
    this._delta = delta;
    this._sidePenalty = sidePenalty;
  }
  
  private static boolean closeEnough(Vec3d a, Vec3d b) {
    return (a.squaredDistanceTo(b) < 0.001D);
  }
  
  protected Goal newGoal(AltoClefController mod) {
    try {
      return (Goal)new GoalDirectionXZ(this._origin, this._delta, this._sidePenalty);
    } catch (Exception e) {
      Debug.logMessage("Invalid goal direction XZ (probably zero distance)");
      return null;
    } 
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.GoInDirectionXZTask) {
      adris.altoclef.tasks.movement.GoInDirectionXZTask task = (adris.altoclef.tasks.movement.GoInDirectionXZTask)other;
      return (closeEnough(task._origin, this._origin) && closeEnough(task._delta, this._delta));
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Going in direction: <" + this._origin.x + "," + this._origin.z + "> direction: <" + this._delta.x + "," + this._delta.z + ">";
  }
}
