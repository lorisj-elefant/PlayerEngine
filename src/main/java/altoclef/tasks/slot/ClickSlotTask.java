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

import altoclef.control.SlotHandler;
import altoclef.tasksystem.Task;
import altoclef.util.slots.Slot;
import net.minecraft.world.inventory.ClickType;

public class ClickSlotTask extends Task {
   private final Slot slot;
   private final int mouseButton;
   private final ClickType type;
   private boolean clicked = false;

   public ClickSlotTask(Slot slot, int mouseButton, ClickType type) {
      this.slot = slot;
      this.mouseButton = mouseButton;
      this.type = type;
   }

   public ClickSlotTask(Slot slot, ClickType type) {
      this(slot, 0, type);
   }

   public ClickSlotTask(Slot slot, int mouseButton) {
      this(slot, mouseButton, ClickType.PICKUP);
   }

   public ClickSlotTask(Slot slot) {
      this(slot, ClickType.PICKUP);
   }

   @Override
   protected void onStart() {
      this.clicked = false;
   }

   @Override
   protected Task onTick() {
      SlotHandler slotHandler = this.controller.getSlotHandler();
      if (slotHandler.canDoSlotAction()) {
         slotHandler.clickSlot(this.slot, this.mouseButton, this.type);
         slotHandler.registerSlotAction();
         this.clicked = true;
      }

      return null;
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   protected boolean isEqual(Task obj) {
      return !(obj instanceof ClickSlotTask task) ? false : task.mouseButton == this.mouseButton && task.type == this.type && task.slot.equals(this.slot);
   }

   @Override
   protected String toDebugString() {
      return "Clicking " + this.slot.toString();
   }

   @Override
   public boolean isFinished() {
      return this.clicked;
   }
}
