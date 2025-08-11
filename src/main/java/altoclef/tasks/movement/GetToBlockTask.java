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

package altoclef.tasks.movement;

import altoclef.AltoClefController;
import altoclef.Debug;
import altoclef.tasksystem.ITaskRequiresGrounded;
import altoclef.tasksystem.Task;
import altoclef.util.Dimension;
import altoclef.util.helpers.WorldHelper;
import altoclef.util.time.TimerGame;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.core.BlockPos;

public class GetToBlockTask extends CustomBaritoneGoalTask implements ITaskRequiresGrounded {
   private final BlockPos position;
   private final boolean preferStairs;
   private final Dimension dimension;
   private int finishedTicks = 0;
   private final TimerGame wanderTimer = new TimerGame(2.0);

   public GetToBlockTask(BlockPos position, boolean preferStairs) {
      this(position, preferStairs, null);
   }

   public GetToBlockTask(BlockPos position, Dimension dimension) {
      this(position, false, dimension);
   }

   public GetToBlockTask(BlockPos position, boolean preferStairs, Dimension dimension) {
      this.dimension = dimension;
      this.position = position;
      this.preferStairs = preferStairs;
   }

   public GetToBlockTask(BlockPos position) {
      this(position, false);
   }

   @Override
   protected Task onTick() {
      if (this.dimension != null && WorldHelper.getCurrentDimension(this.controller) != this.dimension) {
         return new DefaultGoToDimensionTask(this.dimension);
      } else {
         if (this.isFinished()) {
            this.finishedTicks++;
         } else {
            this.finishedTicks = 0;
         }

         if (this.finishedTicks > 200) {
            this.wanderTimer.reset();
            Debug.logWarning("GetToBlock was finished for 10 seconds yet is still being called, wandering");
            this.finishedTicks = 0;
            return new TimeoutWanderTask();
         } else {
            return (Task)(!this.wanderTimer.elapsed() ? new TimeoutWanderTask() : super.onTick());
         }
      }
   }

   @Override
   protected void onStart() {
      super.onStart();
      if (this.preferStairs) {
         this.controller.getBehaviour().push();
         this.controller.getBehaviour().setPreferredStairs(true);
      }
   }

   @Override
   protected void onStop(Task interruptTask) {
      super.onStop(interruptTask);
      if (this.preferStairs) {
         this.controller.getBehaviour().pop();
      }
   }

   @Override
   protected boolean isEqual(Task other) {
      return !(other instanceof GetToBlockTask task)
         ? false
         : task.position.equals(this.position) && task.preferStairs == this.preferStairs && task.dimension == this.dimension;
   }

   @Override
   public boolean isFinished() {
      return super.isFinished() && (this.dimension == null || this.dimension == WorldHelper.getCurrentDimension(this.controller));
   }

   @Override
   protected String toDebugString() {
      return "Getting to block " + this.position + (this.dimension != null ? " in dimension " + this.dimension : "");
   }

   @Override
   protected Goal newGoal(AltoClefController mod) {
      return new GoalBlock(this.position);
   }

   @Override
   protected void onWander(AltoClefController mod) {
      super.onWander(mod);
      mod.getBlockScanner().requestBlockUnreachable(this.position);
   }
}
