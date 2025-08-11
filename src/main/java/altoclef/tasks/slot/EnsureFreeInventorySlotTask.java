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

package altoclef.tasks.slot;

import altoclef.AltoClefController;
import altoclef.tasksystem.Task;
import altoclef.util.helpers.LookHelper;
import altoclef.util.helpers.StorageHelper;
import altoclef.util.slots.Slot;
import java.util.Optional;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class EnsureFreeInventorySlotTask extends Task {
   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      AltoClefController mod = this.controller;
      ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot(this.controller);
      Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
      if (cursorStack.isEmpty() && garbage.isPresent()) {
         mod.getSlotHandler().clickSlot(garbage.get(), 0, ClickType.PICKUP);
         return null;
      } else if (!cursorStack.isEmpty()) {
         LookHelper.randomOrientation(this.controller);
         mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, ClickType.PICKUP);
         return null;
      } else {
         this.setDebugState("All items are protected.");
         return null;
      }
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   protected boolean isEqual(Task obj) {
      return obj instanceof EnsureFreeInventorySlotTask;
   }

   @Override
   protected String toDebugString() {
      return "Ensuring inventory is free";
   }
}
