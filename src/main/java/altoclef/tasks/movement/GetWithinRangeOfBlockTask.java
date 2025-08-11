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
import altoclef.tasksystem.Task;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import net.minecraft.core.BlockPos;

public class GetWithinRangeOfBlockTask extends CustomBaritoneGoalTask {
   public final BlockPos blockPos;
   public final int range;

   public GetWithinRangeOfBlockTask(BlockPos blockPos, int range) {
      this.blockPos = blockPos;
      this.range = range;
   }

   @Override
   protected Goal newGoal(AltoClefController mod) {
      return new GoalNear(this.blockPos, this.range);
   }

   @Override
   protected boolean isEqual(Task other) {
      return !(other instanceof GetWithinRangeOfBlockTask task) ? false : task.blockPos.equals(this.blockPos) && task.range == this.range;
   }

   @Override
   protected String toDebugString() {
      return "Getting within " + this.range + " blocks of " + this.blockPos.toShortString();
   }
}
