package adris.altoclef.tasks.slot;

import adris.altoclef.control.SlotHandler;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.slots.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ClickSlotTask extends Task {
    private final Slot slot;

    private final int mouseButton;

    private final SlotActionType type;

    private boolean clicked = false;

    public ClickSlotTask(Slot slot, int mouseButton, SlotActionType type) {
        this.slot = slot;
        this.mouseButton = mouseButton;
        this.type = type;
    }

    public ClickSlotTask(Slot slot, SlotActionType type) {
        this(slot, 0, type);
    }

    public ClickSlotTask(Slot slot, int mouseButton) {
        this(slot, mouseButton, SlotActionType.PICKUP);
    }

    public ClickSlotTask(Slot slot) {
        this(slot, SlotActionType.PICKUP);
    }

    protected void onStart() {
        this.clicked = false;
    }

    protected Task onTick() {
        SlotHandler slotHandler = controller.getSlotHandler();
        if (slotHandler.canDoSlotAction()) {
            slotHandler.clickSlot(this.slot, this.mouseButton, this.type);
            slotHandler.registerSlotAction();
            this.clicked = true;
        }
        return null;
    }

    protected void onStop(Task interruptTask) {
    }

    protected boolean isEqual(Task obj) {
        if (obj instanceof adris.altoclef.tasks.slot.ClickSlotTask) {
            adris.altoclef.tasks.slot.ClickSlotTask task = (adris.altoclef.tasks.slot.ClickSlotTask) obj;
            return (task.mouseButton == this.mouseButton && task.type == this.type && task.slot.equals(this.slot));
        }
        return false;
    }

    protected String toDebugString() {
        return "Clicking " + this.slot.toString();
    }

    public boolean isFinished() {
        return this.clicked;
    }
}
