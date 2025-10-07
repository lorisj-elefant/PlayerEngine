package adris.altoclef.tasks;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

// Assumes you have the item in your inv already, and the entity is close
public class UseItemOnPositionTask extends Task {
    public static final Logger LOGGER = LogManager.getLogger();
    private final TimerGame lookAtGame = new TimerGame(3.0);
    private boolean hasStartedLook = false;

    String itemName;
    BlockPos position;
    protected final MovementProgressChecker progress = new MovementProgressChecker();
    Item item;
    Optional<Task> specialCase = Optional.empty();
    boolean hasInteracted = false;
    Supplier<Task> extraInteractEffect;

    public UseItemOnPositionTask(String itemName, BlockPos position, @Nullable Supplier<Task> interact) {
        this.itemName = itemName;
        this.position = position;
        this.extraInteractEffect = interact;
        if (itemName.equals("hand")) {
            return;
        }
        // will err if itemName not found
        item = ItemHelper.fromString(itemName).get();
    }

    private void equipItem() {
        AltoClefController mod = this.controller;
        if (itemName.equals("hand")) {
            mod.getSlotHandler().forceDeequip(stack -> !stack.isEmpty());
            return;
        }
        mod.getSlotHandler().forceEquipItem(item);
    }

    @Override
    protected boolean isEqual(Task var1) {
        // TODO Auto-generated method stub
        if (!(var1 instanceof UseItemOnPositionTask)) {
            return false;
        }
        UseItemOnPositionTask rhs = (UseItemOnPositionTask) var1;
        return rhs.itemName.equals(this.itemName) && rhs.position.equals(this.position);
    }

    @Override
    protected void onStart() {
        this.progress.reset();
        hasInteracted = false;
    }

    @Override
    protected Task onTick() {
        if (hasInteracted) {
            return null;
        }

        AltoClefController mod = this.controller;

        if (mod.getPlayer().distanceToSqr(position.getCenter()) > 1) {
            LOGGER.info("going to pos");
            return new GetToBlockTask(position, false);
        }
        if (!hasStartedLook) {
            LOGGER.info("Starting to look");
            lookAtGame.reset();
            hasStartedLook = true;
        }
        LookHelper.lookAt(mod, position);
        if (!hasStartedLook || !lookAtGame.elapsed()) {
            return null;
        }
        LOGGER.info("Done looking, equiping and interacting");
        equipItem();
        if (itemName.equals("hand")) {
            mod.getInputControls().hold(Input.CLICK_RIGHT);
        }
        if (extraInteractEffect != null) {
            Task output = extraInteractEffect.get();
            mod.copiedServerPlayer.updateOriginalItem(mod); // update item from copy => mod
            hasInteracted = true;
            return output;
        }
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClefController mod = this.controller;
        mod.getInputControls().release(Input.CLICK_RIGHT);
    }

    @Override
    protected String toDebugString() {
        return String.format("UseItemOnPosition(%s on %s)", itemName, position.toString());
    }

    @Override
    public boolean isFinished() {
        // TODO Auto-generated method stub
        return hasInteracted;
    }
}