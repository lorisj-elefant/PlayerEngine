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

package altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators;

public abstract class DistancePriorityCalculator {
   public final int minCount;
   public final int maxCount;
   protected boolean minCountSatisfied = false;
   protected boolean maxCountSatisfied = false;

   public DistancePriorityCalculator(int minCount, int maxCount) {
      this.minCount = minCount;
      this.maxCount = maxCount;
   }

   public void update(int count) {
      if (count >= this.minCount) {
         this.minCountSatisfied = true;
      }

      if (count >= this.maxCount) {
         this.maxCountSatisfied = true;
      }
   }

   public double getPriority(double distance) {
      return !Double.isInfinite(distance) && distance != 2.147483647E9 && !this.maxCountSatisfied ? this.calculatePriority(distance) : Double.NEGATIVE_INFINITY;
   }

   abstract double calculatePriority(double var1);
}
