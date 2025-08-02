package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalDirectionXZ;
import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.Vec3d;

public class GoInDirectionXZTask extends CustomBaritoneGoalTask {
  private final Vec3d origin;
  
  private final Vec3d delta;
  
  private final double sidePenalty;
  
  public GoInDirectionXZTask(Vec3d origin, Vec3d delta, double sidePenalty) {
    this .origin = origin;
    this .delta = delta;
    this .sidePenalty = sidePenalty;
  }
  
  private static boolean closeEnough(Vec3d a, Vec3d b) {
    return (a.squaredDistanceTo(b) < 0.001D);
  }
  
  protected Goal newGoal(AltoClefController mod) {
    try {
      return (Goal)new GoalDirectionXZ(this .origin, this .delta, this .sidePenalty);
    } catch (Exception e) {
      Debug.logMessage("Invalid goal direction XZ (probably zero distance)");
      return null;
    } 
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.GoInDirectionXZTask) {
      adris.altoclef.tasks.movement.GoInDirectionXZTask task = (adris.altoclef.tasks.movement.GoInDirectionXZTask)other;
      return (closeEnough(task .origin, this .origin) && closeEnough(task .delta, this .delta));
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Going in direction: <" + this .origin.x + "," + this .origin.z + "> direction: <" + this .delta.x + "," + this .delta.z + ">";
  }
}
