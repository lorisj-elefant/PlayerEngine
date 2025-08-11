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
import altoclef.util.baritone.GoalChunk;
import altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.Goal;
import net.minecraft.world.level.ChunkPos;

public class GetToChunkTask extends CustomBaritoneGoalTask {
   private final ChunkPos pos;

   public GetToChunkTask(ChunkPos pos) {
      this.checker = new MovementProgressChecker();
      this.pos = pos;
   }

   @Override
   protected Goal newGoal(AltoClefController mod) {
      return new GoalChunk(this.pos);
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof GetToChunkTask task ? task.pos.equals(this.pos) : false;
   }

   @Override
   protected String toDebugString() {
      return "Get to chunk: " + this.pos.toString();
   }
}
