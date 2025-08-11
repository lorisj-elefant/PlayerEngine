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
import altoclef.multiversion.world.WorldVer;
import altoclef.tasksystem.Task;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;

public class SearchWithinBiomeTask extends SearchChunksExploreTask {
   private final ResourceKey<Biome> toSearch;

   public SearchWithinBiomeTask(ResourceKey<Biome> toSearch) {
      this.toSearch = toSearch;
   }

   @Override
   protected boolean isChunkWithinSearchSpace(AltoClefController mod, ChunkPos pos) {
      return WorldVer.isBiomeAtPos(mod.getWorld(), this.toSearch, pos.getWorldPosition().offset(1, 1, 1));
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof SearchWithinBiomeTask task ? task.toSearch == this.toSearch : false;
   }

   @Override
   protected String toDebugString() {
      return "Searching for+within biome: " + this.toSearch;
   }
}
