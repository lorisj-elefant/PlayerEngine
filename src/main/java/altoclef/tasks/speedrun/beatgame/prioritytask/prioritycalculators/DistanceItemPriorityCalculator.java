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

public class DistanceItemPriorityCalculator extends DistancePriorityCalculator {
   private final double multiplier;
   private final double unneededMultiplier;
   private final double unneededDistanceThreshold;

   public DistanceItemPriorityCalculator(double multiplier, double unneededMultiplier, double unneededDistanceThreshold, int minCount, int maxCount) {
      super(minCount, maxCount);
      this.multiplier = multiplier;
      this.unneededMultiplier = unneededMultiplier;
      this.unneededDistanceThreshold = unneededDistanceThreshold;
   }

   @Override
   protected double calculatePriority(double distance) {
      double priority = 1.0 / distance;
      if (this.minCountSatisfied) {
         return distance < this.unneededDistanceThreshold ? priority * this.unneededMultiplier : Double.NEGATIVE_INFINITY;
      } else {
         return priority * this.multiplier;
      }
   }
}
