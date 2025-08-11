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

package altoclef.trackers;

import altoclef.AltoClefController;
import altoclef.Debug;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SimpleChunkTracker {
   private final AltoClefController mod;
   private final Set<ChunkPos> loaded = new HashSet<>();

   public SimpleChunkTracker(AltoClefController mod) {
      this.mod = mod;
      MinecraftForge.EVENT_BUS.register(this);
   }

   @SubscribeEvent
   public void onLoad(ChunkEvent.Load event) {
      this.loaded.add(event.getChunk().getPos());
   }

   @SubscribeEvent
   public void onUnload(ChunkEvent.Unload event) {
      this.loaded.remove(event.getChunk().getPos());
   }

   public boolean isChunkLoaded(ChunkPos pos) {
      return !(this.mod.getWorld().getChunk(pos.x, pos.z) instanceof EmptyLevelChunk);
   }

   public boolean isChunkLoaded(BlockPos pos) {
      return this.isChunkLoaded(new ChunkPos(pos));
   }

   public List<ChunkPos> getLoadedChunks() {
      List<ChunkPos> result = new ArrayList<>(this.loaded);
      return result.stream().filter(this::isChunkLoaded).distinct().collect(Collectors.toList());
   }

   public boolean scanChunk(ChunkPos chunk, Predicate<BlockPos> onBlockStop) {
      if (!this.isChunkLoaded(chunk)) {
         return false;
      } else {
         int bottomY = this.mod.getWorld().getMinBuildHeight();
         int topY = this.mod.getWorld().getMaxBuildHeight();

         for (int xx = chunk.getMinBlockX(); xx <= chunk.getMaxBlockX(); xx++) {
            for (int yy = bottomY; yy <= topY; yy++) {
               for (int zz = chunk.getMinBlockZ(); zz <= chunk.getMaxBlockZ(); zz++) {
                  if (onBlockStop.test(new BlockPos(xx, yy, zz))) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   public void scanChunk(ChunkPos chunk, Consumer<BlockPos> onBlock) {
      this.scanChunk(chunk, block -> {
         onBlock.accept(block);
         return false;
      });
   }

   public void reset(AltoClefController mod) {
      Debug.logInternal("CHUNKS RESET");
      this.loaded.clear();
   }
}
