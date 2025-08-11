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
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.core.BlockPos;

public class GetToXZTask extends CustomBaritoneGoalTask {
   private final int x;
   private final int z;
   private final Dimension dimension;

   public GetToXZTask(int x, int z) {
      this(x, z, null);
   }

   public GetToXZTask(int x, int z, Dimension dimension) {
      this.x = x;
      this.z = z;
      this.dimension = dimension;
   }

   @Override
   protected Task onTick() {
      return (Task)(this.dimension != null && WorldHelper.getCurrentDimension(this.controller) != this.dimension
         ? new DefaultGoToDimensionTask(this.dimension)
         : super.onTick());
   }

   @Override
   protected Goal newGoal(AltoClefController mod) {
      return new GoalXZ(this.x, this.z);
   }

   @Override
   protected boolean isEqual(Task other) {
      return !(other instanceof GetToXZTask task) ? false : task.x == this.x && task.z == this.z && task.dimension == this.dimension;
   }

   @Override
   public boolean isFinished() {
      BlockPos cur = this.controller.getPlayer().blockPosition();
      return cur.getX() == this.x && cur.getZ() == this.z && (this.dimension == null || this.dimension == WorldHelper.getCurrentDimension(this.controller));
   }

   @Override
   protected String toDebugString() {
      return "Getting to (" + this.x + "," + this.z + ")" + (this.dimension != null ? " in dimension " + this.dimension : "");
   }
}
