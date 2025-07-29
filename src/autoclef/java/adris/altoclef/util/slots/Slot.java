package adris.altoclef.util.slots;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.Objects;

public class Slot {

  public static final int CURSOR_SLOT_INDEX = -1;
  private static final int UNDEFINED_SLOT_INDEX = -999;

  @SuppressWarnings("StaticInitializerReferencesSubClass")
  public static final Slot UNDEFINED = new Slot(null, UNDEFINED_SLOT_INDEX);

  private final DefaultedList<ItemStack> _inventory;
  private final int _index;

  public Slot(DefaultedList<ItemStack> inventory, int index) {
    this._inventory = inventory;
    this._index = index;
  }

  public DefaultedList<ItemStack> getInventory() {
    return _inventory;
  }

  public int getIndex() {
    return _index;
  }

  public static boolean isCursor(Slot slot) {
    return slot instanceof CursorSlot;
  }

  public ItemStack getStack() {
    if (_inventory == null || _index < 0 || _index >= _inventory.size()) {
      return ItemStack.EMPTY;
    }
    return _inventory.get(_index);
  }

  @Deprecated
  public int getInventorySlot() {
    return _index;
  }

  @Deprecated
  public int getWindowSlot() {
    return -1;
  }

  protected String getName() {
    if (_inventory == null) return "Special";
    return _inventory.getClass().getSimpleName();
  }

  @Override
  public String toString() {
    return getName() + " Slot {" +
            "inventory=" + (_inventory != null ? _inventory.getClass().getSimpleName() : "null") +
            ", index=" + _index +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Slot slot = (Slot) o;
    return _index == slot._index && Objects.equals(_inventory, slot._inventory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_inventory, _index);
  }
}