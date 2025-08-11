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

package altoclef.util.slots;

import baritone.api.entity.LivingEntityInventory;
import net.minecraft.world.entity.EquipmentSlot;

public final class PlayerSlot {
   public static final int ARMOR_BOOTS_SLOT_INDEX = 0;
   public static final int ARMOR_LEGGINGS_SLOT_INDEX = 1;
   public static final int ARMOR_CHESTPLATE_SLOT_INDEX = 2;
   public static final int ARMOR_HELMET_SLOT_INDEX = 3;
   public static final int[] ARMOR_SLOTS_INDICES = new int[]{0, 1, 2, 3};
   public static final int OFFHAND_SLOT_INDEX = 0;

   public static Slot getMainSlot(LivingEntityInventory inventory, int index) {
      return new Slot(inventory.main, index);
   }

   public static Slot getArmorSlot(LivingEntityInventory inventory, int armorIndex) {
      return new Slot(inventory.armor, armorIndex);
   }

   public static Slot getOffhandSlot(LivingEntityInventory inventory) {
      return new Slot(inventory.offHand, 0);
   }

   public static Slot getEquipSlot(LivingEntityInventory inventory, EquipmentSlot equipSlot) {
      switch (equipSlot.getType()) {
         case HAND:
            if (equipSlot == EquipmentSlot.MAINHAND) {
               return getMainSlot(inventory, inventory.selectedSlot);
            }

            return getOffhandSlot(inventory);
         case ARMOR:
            return getArmorSlot(inventory, equipSlot.getIndex());
         default:
            return Slot.UNDEFINED;
      }
   }

   public static Slot getEquipSlot(LivingEntityInventory inventory) {
      return getEquipSlot(inventory, EquipmentSlot.MAINHAND);
   }
}
