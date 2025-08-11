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

import altoclef.AltoClefController;
import altoclef.util.helpers.BaritoneHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.pathing.goals.GoalYLevel;
import java.util.List;
import net.minecraft.world.entity.Entity;

public abstract class GoalRunAwayFromEntities implements Goal {
   private final AltoClefController mod;
   private final double distance;
   private final boolean xzOnly;
   private final double penaltyFactor;

   public GoalRunAwayFromEntities(AltoClefController mod, double distance, boolean xzOnly, double penaltyFactor) {
      this.mod = mod;
      this.distance = distance;
      this.xzOnly = xzOnly;
      this.penaltyFactor = penaltyFactor;
   }

   @Override
   public boolean isInGoal(int x, int y, int z) {
      List<Entity> entities = this.getEntities(this.mod);
      synchronized (BaritoneHelper.MINECRAFT_LOCK) {
         if (!entities.isEmpty()) {
            for (Entity entity : entities) {
               if (entity != null && entity.isAlive()) {
                  double sqDistance;
                  if (this.xzOnly) {
                     sqDistance = entity.position().subtract(x, y, z).multiply(1.0, 0.0, 1.0).lengthSqr();
                  } else {
                     sqDistance = entity.distanceToSqr(x, y, z);
                  }

                  if (sqDistance < this.distance * this.distance) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   @Override
   public double heuristic(int x, int y, int z) {
      double costSum = 0.0;
      List<Entity> entities = this.getEntities(this.mod);
      synchronized (BaritoneHelper.MINECRAFT_LOCK) {
         int max = 10;
         int counter = 0;
         if (!entities.isEmpty()) {
            for (Entity entity : entities) {
               counter++;
               if (entity != null && entity.isAlive()) {
                  double cost = this.getCostOfEntity(entity, x, y, z);
                  if (cost != 0.0) {
                     costSum += 1.0 / cost;
                  } else {
                     costSum += 1000.0;
                  }

                  if (counter >= max) {
                     break;
                  }
               }
            }
         }

         if (counter > 0) {
            costSum /= counter;
         }

         return costSum * this.penaltyFactor;
      }
   }

   protected abstract List<Entity> getEntities(AltoClefController var1);

   protected double getCostOfEntity(Entity entity, int x, int y, int z) {
      double heuristic = 0.0;
      if (!this.xzOnly) {
         heuristic += GoalYLevel.calculate(entity.blockPosition().getY(), y);
      }

      return heuristic + GoalXZ.calculate(entity.blockPosition().getX() - x, entity.blockPosition().getZ() - z);
   }
}
