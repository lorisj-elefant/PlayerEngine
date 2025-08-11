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
import java.util.Arrays;
import java.util.HashSet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.ArrayUtils;

public class SearchChunkForBlockTask extends SearchChunksExploreTask {
   private final HashSet<Block> toSearchFor = new HashSet<>();

   public SearchChunkForBlockTask(Block... blocks) {
      this.toSearchFor.addAll(Arrays.asList(blocks));
   }

   @Override
   protected boolean isChunkWithinSearchSpace(AltoClefController mod, ChunkPos pos) {
      return mod.getChunkTracker().scanChunk(pos, block -> {return this.toSearchFor.contains(mod.getWorld().getBlockState(block).getBlock());});
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof SearchChunkForBlockTask blockTask
         ? Arrays.equals(blockTask.toSearchFor.toArray(Block[]::new), this.toSearchFor.toArray(Block[]::new))
         : false;
   }

   @Override
   protected String toDebugString() {
      return "Searching chunk for blocks " + ArrayUtils.toString(this.toSearchFor.toArray(Block[]::new));
   }
}
