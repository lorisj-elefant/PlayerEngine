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
import altoclef.tasks.ResourceTask;
import altoclef.tasks.container.CraftInTableTask;
import altoclef.tasksystem.Task;
import altoclef.util.CraftingRecipe;
import altoclef.util.ItemTarget;
import altoclef.util.MiningRequirement;
import altoclef.util.RecipeTarget;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CollectHayBlockTask extends ResourceTask {
   private final int count;

   public CollectHayBlockTask(int count) {
      super(Items.HAY_BLOCK, count);
      this.count = count;
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
      if (mod.getBlockScanner().anyFound(Blocks.HAY_BLOCK)) {
         return new MineAndCollectTask(Items.HAY_BLOCK, this.count, new Block[]{Blocks.HAY_BLOCK}, MiningRequirement.HAND);
      } else {
         ItemTarget w = new ItemTarget(Items.WHEAT, 1);
         return new CraftInTableTask(
            new RecipeTarget(Items.HAY_BLOCK, this.count, CraftingRecipe.newShapedRecipe("hay_block", new ItemTarget[]{w, w, w, w, w, w, w, w, w}, 1))
         );
      }
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectHayBlockTask;
   }

   @Override
   protected String toDebugStringName() {
      return "Collecting " + this.count + " hay blocks.";
   }
}
