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

package altoclef.tasks.resources;

import altoclef.util.CraftingRecipe;
import altoclef.util.ItemTarget;
import altoclef.util.helpers.ItemHelper;
import java.util.function.Function;
import net.minecraft.world.item.Item;

public abstract class CraftWithMatchingWoolTask extends CraftWithMatchingMaterialsTask {
   private final Function<ItemHelper.ColorfulItems, Item> getMajorityMaterial;
   private final Function<ItemHelper.ColorfulItems, Item> getTargetItem;

   public CraftWithMatchingWoolTask(
      ItemTarget target,
      Function<ItemHelper.ColorfulItems, Item> getMajorityMaterial,
      Function<ItemHelper.ColorfulItems, Item> getTargetItem,
      CraftingRecipe recipe,
      boolean[] sameMask
   ) {
      super(target, recipe, sameMask);
      this.getMajorityMaterial = getMajorityMaterial;
      this.getTargetItem = getTargetItem;
   }

   @Override
   protected Item getSpecificItemCorrespondingToMajorityResource(Item majority) {
      for (ItemHelper.ColorfulItems colorfulItem : ItemHelper.getColorfulItems()) {
         if (this.getMajorityMaterial.apply(colorfulItem) == majority) {
            return this.getTargetItem.apply(colorfulItem);
         }
      }

      return null;
   }
}
