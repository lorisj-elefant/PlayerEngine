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

import altoclef.TaskCatalogue;
import altoclef.tasksystem.Task;
import altoclef.util.MiningRequirement;
import altoclef.util.helpers.StorageHelper;
import net.minecraft.world.item.Items;

public class SatisfyMiningRequirementTask extends Task {
   private final MiningRequirement requirement;

   public SatisfyMiningRequirementTask(MiningRequirement requirement) {
      this.requirement = requirement;
   }

   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      switch (this.requirement) {
         case HAND:
         default:
            return null;
         case WOOD:
            return TaskCatalogue.getItemTask(Items.WOODEN_PICKAXE, 1);
         case STONE:
            return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 1);
         case IRON:
            return TaskCatalogue.getItemTask(Items.IRON_PICKAXE, 1);
         case DIAMOND:
            return TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
      }
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof SatisfyMiningRequirementTask task ? task.requirement == this.requirement : false;
   }

   @Override
   protected String toDebugString() {
      return "Satisfy Mining Req: " + this.requirement;
   }

   @Override
   public boolean isFinished() {
      return StorageHelper.miningRequirementMetInventory(this.controller, this.requirement);
   }
}
