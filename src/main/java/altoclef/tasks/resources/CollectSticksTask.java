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
import altoclef.trackers.storage.ItemStorageTracker;
import altoclef.util.CraftingRecipe;
import altoclef.util.ItemTarget;
import altoclef.util.MiningRequirement;
import altoclef.util.RecipeTarget;
import altoclef.util.helpers.ItemHelper;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CollectSticksTask extends ResourceTask {
   private final int targetCount;

   public CollectSticksTask(int targetCount) {
      super(Items.STICK, targetCount);
      this.targetCount = targetCount;
   }

   @Override
   protected boolean shouldAvoidPickingUp(AltoClefController mod) {
      return false;
   }

   @Override
   protected void onResourceStart(AltoClefController mod) {
      mod.getBehaviour().push();
   }

   @Override
   protected double getPickupRange(AltoClefController mod) {
      ItemStorageTracker storage = mod.getItemStorage();
      return storage.getItemCount(ItemHelper.PLANKS) * 4 + storage.getItemCount(ItemHelper.LOG) * 4 * 4 > this.targetCount ? 10.0 : 35.0;
   }

   @Override
   protected Task onResourceTick(AltoClefController mod) {
      if (mod.getItemStorage().getItemCount(Items.BAMBOO) >= 2) {
         return new CraftInInventoryTask(
            new RecipeTarget(
               Items.STICK,
               Math.min(mod.getItemStorage().getItemCount(Items.BAMBOO) / 2, this.targetCount),
               CraftingRecipe.newShapedRecipe("sticks", new ItemTarget[]{new ItemTarget("bamboo"), null, new ItemTarget("bamboo"), null}, 1)
            )
         );
      } else {
         Optional<BlockPos> nearestBush = mod.getBlockScanner().getNearestBlock(Blocks.DEAD_BUSH);
         return (Task)(nearestBush.isPresent() && nearestBush.get().closerToCenterThan(mod.getPlayer().position(), 20.0)
            ? new MineAndCollectTask(Items.DEAD_BUSH, 1, new Block[]{Blocks.DEAD_BUSH}, MiningRequirement.HAND)
            : new CraftInInventoryTask(
               new RecipeTarget(
                  Items.STICK,
                  this.targetCount,
                  CraftingRecipe.newShapedRecipe("sticks", new ItemTarget[]{new ItemTarget("planks"), null, new ItemTarget("planks"), null}, 4)
               )
            ));
      }
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
      mod.getBehaviour().pop();
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectSticksTask;
   }

   @Override
   protected String toDebugStringName() {
      return "Crafting " + this.targetCount + " sticks";
   }
}
