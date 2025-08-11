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

public class CollectFenceGateTask extends CraftWithMatchingPlanksTask {
   public CollectFenceGateTask(Item[] targets, ItemTarget planks, int count) {
      super(targets, woodItems -> woodItems.fenceGate, createRecipe(planks), new boolean[]{false, true, false, false, true, false, false, false, false}, count);
   }

   public CollectFenceGateTask(Item target, String plankCatalogueName, int count) {
      this(new Item[]{target}, new ItemTarget(plankCatalogueName, 1), count);
   }

   public CollectFenceGateTask(int count) {
      this(ItemHelper.WOOD_FENCE_GATE, TaskCatalogue.getItemTarget("planks", 1), count);
   }

   private static CraftingRecipe createRecipe(ItemTarget planks) {
      ItemTarget s = TaskCatalogue.getItemTarget("stick", 1);
      return CraftingRecipe.newShapedRecipe(new ItemTarget[]{s, planks, s, s, planks, s, null, null, null}, 1);
   }
}
