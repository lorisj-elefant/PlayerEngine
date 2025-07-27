// File: adris/altolef/control/SlotHandler.java
package adris.altoclef.control;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import baritone.api.entity.IInventoryProvider;
import baritone.api.entity.LivingEntityInventory;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.EmptyMapItem;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.OnAStickItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.ToolItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class SlotHandler {
    private final AltoClefController _controller;
    private ItemStack _cursorStack = ItemStack.EMPTY;

    public SlotHandler(AltoClefController controller) {
        this._controller = controller;
    }

    // Симуляция курсора на сервере
    public ItemStack getCursorStack() {
        return _cursorStack;
    }

    public void setCursorStack(ItemStack stack) {
        _cursorStack = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack;
    }

    // Таймеры больше не нужны, серверные операции мгновенны.
    public boolean canDoSlotAction() {
        return true;
    }

    public void registerSlotAction() {
        // Уведомляем трекер, что инвентарь изменился.
        _controller.getItemStorage().registerSlotAction();
    }

    public void clickSlot(Slot slot, int mouseButton, SlotActionType type) {
        if (slot == null || slot.equals(Slot.UNDEFINED)) {
            // Клик "мимо" инвентаря = выбросить предмет с курсора
            if (!_cursorStack.isEmpty()) {
                _controller.getEntity().dropStack(_cursorStack.copy());
                setCursorStack(ItemStack.EMPTY);
                registerSlotAction();
            }
            return;
        }

        DefaultedList<ItemStack> inventory = slot.getInventory();
        int index = slot.getIndex();

        if (inventory == null) {
            Debug.logWarning("Попытка кликнуть по слоту без инвентаря: " + slot);
            return;
        }

        ItemStack slotStack = inventory.get(index);

        switch (type) {
            case PICKUP:
                ItemStack temp = _cursorStack;
                setCursorStack(slotStack);
                inventory.set(index, temp);
                break;

            case QUICK_MOVE:
                // Упрощенная логика shift-клика: переместить в "другой" инвентарь.
                // Требует контекста, который должен предоставляться задачей.
                // Например, из инвентаря игрока в открытый сундук.
                // Пока что просто выбрасываем ошибку, чтобы найти все места использования.
                Debug.logError("QUICK_MOVE не реализован в серверном SlotHandler. Это должно обрабатываться задачей.");
                break;

            default:
                Debug.logWarning("Неподдерживаемый SlotActionType в серверном SlotHandler: " + type);
                break;
        }
        registerSlotAction();
    }

    public void forceEquipItemToOffhand(Item toEquip) {
        LivingEntityInventory inventory = ((IInventoryProvider) _controller.getEntity()).getLivingInventory();
        ItemStack offhandStack = inventory.getStack(PlayerSlot.OFFHAND_SLOT_INDEX);

        if (offhandStack.isOf(toEquip)) {
            return;
        }

        // 1. Найти предмет
        for (int i = 0; i < inventory.main.size(); i++) {
            ItemStack potential = inventory.main.get(i);
            if (potential.isOf(toEquip)) {
                // 2. Поменять местами
                inventory.setStack(PlayerSlot.OFFHAND_SLOT_INDEX, potential);
                inventory.main.set(i, offhandStack);
                registerSlotAction();
                return;
            }
        }
    }

    public boolean forceEquipItem(Item[] toEquip) {
        LivingEntityInventory inventory = ((IInventoryProvider) _controller.getEntity()).getLivingInventory();
        if (Arrays.stream(toEquip).allMatch((i) -> i == inventory.getMainHandStack().getItem())) {
            return true;
        }

        // 1. Ищем в хотбаре
        for (int i = 0; i < 9; i++) {
            int finalI = i;
            if (Arrays.stream(toEquip).allMatch((it) -> it == inventory.getStack(finalI).getItem())) {
                inventory.selectedSlot = i;
                registerSlotAction();
                return true;
            }
        }

        // 2. Ищем в инвентаре и меняем местами с текущим слотом хотбара
        for (int i = 9; i < inventory.main.size(); i++) {
            int finalI = i;
            if (Arrays.stream(toEquip).allMatch((it) -> it == inventory.getStack(finalI).getItem())) {
                ItemStack handStack = inventory.getMainHandStack();
                inventory.setStack(inventory.selectedSlot, inventory.getStack(i));
                inventory.setStack(i, handStack);
                registerSlotAction();
                return true;
            }
        }
        return false;
    }

    public boolean forceEquipItem(Item toEquip) {
        LivingEntityInventory inventory = ((IInventoryProvider) _controller.getEntity()).getLivingInventory();
        if (inventory.getMainHandStack().isOf(toEquip)) {
            return true;
        }

        // 1. Ищем в хотбаре
        for (int i = 0; i < 9; i++) {
            if (inventory.getStack(i).isOf(toEquip)) {
                inventory.selectedSlot = i;
                registerSlotAction();
                return true;
            }
        }

        // 2. Ищем в инвентаре и меняем местами с текущим слотом хотбара
        for (int i = 9; i < inventory.main.size(); i++) {
            if (inventory.getStack(i).isOf(toEquip)) {
                ItemStack handStack = inventory.getMainHandStack();
                inventory.setStack(inventory.selectedSlot, inventory.getStack(i));
                inventory.setStack(i, handStack);
                registerSlotAction();
                return true;
            }
        }
        return false;
    }

    public boolean forceDeequip(Predicate<ItemStack> isBad) {
        LivingEntityInventory inventory = ((IInventoryProvider) _controller.getEntity()).getLivingInventory();
        ItemStack equip = inventory.getMainHandStack();

        if (isBad.test(equip)) {
            int emptySlot = inventory.getEmptySlot();
            if (emptySlot != -1) {
                if (LivingEntityInventory.isValidHotbarIndex(emptySlot)) {
                    // Просто переключаемся на пустой слот хотбара
                    inventory.selectedSlot = emptySlot;
                } else {
                    // Меняем предмет в руке с пустым слотом в инвентаре
                    inventory.setStack(emptySlot, equip);
                    inventory.setStack(inventory.selectedSlot, ItemStack.EMPTY);
                }
                registerSlotAction();
                return true;
            }
            return false; // Нет места для снятия
        }
        return true;
    }

    public boolean forceDeequipHitTool() {
        return forceDeequip(stack -> stack.getItem() instanceof ToolItem);
    }

    public boolean forceEquipItem(ItemTarget toEquip, boolean unInterruptable) {
        if (toEquip == null || toEquip.isEmpty()) {
            return forceDeequip(stack -> !stack.isEmpty());
        }
        if (_controller.getFoodChain().needsToEat() && !unInterruptable) {
            return false;
        }
        for (Item item : toEquip.getMatches()) {
            if (forceEquipItem(item)) {
                return true;
            }
        }
        return false;
    }

    public void refreshInventory() {
        // На сервере это не имеет смысла, инвентарь всегда "актуален".
        // Оставляем метод пустым для совместимости.
    }

    public void forceDeequipRightClickableItem() {
        forceDeequip(stack -> {
                    Item item = stack.getItem();
                    return item instanceof BucketItem // water,lava,milk,fishes
                            || item instanceof EnderEyeItem
                            || item == Items.BOW
                            || item == Items.CROSSBOW
                            || item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE
                            || item == Items.ENDER_PEARL
                            || item instanceof FireworkRocketItem
                            || item instanceof SpawnEggItem
                            || item == Items.END_CRYSTAL
                            || item == Items.EXPERIENCE_BOTTLE
                            || item instanceof PotionItem // also includes splash/lingering
                            || item == Items.TRIDENT
                            || item == Items.WRITABLE_BOOK
                            || item == Items.WRITTEN_BOOK
                            || item instanceof FishingRodItem
                            || item instanceof OnAStickItem
                            || item == Items.COMPASS
                            || item instanceof EmptyMapItem
                            || item instanceof ArmorItem
                            || item == Items.LEAD
                            || item == Items.SHIELD;
                }
        );
    }

    private void swapSlots(Slot slot, Slot target) {
        ItemStack stack = slot.getStack();
        ItemStack targetStack = target.getStack();
        target.getInventory().set(target.getIndex(), stack);
        slot.getInventory().set(slot.getIndex(), targetStack);
    }

    public void forceEquipSlot(AltoClefController controller, Slot slot) {
        Slot target = PlayerSlot.getEquipSlot(controller.getInventory());
        swapSlots(slot, target);
    }

    public void forceEquipArmor(AltoClefController controller, ItemTarget target) {
        LivingEntityInventory inventory = ((IInventoryProvider) _controller.getEntity()).getLivingInventory();
        for (Item item : target.getMatches()) {
            if (item instanceof ArmorItem armorItem) {
                EquipmentSlot slotType = armorItem.getArmorSlot().getEquipmentSlot();

                // Skip if already wearing this or a better piece of armor (simplification)
                if (_controller.getEntity().getEquippedStack(slotType).isOf(item)) {
                    continue;
                }

                // Find the armor piece in inventory
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stackInSlot = inventory.getStack(i);
                    if (stackInSlot.isOf(item)) {
                        // Swap with currently equipped armor
                        ItemStack currentlyEquipped = _controller.getEntity().getEquippedStack(slotType).copy();
                        _controller.getEntity().equipStack(slotType, stackInSlot.copy());
                        inventory.setStack(i, currentlyEquipped);
                        registerSlotAction();
                        break; // Move to the next armor target
                    }
                }
            }
        }
    }
}