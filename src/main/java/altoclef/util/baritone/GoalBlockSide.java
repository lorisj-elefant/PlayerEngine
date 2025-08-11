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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class GoalBlockSide implements Goal {
   private final BlockPos block;
   private final Direction direction;
   private final double buffer;

   public GoalBlockSide(BlockPos block, Direction direction, double bufferDistance) {
      this.block = block;
      this.direction = direction;
      this.buffer = bufferDistance;
   }

   public GoalBlockSide(BlockPos block, Direction direction) {
      this(block, direction, 1.0);
   }

   @Override
   public boolean isInGoal(int x, int y, int z) {
      return this.getDistanceInRightDirection(x, y, z) > 0.0;
   }

   @Override
   public double heuristic(int x, int y, int z) {
      return Math.min(this.getDistanceInRightDirection(x, y, z), 0.0);
   }

   private double getDistanceInRightDirection(int x, int y, int z) {
      Vec3 delta = new Vec3(x, y, z).subtract(this.block.getX(), this.block.getY(), this.block.getZ());
      Vec3i dir = this.direction.getNormal();
      double dot = new Vec3(dir.getX(), dir.getY(), dir.getZ()).dot(delta);
      return dot - this.buffer;
   }
}
