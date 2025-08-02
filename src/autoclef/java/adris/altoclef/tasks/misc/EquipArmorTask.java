package adris.altoclef.tasks.misc;

import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.item.Item;

import java.util.Arrays;

public class EquipArmorTask extends Task {

    private final ItemTarget[] toEquip;

    public EquipArmorTask(ItemTarget... toEquip) {
        this.toEquip = toEquip;
    }

    public EquipArmorTask(Item... toEquip) {
        this(Arrays.stream(toEquip).map(ItemTarget::new).toArray(ItemTarget[]::new));
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected Task onTick() {
        ItemTarget[] armorNotPresent = Arrays.stream(toEquip)
                .filter(target -> !controller.getItemStorage().hasItem(target.getMatches()) && !StorageHelper.isArmorEquipped(controller, target.getMatches()))
                .toArray(ItemTarget[]::new);

        if (armorNotPresent.length > 0) {
            setDebugState("Obtaining armor to equip.");
            return new CataloguedResourceTask(armorNotPresent);
        }

        setDebugState("Equipping armor.");
        for (ItemTarget target : toEquip) {
            if (!StorageHelper.isArmorEquipped(controller, target.getMatches())) {
                // The actual equipping logic is now inside SlotHandler
                controller.getSlotHandler().forceEquipArmor(controller, target);
            }
        }

        // Equipping is now synchronous, so we can return null.
        // The isFinished check will pass on the next tick.
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
    }

    @Override
    public boolean isFinished() {
        return Arrays.stream(toEquip).allMatch(target -> StorageHelper.isArmorEquipped(controller, target.getMatches()));
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof EquipArmorTask task) {
            return Arrays.equals(task.toEquip, this.toEquip);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Equipping armor: " + Arrays.toString(toEquip);
    }
}