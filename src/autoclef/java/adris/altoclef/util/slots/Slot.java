package adris.altoclef.util.slots;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.Objects;

public class Slot {

  public static final int CURSOR_SLOT_INDEX = -1;
  private static final int UNDEFINED_SLOT_INDEX = -999;

  @SuppressWarnings("StaticInitializerReferencesSubClass")
  public static final Slot UNDEFINED = new Slot(null, UNDEFINED_SLOT_INDEX);

  private final DefaultedList<ItemStack> inventory;
  private final int index;

  public Slot(DefaultedList<ItemStack> inventory, int index) {
    this .inventory = inventory;
    this .index = index;
  }

  public DefaultedList<ItemStack> getInventory() {
    return inventory;
  }

  public int getIndex() {
    return index;
  }

  public static boolean isCursor(Slot slot) {
    return slot instanceof CursorSlot;
  }

  public ItemStack getStack() {
    if (inventory == null || index < 0 || index >= inventory.size()) {
      return ItemStack.EMPTY;
    }
    return inventory.get(index);
  }

  @Deprecated
  public int getInventorySlot() {
    return index;
  }

  @Deprecated
  public int getWindowSlot() {
    return -1;
  }

  protected String getName() {
    if (inventory == null) return "Special";
    return inventory.getClass().getSimpleName();
  }

  @Override
  public String toString() {
    return getName() + " Slot {" +
            "inventory=" + (inventory != null ? inventory.getClass().getSimpleName() : "null") +
            ", index=" + index +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Slot slot = (Slot) o;
    return index == slot .index && Objects.equals(inventory, slot .inventory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(inventory, index);
  }
}