package adris.altoclef.trackers.storage;

import adris.altoclef.AltoClefController;
import adris.altoclef.trackers.Tracker;
import adris.altoclef.trackers.TrackerManager;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.slots.Slot;
import baritone.api.entity.IInventoryProvider;
import baritone.api.entity.LivingEntityInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class InventorySubTracker extends Tracker {

  private final Map<Item, List<Integer>> itemToSlotPlayer = new HashMap<>();
  private final Map<Item, Integer> itemCountsPlayer = new HashMap<>();

  public InventorySubTracker(TrackerManager manager) {
    super(manager);
  }

  public int getItemCount(Item... items) {
    ensureUpdated();
    int result = 0;
    ItemStack cursorStack = mod.getSlotHandler().getCursorStack();
    for (Item item : items) {
      if (cursorStack.isOf(item)) {
        result += cursorStack.getCount();
      }
      result += itemCountsPlayer.getOrDefault(item, 0);
    }
    return result;
  }

  public boolean hasItem(Item... items) {
    ensureUpdated();
    ItemStack cursorStack = mod.getSlotHandler().getCursorStack();
    for (Item item : items) {
      if (cursorStack.isOf(item)) {
        return true;
      }
      if (itemCountsPlayer.containsKey(item)) {
        return true;
      }
    }
    return false;
  }

  public List<Slot> getSlotsWithItemsPlayerInventory(boolean includeArmor, Item... items) {
    ensureUpdated();
    List<Slot> result = new ArrayList<>();
    LivingEntityInventory inventory = ((IInventoryProvider) mod.getEntity()).getLivingInventory();

    for (Item item : items) {
      if (itemToSlotPlayer.containsKey(item)) {
        for(Integer index : itemToSlotPlayer.get(item)) {
          result.add(new Slot(inventory.main, index));
        }
      }
    }

    if (includeArmor) {
      for (int i = 0; i < inventory.armor.size(); i++) {
        ItemStack stack = inventory.armor.get(i);
        if (Arrays.stream(items).anyMatch(stack::isOf)) {
          result.add(new Slot(inventory.armor, i));
        }
      }
    }

    ItemStack offhandStack = inventory.offHand.get(0);
    if (Arrays.stream(items).anyMatch(offhandStack::isOf)) {
      result.add(new Slot(inventory.offHand, 0));
    }

    return result;
  }


  public List<ItemStack> getInventoryStacks() {
    ensureUpdated();
    LivingEntityInventory inventory = ((IInventoryProvider) mod.getEntity()).getLivingInventory();
    List<ItemStack> stacks = new ArrayList<>();
    stacks.addAll(inventory.main);
    stacks.addAll(inventory.armor);
    stacks.addAll(inventory.offHand);
    return stacks;
  }

  public List<Slot> getSlotsThatCanFit(ItemStack item, boolean acceptPartial) {
    ensureUpdated();
    List<Slot> result = new ArrayList<>();
    LivingEntityInventory inventory = ((IInventoryProvider) mod.getEntity()).getLivingInventory();

    // Check for stackable slots
    if (item.isStackable()) {
      for (int i = 0; i < inventory.main.size(); i++) {
        ItemStack stackInSlot = inventory.main.get(i);
        if (ItemHelper.canStackTogether(item, stackInSlot)) {
          int roomLeft = stackInSlot.getMaxCount() - stackInSlot.getCount();
          if (acceptPartial || roomLeft >= item.getCount()) {
            result.add(new Slot(inventory.main, i));
          }
        }
      }
    }

    // Check for empty slots
    for (int i = 0; i < inventory.main.size(); i++) {
      if (inventory.main.get(i).isEmpty()) {
        result.add(new Slot(inventory.main, i));
      }
    }
    return result;
  }

  public boolean hasEmptySlot() {
    ensureUpdated();
    return itemCountsPlayer.getOrDefault(Items.AIR, 0) > 0;
  }

  @Override
  protected void updateState() {
    reset();
    LivingEntityInventory inventory = ((IInventoryProvider) mod.getEntity()).getLivingInventory();
    if (inventory == null) return;

    // Main inventory + hotbar
    for (int i = 0; i < inventory.main.size(); i++) {
      ItemStack stack = inventory.main.get(i);
      registerItem(stack, i, inventory.main);
    }
    // Armor
    for (int i = 0; i < inventory.armor.size(); i++) {
      ItemStack stack = inventory.armor.get(i);
      registerItem(stack, i, inventory.armor);
    }
    // Offhand
    for (int i = 0; i < inventory.offHand.size(); i++) {
      ItemStack stack = inventory.offHand.get(i);
      registerItem(stack, i, inventory.offHand);
    }
  }

  private void registerItem(ItemStack stack, int index, DefaultedList<ItemStack> inventory) {
    Item item = stack.isEmpty() ? Items.AIR : stack.getItem();
    int count = stack.getCount();

    itemCountsPlayer.put(item, itemCountsPlayer.getOrDefault(item, 0) + count);

    // We only map slots for main inventory as armor/offhand are handled separately.
    if (inventory instanceof net.minecraft.util.collection.DefaultedList) {
      itemToSlotPlayer.computeIfAbsent(item, k -> new ArrayList<>()).add(index);
    }
  }

  @Override
  protected void reset() {
    itemToSlotPlayer.clear();
    itemCountsPlayer.clear();
  }
}