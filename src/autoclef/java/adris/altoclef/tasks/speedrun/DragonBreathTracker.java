package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClefController;
import adris.altoclef.BotBehaviour;
import adris.altoclef.tasks.movement.CustomBaritoneGoalTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import java.util.HashSet;

import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalRunAway;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.util.math.BlockPos;

public class DragonBreathTracker {
  private final HashSet<BlockPos> breathBlocks = new HashSet<>();
  
  public void updateBreath(AltoClefController mod) {
    this.breathBlocks.clear();
    for (AreaEffectCloudEntity cloud : mod.getEntityTracker().getTrackedEntities(AreaEffectCloudEntity.class)) {
      for (BlockPos bad : WorldHelper.getBlocksTouchingBox(cloud.getBoundingBox()))
        this.breathBlocks.add(bad); 
    } 
  }
  
  public boolean isTouchingDragonBreath(BlockPos pos) {
    return this.breathBlocks.contains(pos);
  }
  
  public Task getRunAwayTask() {
    return (Task)new RunAwayFromDragonsBreathTask();
  }

  private class RunAwayFromDragonsBreathTask extends CustomBaritoneGoalTask {

    @Override
    protected void onStart() {
      super.onStart();
      BotBehaviour botBehaviour = controller.getBehaviour();

      botBehaviour.push();
      botBehaviour.setBlockPlacePenalty(Double.POSITIVE_INFINITY);
      // do NOT ever wander
      checker = new MovementProgressChecker((int) Float.POSITIVE_INFINITY);
    }

    @Override
    protected void onStop(Task interruptTask) {
      super.onStop(interruptTask);
      controller.getBehaviour().pop();
    }

    @Override
    protected Goal newGoal(AltoClefController mod) {
      return new GoalRunAway(10, breathBlocks.toArray(BlockPos[]::new));
    }

    @Override
    protected boolean isEqual(Task other) {
      return other instanceof RunAwayFromDragonsBreathTask;
    }

    @Override
    protected String toDebugString() {
      return "ESCAPE Dragons Breath";
    }
  }
}
