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
import altoclef.util.Dimension;
import altoclef.util.ItemTarget;
import altoclef.util.MiningRequirement;
import altoclef.util.RecipeTarget;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CollectCoarseDirtTask extends ResourceTask {
   private static final float CLOSE_ENOUGH_COARSE_DIRT = 128.0F;
   private final int count;

   public CollectCoarseDirtTask(int targetCount) {
      super(Items.COARSE_DIRT, targetCount);
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
      double c = Math.ceil((this.count - mod.getItemStorage().getItemCount(Items.COARSE_DIRT)) / 4.0) * 2.0;
      Optional<BlockPos> closest = mod.getBlockScanner().getNearestBlock(Blocks.COARSE_DIRT);
      if ((mod.getItemStorage().getItemCount(Items.DIRT) < c || mod.getItemStorage().getItemCount(Items.GRAVEL) < c)
         && closest.isPresent()
         && closest.get().closerToCenterThan(mod.getPlayer().position(), 128.0)) {
         return new MineAndCollectTask(new ItemTarget(Items.COARSE_DIRT), new Block[]{Blocks.COARSE_DIRT}, MiningRequirement.HAND)
            .forceDimension(Dimension.OVERWORLD);
      } else {
         int target = this.count;
         ItemTarget d = new ItemTarget(Items.DIRT, 1);
         ItemTarget g = new ItemTarget(Items.GRAVEL, 1);
         return new CraftInInventoryTask(
            new RecipeTarget(Items.COARSE_DIRT, target, CraftingRecipe.newShapedRecipe("coarse_dirt", new ItemTarget[]{d, g, g, d}, 4))
         );
      }
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectCoarseDirtTask;
   }

   @Override
   protected String toDebugStringName() {
      return "Collecting " + this.count + " Coarse Dirt.";
   }
}
