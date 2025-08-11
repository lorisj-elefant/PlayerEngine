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

package altoclef.tasks;

import altoclef.AltoClefController;
import altoclef.tasks.resources.CollectBucketLiquidTask;
import altoclef.tasksystem.Task;
import altoclef.util.ItemTarget;
import net.minecraft.world.item.Items;

public class GetRidOfExtraWaterBucketTask extends Task {
   private boolean needsPickup = false;

   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      AltoClefController mod = this.controller;
      if (mod.getItemStorage().getItemCount(Items.WATER_BUCKET) != 0 && !this.needsPickup) {
         return new InteractWithBlockTask(new ItemTarget(Items.WATER_BUCKET, 1), mod.getPlayer().blockPosition().below(), false);
      } else {
         this.needsPickup = true;
         return mod.getItemStorage().getItemCount(Items.WATER_BUCKET) < 1 ? new CollectBucketLiquidTask.CollectWaterBucketTask(1) : null;
      }
   }

   @Override
   public boolean isFinished() {
      return this.controller.getItemStorage().getItemCount(Items.WATER_BUCKET) == 1 && this.needsPickup;
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof GetRidOfExtraWaterBucketTask;
   }

   @Override
   protected String toDebugString() {
      return null;
   }
}
