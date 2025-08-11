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

package altoclef.tasks.movement;

import altoclef.AltoClefController;
import altoclef.tasksystem.Task;
import altoclef.util.helpers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class GoToStrongholdPortalTask extends Task {
   private LocateStrongholdCoordinatesTask locateCoordsTask;
   private final int targetEyes;
   private final int MINIMUM_EYES = 12;
   private BlockPos strongholdCoordinates;

   public GoToStrongholdPortalTask(int targetEyes) {
      this.targetEyes = targetEyes;
      this.strongholdCoordinates = null;
      this.locateCoordsTask = new LocateStrongholdCoordinatesTask(targetEyes);
   }

   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      AltoClefController mod = this.controller;
      if (this.strongholdCoordinates == null) {
         this.strongholdCoordinates = this.locateCoordsTask.getStrongholdCoordinates().orElse(null);
         if (this.strongholdCoordinates == null) {
            if (mod.getItemStorage().getItemCount(Items.ENDER_EYE) < 12 && mod.getEntityTracker().itemDropped(Items.ENDER_EYE)) {
               this.setDebugState("Picking up dropped eye");
               return new PickupDroppedItemTask(Items.ENDER_EYE, 12);
            }

            this.setDebugState("Triangulating stronghold...");
            return this.locateCoordsTask;
         }
      }

      if (mod.getPlayer().position().distanceTo(WorldHelper.toVec3d(this.strongholdCoordinates)) < 10.0
         && !mod.getBlockScanner().anyFound(Blocks.END_PORTAL_FRAME)) {
         mod.log("Something went wrong whilst triangulating the stronghold... either the action got disrupted or the second eye went to a different stronghold");
         mod.log("We will try to triangulate again now...");
         this.strongholdCoordinates = null;
         this.locateCoordsTask = new LocateStrongholdCoordinatesTask(this.targetEyes);
         return null;
      } else {
         this.setDebugState("Searching for Stronghold...");
         return new FastTravelTask(this.strongholdCoordinates, 300, true);
      }
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof GoToStrongholdPortalTask;
   }

   @Override
   protected String toDebugString() {
      return "Locating Stronghold";
   }
}
