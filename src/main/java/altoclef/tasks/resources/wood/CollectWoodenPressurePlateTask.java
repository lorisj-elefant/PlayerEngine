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

package altoclef.tasks.resources.wood;

import altoclef.TaskCatalogue;
import altoclef.tasks.resources.CraftWithMatchingPlanksTask;
import altoclef.util.CraftingRecipe;
import altoclef.util.ItemTarget;
import altoclef.util.helpers.ItemHelper;
import net.minecraft.world.item.Item;

public class CollectWoodenPressurePlateTask extends CraftWithMatchingPlanksTask {
   public CollectWoodenPressurePlateTask(Item[] targets, ItemTarget planks, int count) {
      super(targets, woodItems -> woodItems.pressurePlate, createRecipe(planks), new boolean[]{true, true, false, false}, count);
   }

   public CollectWoodenPressurePlateTask(Item target, String plankCatalogueName, int count) {
      this(new Item[]{target}, new ItemTarget(plankCatalogueName, 1), count);
   }

   public CollectWoodenPressurePlateTask(int count) {
      this(ItemHelper.WOOD_PRESSURE_PLATE, TaskCatalogue.getItemTarget("planks", 1), count);
   }

   private static CraftingRecipe createRecipe(ItemTarget planks) {
      return CraftingRecipe.newShapedRecipe(new ItemTarget[]{planks, planks, null, null}, 1);
   }
}
