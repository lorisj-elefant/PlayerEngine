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

import altoclef.AltoClefController;
import altoclef.TaskCatalogue;
import altoclef.tasks.ResourceTask;
import altoclef.tasksystem.Task;
import altoclef.util.CraftingRecipe;
import altoclef.util.ItemTarget;
import altoclef.util.MiningRequirement;
import altoclef.util.helpers.ItemHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CollectBedTask extends CraftWithMatchingWoolTask {
   public static final Block[] BEDS = ItemHelper.itemsToBlocks(ItemHelper.BED);
   private final ItemTarget visualBedTarget;

   public CollectBedTask(Item[] beds, ItemTarget wool, int count) {
      super(
         new ItemTarget(beds, count),
         colorfulItems -> colorfulItems.wool,
         colorfulItems -> colorfulItems.bed,
         createBedRecipe(wool),
         new boolean[]{true, true, true, false, false, false, false, false, false}
      );
      this.visualBedTarget = new ItemTarget(beds, count);
   }

   public CollectBedTask(Item bed, String woolCatalogueName, int count) {
      this(new Item[]{bed}, new ItemTarget(woolCatalogueName, 1), count);
   }

   public CollectBedTask(int count) {
      this(ItemHelper.BED, TaskCatalogue.getItemTarget("wool", 1), count);
   }

   private static CraftingRecipe createBedRecipe(ItemTarget wool) {
      ItemTarget p = TaskCatalogue.getItemTarget("planks", 1);
      return CraftingRecipe.newShapedRecipe(new ItemTarget[]{wool, wool, wool, p, p, p, null, null, null}, 1);
   }

   @Override
   protected boolean shouldAvoidPickingUp(AltoClefController mod) {
      return false;
   }

   @Override
   protected Task onResourceTick(AltoClefController mod) {
      return (Task)(mod.getBlockScanner().anyFound(BEDS)
         ? new MineAndCollectTask(new ItemTarget(ItemHelper.BED, 1), BEDS, MiningRequirement.HAND)
         : super.onResourceTick(mod));
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectBedTask task ? task.visualBedTarget.equals(this.visualBedTarget) : false;
   }

   @Override
   protected String toDebugStringName() {
      return "Crafting bed: " + this.visualBedTarget;
   }
}
