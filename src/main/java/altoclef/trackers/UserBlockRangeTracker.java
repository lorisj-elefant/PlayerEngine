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

import altoclef.util.helpers.BaritoneHelper;
import altoclef.util.helpers.ItemHelper;
import altoclef.util.helpers.WorldHelper;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class UserBlockRangeTracker extends Tracker {
   final int AVOID_BREAKING_RANGE = 16;
   final Block[] USER_INDICATOR_BLOCKS = Streams.concat(Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.BED))).toArray(Block[]::new);
   final Block[] USER_BLOCKS_TO_AVOID_BREAKING = Streams.concat(
         Arrays.asList(Blocks.COBBLESTONE).stream(), Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.LOG))
      )
      .toArray(Block[]::new);
   private final Set<BlockPos> dontBreakBlocks = new HashSet<>();

   public UserBlockRangeTracker(TrackerManager manager) {
      super(manager);
   }

   public boolean isNearUserTrackedBlock(BlockPos pos) {
      this.ensureUpdated();
      synchronized (BaritoneHelper.MINECRAFT_LOCK) {
         return this.dontBreakBlocks.contains(pos);
      }
   }

   @Override
   protected void updateState() {
      this.dontBreakBlocks.clear();
      List<BlockPos> userBlocks = this.mod.getBlockScanner().getKnownLocationsIncludeUnreachable(this.USER_INDICATOR_BLOCKS);
      Set<Block> userIndicatorBlocks = new HashSet<>(Arrays.asList(this.USER_INDICATOR_BLOCKS));
      Set<Block> userBlocksToAvoidMining = new HashSet<>(Arrays.asList(this.USER_BLOCKS_TO_AVOID_BREAKING));
      userBlocks.removeIf(bpos -> {
         Block bx = this.mod.getWorld().getBlockState(bpos).getBlock();
         return !userIndicatorBlocks.contains(bx);
      });

      for (BlockPos userBlockPos : userBlocks) {
         BlockPos min = userBlockPos.offset(-16, -16, -16);
         BlockPos max = userBlockPos.offset(16, 16, 16);

         for (BlockPos possible : WorldHelper.scanRegion(min, max)) {
            Block b = this.mod.getWorld().getBlockState(possible).getBlock();
            if (userBlocksToAvoidMining.contains(b)) {
               this.dontBreakBlocks.add(possible);
            }
         }
      }
   }

   @Override
   protected void reset() {
      this.dontBreakBlocks.clear();
   }
}
