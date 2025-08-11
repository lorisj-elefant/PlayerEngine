/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package altoclef.tasks.speedrun;

import altoclef.AltoClefController;
import altoclef.BotBehaviour;
import altoclef.tasks.movement.CustomBaritoneGoalTask;
import altoclef.tasksystem.Task;
import altoclef.util.helpers.WorldHelper;
import altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalRunAway;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AreaEffectCloud;

public class DragonBreathTracker {
   private final HashSet<BlockPos> breathBlocks = new HashSet<>();

   public void updateBreath(AltoClefController mod) {
      this.breathBlocks.clear();

      for (AreaEffectCloud cloud : mod.getEntityTracker().getTrackedEntities(AreaEffectCloud.class)) {
         for (BlockPos bad : WorldHelper.getBlocksTouchingBox(cloud.getBoundingBox())) {
            this.breathBlocks.add(bad);
         }
      }
   }

   public boolean isTouchingDragonBreath(BlockPos pos) {
      return this.breathBlocks.contains(pos);
   }

   public Task getRunAwayTask() {
      return new DragonBreathTracker.RunAwayFromDragonsBreathTask();
   }

   private class RunAwayFromDragonsBreathTask extends CustomBaritoneGoalTask {
      @Override
      protected void onStart() {
         super.onStart();
         BotBehaviour botBehaviour = this.controller.getBehaviour();
         botBehaviour.push();
         botBehaviour.setBlockPlacePenalty(Double.POSITIVE_INFINITY);
         this.checker = new MovementProgressChecker(Integer.MAX_VALUE);
      }

      @Override
      protected void onStop(Task interruptTask) {
         super.onStop(interruptTask);
         this.controller.getBehaviour().pop();
      }

      @Override
      protected Goal newGoal(AltoClefController mod) {
         return new GoalRunAway(10.0, DragonBreathTracker.this.breathBlocks.toArray(BlockPos[]::new));
      }

      @Override
      protected boolean isEqual(Task other) {
         return other instanceof DragonBreathTracker.RunAwayFromDragonsBreathTask;
      }

      @Override
      protected String toDebugString() {
         return "ESCAPE Dragons Breath";
      }
   }
}
