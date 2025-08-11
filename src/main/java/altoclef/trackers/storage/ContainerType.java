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

package altoclef.trackers.storage;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public enum ContainerType {
   CHEST,
   ENDER_CHEST,
   SHULKER,
   FURNACE,
   BREWING,
   MISC,
   EMPTY;

   public static ContainerType getFromBlock(Block block) {
      if (block instanceof ChestBlock) {
         return CHEST;
      } else if (block instanceof AbstractFurnaceBlock) {
         return FURNACE;
      } else if (block.equals(Blocks.ENDER_CHEST)) {
         return ENDER_CHEST;
      } else if (block instanceof ShulkerBoxBlock) {
         return SHULKER;
      } else if (block instanceof BrewingStandBlock) {
         return BREWING;
      } else {
         return !(block instanceof BarrelBlock) && !(block instanceof DispenserBlock) && !(block instanceof HopperBlock) ? EMPTY : MISC;
      }
   }
}
