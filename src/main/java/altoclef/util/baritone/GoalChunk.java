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
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.world.level.ChunkPos;

public class GoalChunk implements Goal {
   private final ChunkPos pos;

   public GoalChunk(ChunkPos pos) {
      this.pos = pos;
   }

   @Override
   public boolean isInGoal(int x, int y, int z) {
      return this.pos.getMinBlockX() <= x && x <= this.pos.getMaxBlockX() && this.pos.getMinBlockZ() <= z && z <= this.pos.getMaxBlockZ();
   }

   @Override
   public double heuristic(int x, int y, int z) {
      double cx = (this.pos.getMinBlockX() + this.pos.getMaxBlockX()) / 2.0;
      double cz = (this.pos.getMinBlockZ() + this.pos.getMaxBlockZ()) / 2.0;
      return GoalXZ.calculate(cx - x, cz - z);
   }
}
