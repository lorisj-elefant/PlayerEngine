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

package altoclef.util.helpers;

import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.world.phys.Vec3;

public class BaritoneHelper {
   public static final Object MINECRAFT_LOCK = new Object();

   public static double calculateGenericHeuristic(Vec3 start, Vec3 target) {
      return calculateGenericHeuristic(start.x, start.y, start.z, target.x, target.y, target.z);
   }

   public static double calculateGenericHeuristic(double xStart, double yStart, double zStart, double xTarget, double yTarget, double zTarget) {
      double xDiff = xTarget - xStart;
      int yDiff = (int)yTarget - (int)yStart;
      double zDiff = zTarget - zStart;
      return GoalBlock.calculate(xDiff, yDiff < 0 ? yDiff - 1 : yDiff, zDiff);
   }
}
