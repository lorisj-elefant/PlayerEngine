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

package altoclef.tasks.examples;

import altoclef.AltoClefController;
import altoclef.TaskCatalogue;
import altoclef.tasks.construction.PlaceBlockTask;
import altoclef.tasks.movement.GetToBlockTask;
import altoclef.tasksystem.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class ExampleTask extends Task {
   private final int numberOfStonePickaxesToGrab;
   private final BlockPos whereToPlaceCobblestone;

   public ExampleTask(int numberOfStonePickaxesToGrab, BlockPos whereToPlaceCobblestone) {
      this.numberOfStonePickaxesToGrab = numberOfStonePickaxesToGrab;
      this.whereToPlaceCobblestone = whereToPlaceCobblestone;
   }

   @Override
   protected void onStart() {
      AltoClefController mod = this.controller;
      mod.getBehaviour().push();
      mod.getBehaviour().addProtectedItems(Items.COBBLESTONE);
   }

   @Override
   protected Task onTick() {
      AltoClefController mod = this.controller;
      if (mod.getItemStorage().getItemCount(Items.STONE_PICKAXE) < this.numberOfStonePickaxesToGrab) {
         return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, this.numberOfStonePickaxesToGrab);
      } else if (!mod.getItemStorage().hasItem(Items.COBBLESTONE)) {
         return TaskCatalogue.getItemTask(Items.COBBLESTONE, 1);
      } else if (mod.getChunkTracker().isChunkLoaded(this.whereToPlaceCobblestone)) {
         return mod.getWorld().getBlockState(this.whereToPlaceCobblestone).getBlock() != Blocks.COBBLESTONE
            ? new PlaceBlockTask(this.whereToPlaceCobblestone, Blocks.COBBLESTONE)
            : null;
      } else {
         return new GetToBlockTask(this.whereToPlaceCobblestone);
      }
   }

   @Override
   protected void onStop(Task interruptTask) {
      this.controller.getBehaviour().pop();
   }

   @Override
   public boolean isFinished() {
      AltoClefController mod = this.controller;
      return mod.getItemStorage().getItemCount(Items.STONE_PICKAXE) >= this.numberOfStonePickaxesToGrab
         && mod.getWorld().getBlockState(this.whereToPlaceCobblestone).getBlock() == Blocks.COBBLESTONE;
   }

   @Override
   protected boolean isEqual(Task other) {
      return !(other instanceof ExampleTask task)
         ? false
         : task.numberOfStonePickaxesToGrab == this.numberOfStonePickaxesToGrab && task.whereToPlaceCobblestone.equals(this.whereToPlaceCobblestone);
   }

   @Override
   protected String toDebugString() {
      return "Boofin";
   }
}
