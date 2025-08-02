package adris.altoclef.util.slots;

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