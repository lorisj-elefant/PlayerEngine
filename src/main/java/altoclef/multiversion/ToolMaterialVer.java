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

package altoclef.multiversion;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;

public class ToolMaterialVer {
   public static int getMiningLevel(TieredItem item) {
      return getMiningLevel(item.getTier());
   }

   public static int getMiningLevel(Tier material) {
      if (material.equals(Tiers.WOOD) || material.equals(Tiers.GOLD)) {
         return 0;
      } else if (material.equals(Tiers.STONE)) {
         return 1;
      } else if (material.equals(Tiers.IRON)) {
         return 2;
      } else if (material.equals(Tiers.DIAMOND)) {
         return 3;
      } else if (material.equals(Tiers.NETHERITE)) {
         return 4;
      } else {
         throw new IllegalStateException("Unexpected value: " + material);
      }
   }
}
