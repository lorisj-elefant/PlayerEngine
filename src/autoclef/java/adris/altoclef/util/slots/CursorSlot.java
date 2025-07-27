// File: adris/altoclef/util/slots/CursorSlot.java
package adris.altoclef.util.slots;

/**
 * Представляет "курсор" на сервере.
 * Настоящий предмет в курсоре будет храниться в SlotHandler.
 * Этот класс используется как уникальный маркер.
 */
public final class CursorSlot extends Slot {
  public static final CursorSlot SLOT = new CursorSlot();

  private CursorSlot() {
    super(null, CURSOR_SLOT_INDEX);
  }

  @Override
  protected String getName() {
    return "Cursor Slot";
  }
}