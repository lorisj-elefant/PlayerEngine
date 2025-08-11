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
import baritone.api.pathing.goals.GoalRunAway;
import java.util.Arrays;
import net.minecraft.core.BlockPos;

public class RunAwayFromPositionTask extends CustomBaritoneGoalTask {
   private final BlockPos[] dangerBlocks;
   private final double distance;
   private final Integer maintainY;

   public RunAwayFromPositionTask(double distance, BlockPos... toRunAwayFrom) {
      this(distance, null, toRunAwayFrom);
   }

   public RunAwayFromPositionTask(double distance, Integer maintainY, BlockPos... toRunAwayFrom) {
      this.distance = distance;
      this.dangerBlocks = toRunAwayFrom;
      this.maintainY = maintainY;
   }

   @Override
   protected Goal newGoal(AltoClefController mod) {
      return new GoalRunAway(this.distance, this.maintainY, this.dangerBlocks);
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof RunAwayFromPositionTask task ? Arrays.equals((Object[])task.dangerBlocks, (Object[])this.dangerBlocks) : false;
   }

   @Override
   protected String toDebugString() {
      return "Running away from " + Arrays.toString((Object[])this.dangerBlocks);
   }
}
