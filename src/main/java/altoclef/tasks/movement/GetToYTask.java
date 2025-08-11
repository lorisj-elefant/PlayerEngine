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
import altoclef.util.Dimension;
import altoclef.util.helpers.WorldHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalYLevel;

public class GetToYTask extends CustomBaritoneGoalTask {
   private final int yLevel;
   private final Dimension dimension;

   public GetToYTask(int ylevel, Dimension dimension) {
      this.yLevel = ylevel;
      this.dimension = dimension;
   }

   public GetToYTask(int ylevel) {
      this(ylevel, null);
   }

   @Override
   protected Task onTick() {
      return (Task)(this.dimension != null && WorldHelper.getCurrentDimension(this.controller) != this.dimension
         ? new DefaultGoToDimensionTask(this.dimension)
         : super.onTick());
   }

   @Override
   protected Goal newGoal(AltoClefController mod) {
      return new GoalYLevel(this.yLevel);
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof GetToYTask task ? task.yLevel == this.yLevel : false;
   }

   @Override
   protected String toDebugString() {
      return "Going to y=" + this.yLevel + (this.dimension != null ? "in dimension" + this.dimension : "");
   }
}
