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
import altoclef.tasks.resources.CraftWithMatchingStrippedLogsTask;
import altoclef.util.CraftingRecipe;
import altoclef.util.ItemTarget;
import altoclef.util.helpers.ItemHelper;
import net.minecraft.world.item.Item;

public class CollectHangingSignTask extends CraftWithMatchingStrippedLogsTask {
   public CollectHangingSignTask(Item[] targets, ItemTarget strippedLogs, int count) {
      super(
         targets, woodItems -> woodItems.hangingSign, createRecipe(strippedLogs), new boolean[]{false, false, false, true, true, true, true, true, true}, count
      );
   }

   public CollectHangingSignTask(Item target, String strippedLogCatalogueName, int count) {
      this(new Item[]{target}, new ItemTarget(strippedLogCatalogueName, 1), count);
   }

   public CollectHangingSignTask(int count) {
      this(ItemHelper.WOOD_HANGING_SIGN, TaskCatalogue.getItemTarget("stripped_logs", 1), count);
   }

   private static CraftingRecipe createRecipe(ItemTarget strippedLogs) {
      ItemTarget chain = TaskCatalogue.getItemTarget("chain", 1);
      return CraftingRecipe.newShapedRecipe(
         new ItemTarget[]{chain, null, chain, strippedLogs, strippedLogs, strippedLogs, strippedLogs, strippedLogs, strippedLogs}, 6
      );
   }
}
