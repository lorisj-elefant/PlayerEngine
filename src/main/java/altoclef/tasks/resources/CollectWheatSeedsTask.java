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
import altoclef.tasksystem.Task;
import altoclef.util.MiningRequirement;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CollectWheatSeedsTask extends ResourceTask {
   private final int count;

   public CollectWheatSeedsTask(int count) {
      super(Items.WHEAT_SEEDS, count);
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
      return (Task)(mod.getBlockScanner().anyFound(Blocks.WHEAT)
         ? new CollectCropTask(Items.AIR, 999, Blocks.WHEAT, Items.WHEAT_SEEDS)
         : new MineAndCollectTask(Items.WHEAT_SEEDS, this.count, new Block[]{Blocks.GRASS, Blocks.TALL_GRASS}, MiningRequirement.HAND));
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectWheatSeedsTask;
   }

   @Override
   protected String toDebugStringName() {
      return "Collecting " + this.count + " wheat seeds.";
   }
}
