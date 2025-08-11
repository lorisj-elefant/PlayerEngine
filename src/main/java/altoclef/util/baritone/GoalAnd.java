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

package altoclef.util.baritone;

import baritone.api.pathing.goals.Goal;
import java.util.Arrays;

public class GoalAnd implements Goal {
   private final Goal[] goals;

   public GoalAnd(Goal... goals) {
      this.goals = goals;
   }

   @Override
   public boolean isInGoal(int x, int y, int z) {
      Goal[] var4 = this.goals;
      int var5 = var4.length;

      for (Goal goal : var4) {
         if (!goal.isInGoal(x, y, z)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public double heuristic(int x, int y, int z) {
      double sum = 0.0;
      if (this.goals != null) {
         for (Goal goal : this.goals) {
            sum += goal.heuristic(x, y, z);
         }
      }

      return sum;
   }

   @Override
   public String toString() {
      return "GoalAnd" + Arrays.toString((Object[])this.goals);
   }

   public Goal[] goals() {
      return this.goals;
   }
}
