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

package altoclef.util;

import altoclef.Debug;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public enum MiningRequirement implements Comparable<MiningRequirement> {
   HAND(Items.AIR),
   WOOD(Items.WOODEN_PICKAXE),
   STONE(Items.STONE_PICKAXE),
   IRON(Items.IRON_PICKAXE),
   DIAMOND(Items.DIAMOND_PICKAXE);

   private final Item minPickaxe;

   private MiningRequirement(Item minPickaxe) {
      this.minPickaxe = minPickaxe;
   }

   public static MiningRequirement getMinimumRequirementForBlock(Block block) {
      if (block.defaultBlockState().requiresCorrectToolForDrops()) {
         for (MiningRequirement req : values()) {
            if (req != HAND) {
               Item pick = req.getMinimumPickaxe();
               if (pick.isCorrectToolForDrops(block.defaultBlockState())) {
                  return req;
               }
            }
         }

         Debug.logWarning(
            "Failed to find ANY effective tool against: " + block + ". I assume netherite is not required anywhere, so something else probably went wrong."
         );
         return DIAMOND;
      } else {
         return HAND;
      }
   }

   public Item getMinimumPickaxe() {
      return this.minPickaxe;
   }
}
