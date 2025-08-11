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
import altoclef.util.baritone.GoalDodgeProjectiles;
import baritone.api.pathing.goals.Goal;

public class DodgeProjectilesTask extends CustomBaritoneGoalTask {
   private final double distanceHorizontal;
   private final double distanceVertical;

   public DodgeProjectilesTask(double distanceHorizontal, double distanceVertical) {
      this.distanceHorizontal = distanceHorizontal;
      this.distanceVertical = distanceVertical;
   }

   @Override
   protected Task onTick() {
      if (this.cachedGoal != null) {
         GoalDodgeProjectiles var1 = (GoalDodgeProjectiles)this.cachedGoal;
      }

      return super.onTick();
   }

   @Override
   protected boolean isEqual(Task other) {
      if (other instanceof DodgeProjectilesTask task) {
         return Math.abs(task.distanceHorizontal - this.distanceHorizontal) > 1.0 ? false : !(Math.abs(task.distanceVertical - this.distanceVertical) > 1.0);
      } else {
         return false;
      }
   }

   @Override
   protected String toDebugString() {
      return "Dodge arrows at " + this.distanceHorizontal + " blocks away";
   }

   @Override
   protected Goal newGoal(AltoClefController mod) {
      return new GoalDodgeProjectiles(mod, this.distanceHorizontal, this.distanceVertical);
   }
}
