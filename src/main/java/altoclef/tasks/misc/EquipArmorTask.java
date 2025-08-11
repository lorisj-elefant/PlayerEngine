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

package altoclef.tasks.misc;

import altoclef.tasks.squashed.CataloguedResourceTask;
import altoclef.tasksystem.Task;
import altoclef.util.ItemTarget;
import altoclef.util.helpers.StorageHelper;
import java.util.Arrays;
import net.minecraft.world.item.Item;

public class EquipArmorTask extends Task {
   private final ItemTarget[] toEquip;

   public EquipArmorTask(ItemTarget... toEquip) {
      this.toEquip = toEquip;
   }

   public EquipArmorTask(Item... toEquip) {
      this(Arrays.stream(toEquip).map(ItemTarget::new).toArray(ItemTarget[]::new));
   }

   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      ItemTarget[] armorNotPresent = Arrays.stream(this.toEquip)
         .filter(
            targetx -> !this.controller.getItemStorage().hasItem(targetx.getMatches()) && !StorageHelper.isArmorEquipped(this.controller, targetx.getMatches())
         )
         .toArray(ItemTarget[]::new);
      if (armorNotPresent.length > 0) {
         this.setDebugState("Obtaining armor to equip.");
         return new CataloguedResourceTask(armorNotPresent);
      } else {
         this.setDebugState("Equipping armor.");

         for (ItemTarget target : this.toEquip) {
            if (!StorageHelper.isArmorEquipped(this.controller, target.getMatches())) {
               this.controller.getSlotHandler().forceEquipArmor(this.controller, target);
            }
         }

         return null;
      }
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   public boolean isFinished() {
      return Arrays.stream(this.toEquip).allMatch(target -> StorageHelper.isArmorEquipped(this.controller, target.getMatches()));
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof EquipArmorTask task ? Arrays.equals((Object[])task.toEquip, (Object[])this.toEquip) : false;
   }

   @Override
   protected String toDebugString() {
      return "Equipping armor: " + Arrays.toString((Object[])this.toEquip);
   }
}
