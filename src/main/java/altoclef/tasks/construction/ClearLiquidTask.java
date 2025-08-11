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

package altoclef.tasks.construction;

import altoclef.tasks.InteractWithBlockTask;
import altoclef.tasksystem.Task;
import altoclef.util.ItemTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext.Fluid;

public class ClearLiquidTask extends Task {
   private final BlockPos liquidPos;

   public ClearLiquidTask(BlockPos liquidPos) {
      this.liquidPos = liquidPos;
   }

   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      if (this.controller.getItemStorage().hasItem(Items.BUCKET)) {
         this.controller.getBehaviour().setRayTracingFluidHandling(Fluid.SOURCE_ONLY);
         return new InteractWithBlockTask(new ItemTarget(Items.BUCKET, 1), this.liquidPos, false);
      } else {
         return new PlaceStructureBlockTask(this.liquidPos);
      }
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   public boolean isFinished() {
      return this.controller.getChunkTracker().isChunkLoaded(this.liquidPos)
         ? this.controller.getWorld().getBlockState(this.liquidPos).getFluidState().isEmpty()
         : false;
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof ClearLiquidTask task ? task.liquidPos.equals(this.liquidPos) : false;
   }

   @Override
   protected String toDebugString() {
      return "Clear liquid at " + this.liquidPos;
   }
}
