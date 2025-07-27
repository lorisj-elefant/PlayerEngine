// File: adris/altoclef/util/slots/Slot.java
package adris.altoclef.util.slots;

import baritone.api.entity.LivingEntityInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.Objects;

/**
 * Серверная реализация Слота.
 * <p>
 * Вместо привязки к ID слота в окне (клиентская концепция),
 * этот класс напрямую ссылается на серверный объект `Inventory` и `index` внутри него.
 * Это делает всю систему работы с инвентарем полностью серверной.
 */
public class Slot {

  // -1 специальный индекс для курсора, -999 для неопределенного слота.
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

  /**
   * @return Инвентарь, к которому принадлежит этот слот. Может быть null для специальных слотов (Undefined, Cursor).
   */
  public DefaultedList<ItemStack> getInventory() {
    return _inventory;
  }

  /**
   * @return Индекс слота внутри его инвентаря.
   */
  public int getIndex() {
    return _index;
  }

  /**
   * Проверяет, является ли данный слот курсором.
   * На сервере курсор симулируется в SlotHandler.
   */
  public static boolean isCursor(Slot slot) {
    return slot instanceof CursorSlot;
  }

  /**
   * @return ItemStack, находящийся в данном слоте.
   */
  public ItemStack getStack() {
    if (_inventory == null || _index < 0 || _index >= _inventory.size()) {
      return ItemStack.EMPTY;
    }
    return _inventory.get(_index);
  }

  /**
   * Устаревшее. Всегда возвращает индекс в инвентаре.
   */
  @Deprecated
  public int getInventorySlot() {
    return _index;
  }

  /**
   * Устаревшее. На сервере нет "оконных" слотов. Возвращает -1.
   */
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
    // Слоты равны, если это один и тот же инвентарь и один и тот же индекс.
    return _index == slot._index && Objects.equals(_inventory, slot._inventory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_inventory, _index);
  }
}