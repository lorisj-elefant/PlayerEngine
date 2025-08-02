package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalChunk;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.ChunkPos;

public class GetToChunkTask extends CustomBaritoneGoalTask {
  private final ChunkPos pos;
  
  public GetToChunkTask(ChunkPos pos) {
    this.checker = new MovementProgressChecker();
    this .pos = pos;
  }
  
  protected Goal newGoal(AltoClefController mod) {
    return (Goal)new GoalChunk(this .pos);
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.GetToChunkTask) {
      adris.altoclef.tasks.movement.GetToChunkTask task = (adris.altoclef.tasks.movement.GetToChunkTask)other;
      return task .pos.equals(this .pos);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Get to chunk: " + this .pos.toString();
  }
}
