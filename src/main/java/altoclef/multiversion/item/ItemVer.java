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

package altoclef.multiversion.item;

import altoclef.multiversion.FoodComponentWrapper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class ItemVer {
   public static FoodComponentWrapper getFoodComponent(Item item) {
      return FoodComponentWrapper.of(item.getFoodProperties());
   }

   public static boolean isFood(ItemStack stack) {
      return isFood(stack.getItem());
   }

   public static boolean hasCustomName(ItemStack stack) {
      return stack.hasCustomHoverName();
   }

   public static boolean isFood(Item item) {
      return item.isEdible();
   }

   private static boolean isSuitableFor(Item item, BlockState state) {
      return item.isCorrectToolForDrops(state);
   }

   private static Item RAW_GOLD() {
      return Items.RAW_GOLD;
   }

   private static Item RAW_IRON() {
      return Items.RAW_IRON;
   }
}
