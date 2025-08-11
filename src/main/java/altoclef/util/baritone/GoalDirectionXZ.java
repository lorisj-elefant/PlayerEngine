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

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import net.minecraft.world.phys.Vec3;

public class GoalDirectionXZ implements Goal {
   private final double originx;
   private final double originz;
   private final double dirx;
   private final double dirz;
   private final double sidePenalty;

   public GoalDirectionXZ(Vec3 origin, Vec3 offset, double sidePenalty) {
      this.originx = origin.x();
      this.originz = origin.z();
      offset = offset.multiply(1.0, 0.0, 1.0);
      offset = offset.normalize();
      this.dirx = offset.x;
      this.dirz = offset.z;
      if (this.dirx == 0.0 && this.dirz == 0.0) {
         throw new IllegalArgumentException(String.valueOf(offset));
      } else {
         this.sidePenalty = sidePenalty;
      }
   }

   private static String maybeCensor(double value) {
      return BaritoneAPI.getGlobalSettings().censorCoordinates.get() ? "<censored>" : Double.toString(value);
   }

   @Override
   public boolean isInGoal(int x, int y, int z) {
      return false;
   }

   @Override
   public double heuristic(int x, int y, int z) {
      double dx = x - this.originx;
      double dz = z - this.originz;
      double correctDistance = dx * this.dirx + dz * this.dirz;
      double px = this.dirx * correctDistance;
      double pz = this.dirz * correctDistance;
      double perpendicularDistance = (dx - px) * (dx - px) + (dz - pz) * (dz - pz);
      return -correctDistance * BaritoneAPI.getGlobalSettings().costHeuristic.get() + perpendicularDistance * this.sidePenalty;
   }

   @Override
   public String toString() {
      return String.format(
         "GoalDirection{x=%s, z=%s, dx=%s, dz=%s}", maybeCensor(this.originx), maybeCensor(this.originz), maybeCensor(this.dirx), maybeCensor(this.dirz)
      );
   }
}
