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

package altoclef.util.progresscheck;

import net.minecraft.world.phys.Vec3;

public class DistanceProgressChecker implements IProgressChecker<Vec3> {
   private final IProgressChecker<Double> distanceChecker;
   private final boolean reduceDistance;
   private Vec3 start;
   private Vec3 prevPos;

   public DistanceProgressChecker(IProgressChecker<Double> distanceChecker, boolean reduceDistance) {
      this.distanceChecker = distanceChecker;
      this.reduceDistance = reduceDistance;
      if (reduceDistance) {
         this.distanceChecker.setProgress(Double.NEGATIVE_INFINITY);
      }

      this.reset();
   }

   public DistanceProgressChecker(double timeout, double minDistanceToMake, boolean reduceDistance) {
      this(new LinearProgressChecker(timeout, minDistanceToMake), reduceDistance);
   }

   public DistanceProgressChecker(double timeout, double minDistanceToMake) {
      this(timeout, minDistanceToMake, false);
   }

   public void setProgress(Vec3 position) {
      if (this.start == null) {
         this.start = position;
      } else {
         double delta = position.distanceTo(this.start);
         if (this.reduceDistance) {
            delta *= -1.0;
         }

         this.prevPos = position;
         this.distanceChecker.setProgress(delta);
      }
   }

   @Override
   public boolean failed() {
      return this.distanceChecker.failed();
   }

   @Override
   public void reset() {
      this.start = null;
      this.distanceChecker.setProgress(0.0);
      this.distanceChecker.reset();
   }
}
