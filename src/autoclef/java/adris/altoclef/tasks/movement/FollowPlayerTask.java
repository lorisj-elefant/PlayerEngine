package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

public class FollowPlayerTask extends Task {
  private final String playerName;
  
  private final double followDistance;
  
  public FollowPlayerTask(String playerName, double followDistance) {
    this .playerName = playerName;
    this .followDistance = followDistance;
  }
  
  public FollowPlayerTask(String playerName) {
    this(playerName, 2.0D);
  }
  
  protected void onStart() {}
  
  protected Task onTick() {
    AltoClefController mod = controller;
    Optional<Vec3d> lastPos = mod.getEntityTracker().getPlayerMostRecentPosition(this .playerName);
    if (lastPos.isEmpty()) {
      setDebugState("No player found/detected. Doing nothing until player loads into render distance.");
      return null;
    } 
    Vec3d target = lastPos.get();
    if (target.isInRange((Position)mod.getPlayer().getPos(), 1.0D) && !mod.getEntityTracker().isPlayerLoaded(this .playerName)) {
      mod.logWarning("Failed to get to player \"" + this .playerName + "\". We moved to where we last saw them but now have no idea where they are.");
      stop();
      return null;
    } 
    Optional<PlayerEntity> player = mod.getEntityTracker().getPlayerEntity(this .playerName);
    if (player.isEmpty())
      return (Task)new GetToBlockTask(new BlockPos((int)target.x, (int)target.y, (int)target.z), false); 
    return (Task)new GetToEntityTask((Entity)player.get(), this .followDistance);
  }
  
  protected void onStop(Task interruptTask) {}
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.FollowPlayerTask) {
      adris.altoclef.tasks.movement.FollowPlayerTask task = (adris.altoclef.tasks.movement.FollowPlayerTask)other;
      return (task .playerName.equals(this .playerName) && Math.abs(this .followDistance - task .followDistance) < 0.1D);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Going to player " + this .playerName;
  }
}
