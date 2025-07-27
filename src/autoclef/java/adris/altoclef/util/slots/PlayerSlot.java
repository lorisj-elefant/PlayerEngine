// File: adris/altoclef/util/slots/PlayerSlot.java
package adris.altoclef.util.slots;

import baritone.api.entity.LivingEntityInventory;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.Inventory;

/**
 * Содержит константы-индексы для слотов инвентаря игрока/сущности (LivingEntityInventory).
 */
public final class PlayerSlot {

  // LivingEntityInventory.main (0-35)
  // 0-8: Хотбар
  // 9-35: Основной инвентарь

  // LivingEntityInventory.armor (0-3)
  public static final int ARMOR_BOOTS_SLOT_INDEX = 0;
  public static final int ARMOR_LEGGINGS_SLOT_INDEX = 1;
  public static final int ARMOR_CHESTPLATE_SLOT_INDEX = 2;
  public static final int ARMOR_HELMET_SLOT_INDEX = 3;
  public static final int[] ARMOR_SLOTS_INDICES = {
          ARMOR_BOOTS_SLOT_INDEX, ARMOR_LEGGINGS_SLOT_INDEX, ARMOR_CHESTPLATE_SLOT_INDEX, ARMOR_HELMET_SLOT_INDEX
  };

  // LivingEntityInventory.offHand (0)
  public static final int OFFHAND_SLOT_INDEX = 0;

  // Хелпер-методы для создания объектов Slot для инвентаря сущности
  public static Slot getMainSlot(LivingEntityInventory inventory, int index) {
    return new Slot(inventory.main, index);
  }

  public static Slot getArmorSlot(LivingEntityInventory inventory, int armorIndex) {
    return new Slot(inventory.armor, armorIndex);
  }

  public static Slot getOffhandSlot(LivingEntityInventory inventory) {
    return new Slot(inventory.offHand, OFFHAND_SLOT_INDEX);
  }

  public static Slot getEquipSlot(LivingEntityInventory inventory, EquipmentSlot equipSlot) {
    switch (equipSlot.getType()) {
      case HAND:
        if (equipSlot == EquipmentSlot.MAINHAND) {
          return getMainSlot(inventory, inventory.selectedSlot);
        } else {
          return getOffhandSlot(inventory);
        }
      case ARMOR:
        return getArmorSlot(inventory, equipSlot.getEntitySlotId());
    }
    return Slot.UNDEFINED;
  }

  public static Slot getEquipSlot(LivingEntityInventory inventory) {
    return getEquipSlot(inventory, EquipmentSlot.MAINHAND);
  }
}