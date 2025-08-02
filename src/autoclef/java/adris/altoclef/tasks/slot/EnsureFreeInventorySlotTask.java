package adris.altoclef.tasks.slot;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Optional;

public class EnsureFreeInventorySlotTask extends Task {
    protected void onStart() {
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot(controller);
        Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
        if (cursorStack.isEmpty() &&
                garbage.isPresent()) {
            mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
            return null;
        }
        if (!cursorStack.isEmpty()) {
            LookHelper.randomOrientation(controller);
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            return null;
        }
        setDebugState("All items are protected.");
        return null;
    }

    protected void onStop(Task interruptTask) {
    }

    protected boolean isEqual(Task obj) {
        return obj instanceof adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
    }

    protected String toDebugString() {
        return "Ensuring inventory is free";
    }
}
