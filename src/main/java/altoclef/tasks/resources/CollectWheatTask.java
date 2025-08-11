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
import altoclef.tasks.CraftInInventoryTask;
import altoclef.tasks.ResourceTask;
import altoclef.tasksystem.Task;
import altoclef.util.CraftingRecipe;
import altoclef.util.ItemTarget;
import altoclef.util.MiningRequirement;
import altoclef.util.RecipeTarget;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CollectWheatTask extends ResourceTask {
   private final int count;

   public CollectWheatTask(int targetCount) {
      super(Items.WHEAT, targetCount);
      this.count = targetCount;
   }

   @Override
   protected boolean shouldAvoidPickingUp(AltoClefController mod) {
      return false;
   }

   @Override
   protected void onResourceStart(AltoClefController mod) {
   }

   @Override
   protected Task onResourceTick(AltoClefController mod) {
      int potentialCount = mod.getItemStorage().getItemCount(Items.WHEAT) + 9 * mod.getItemStorage().getItemCount(Items.HAY_BLOCK);
      if (potentialCount >= this.count) {
         this.setDebugState("Crafting wheat");
         return new CraftInInventoryTask(
            new RecipeTarget(
               Items.WHEAT, this.count, CraftingRecipe.newShapedRecipe("wheat", new ItemTarget[]{new ItemTarget(Items.HAY_BLOCK, 1), null, null, null}, 9)
            )
         );
      } else {
         return (Task)(!mod.getBlockScanner().anyFound(Blocks.HAY_BLOCK) && !mod.getEntityTracker().itemDropped(Items.HAY_BLOCK)
            ? new CollectCropTask(new ItemTarget(Items.WHEAT, this.count), new Block[]{Blocks.WHEAT}, Items.WHEAT_SEEDS)
            : new MineAndCollectTask(Items.HAY_BLOCK, 99999999, new Block[]{Blocks.HAY_BLOCK}, MiningRequirement.HAND));
      }
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectWheatTask;
   }

   @Override
   protected String toDebugStringName() {
      return "Collecting " + this.count + " wheat.";
   }
}
