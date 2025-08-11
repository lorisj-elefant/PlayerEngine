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

import java.util.Objects;
import net.minecraft.world.item.Item;

public class RecipeTarget {
   private final CraftingRecipe recipe;
   private final Item item;
   private final int targetCount;

   public RecipeTarget(Item item, int targetCount, CraftingRecipe recipe) {
      this.item = item;
      this.targetCount = targetCount;
      this.recipe = recipe;
   }

   public CraftingRecipe getRecipe() {
      return this.recipe;
   }

   public Item getOutputItem() {
      return this.item;
   }

   public int getTargetCount() {
      return this.targetCount;
   }

   @Override
   public String toString() {
      return this.targetCount == 1 ? "Recipe{" + this.item + "}" : "Recipe{" + this.item + " x " + this.targetCount + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RecipeTarget that = (RecipeTarget)o;
         return this.targetCount == that.targetCount && this.recipe.equals(that.recipe) && Objects.equals(this.item, that.item);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.recipe, this.item);
   }
}
