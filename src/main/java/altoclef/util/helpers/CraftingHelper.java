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

package altoclef.util.helpers;

import altoclef.AltoClefController;
import altoclef.util.CraftingRecipe;
import altoclef.util.ItemTarget;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CraftingHelper {
   public static boolean canCraftItemNow(AltoClefController mod, Item item) {
      List<ItemStack> inventoryItems = new ArrayList<>();

      for (ItemStack stack : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
         inventoryItems.add(new ItemStack(stack.getItem(), stack.getCount()));
      }

      for (CraftingRecipe recipe : mod.getCraftingRecipeTracker().getRecipeForItem(item)) {
         if (canCraftItemNow(mod, new ArrayList<>(inventoryItems), recipe, new HashSet<>())) {
            return true;
         }
      }

      return false;
   }

   private static boolean canCraftItemNow(AltoClefController mod, List<ItemStack> inventoryStacks, CraftingRecipe recipe, HashSet<Item> alreadyChecked) {
      Item recipeResult = mod.getCraftingRecipeTracker().getRecipeResult(recipe).getItem();
      if (alreadyChecked.contains(recipeResult)) {
         return false;
      } else {
         alreadyChecked.add(recipeResult);
         ItemTarget[] targets = recipe.getSlots();
         ItemTarget[] arrayOfItemTarget1 = targets;
         int i = targets.length;
         byte b = 0;

         label64:
         while (b < i) {
            ItemTarget itemTarget = arrayOfItemTarget1[b];
            if (itemTarget == ItemTarget.EMPTY) {
               b++;
            } else {
               for (Item item : itemTarget.getMatches()) {
                  for (ItemStack inventoryStack : inventoryStacks) {
                     if (inventoryStack.getItem() == item && inventoryStack.getCount() >= itemTarget.getTargetCount()) {
                        inventoryStack.setCount(inventoryStack.getCount() - itemTarget.getTargetCount());
                        continue label64;
                     }
                  }
               }

               for (Item item : itemTarget.getMatches()) {
                  if (mod.getCraftingRecipeTracker().hasRecipeForItem(item)) {
                     for (CraftingRecipe newRecipe : mod.getCraftingRecipeTracker().getRecipeForItem(item)) {
                        List<ItemStack> inventoryStacksCopy = new ArrayList<>(inventoryStacks);
                        if (canCraftItemNow(mod, inventoryStacksCopy, newRecipe, new HashSet<>(alreadyChecked))) {
                           inventoryStacks = inventoryStacksCopy;
                           ItemStack result = mod.getCraftingRecipeTracker().getRecipeResult(newRecipe);
                           result.setCount(result.getCount() - 1);
                           inventoryStacksCopy.add(result);
                           continue label64;
                        }
                     }
                  }
               }

               return false;
            }
         }

         return true;
      }
   }
}
