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

public class CollectWoodenStairsTask extends CraftWithMatchingPlanksTask {
   public CollectWoodenStairsTask(Item[] targets, ItemTarget planks, int count) {
      super(targets, woodItems -> woodItems.stairs, createRecipe(planks), new boolean[]{true, false, false, true, true, false, true, true, true}, count);
   }

   public CollectWoodenStairsTask(Item target, String plankCatalogueName, int count) {
      this(new Item[]{target}, new ItemTarget(plankCatalogueName, 1), count);
   }

   public CollectWoodenStairsTask(int count) {
      this(ItemHelper.WOOD_STAIRS, TaskCatalogue.getItemTarget("planks", 1), count);
   }

   private static CraftingRecipe createRecipe(ItemTarget planks) {
      ItemTarget o = null;
      return CraftingRecipe.newShapedRecipe(new ItemTarget[]{planks, o, o, planks, planks, o, planks, planks, planks}, 4);
   }
}
