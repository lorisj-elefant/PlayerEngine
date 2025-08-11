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

package altoclef.multiversion.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class WorldVer {
   public static boolean isBiomeAtPos(Level world, ResourceKey<Biome> biome, BlockPos pos) {
      Holder<Biome> b = world.getBiome(pos);
      return b.is(biome);
   }

   public static boolean isBiome(Holder<Biome> biome1, ResourceKey<Biome> biome2) {
      return biome1.is(biome2);
   }

   public static int getBottomY(Level world) {
      return world.getMinBuildHeight();
   }

   public static int getTopY(Level world) {
      return world.getMaxBuildHeight();
   }

   private static boolean isOutOfHeightLimit(Level world, BlockPos pos) {
      return world.isOutsideBuildHeight(pos);
   }
}
