package adris.altoclef.tasks.container;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import baritone.api.entity.IInventoryProvider;
import baritone.api.entity.LivingEntityInventory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class PickupFromContainerTask extends Task {

  private final BlockPos _containerPos;
  private final ItemTarget[] _targets;

  public PickupFromContainerTask(BlockPos targetContainer, ItemTarget... targets) {
    this._containerPos = targetContainer;
    this._targets = targets;
  }

  @Override
  protected void onStart() {
  }


  @Override
  protected Task onTick() {
    if (isFinished()) {
      return null;
    }

    // Go to container
    if (!_containerPos.isWithinDistance(new Vec3i((int) controller.getEntity().getPos().x, (int) controller.getEntity().getPos().y, (int) controller.getEntity().getPos().z), 4.5)) {
      return new GetToBlockTask(_containerPos);
    }

    // Get container inventory
    BlockEntity be = controller.getWorld().getBlockEntity(_containerPos);
    if (!(be instanceof LootableContainerBlockEntity container)) {
      Debug.logWarning("Block at " + _containerPos + " is not a lootable container. Stopping.");
      return null;
    }
    Inventory containerInventory = container;
    LivingEntityInventory playerInventory = ((IInventoryProvider) controller.getEntity()).getLivingInventory();

    // For each target, try to move items.
    for (ItemTarget target : _targets) {
      int needed = target.getTargetCount() - controller.getItemStorage().getItemCount(target);
      if (needed <= 0) continue;

      // Find item in container
      for (int i = 0; i < containerInventory.size(); i++) {
        ItemStack stack = containerInventory.getStack(i);
        if (target.matches(stack.getItem())) {
          setDebugState("Looting " + target);

          // Ensure we have space
          if (!playerInventory.insertStack(new ItemStack(stack.getItem()))) {
            return new EnsureFreeInventorySlotTask();
          }

          // Loot
          ItemStack toMove = stack.copy();
          int moveAmount = Math.min(toMove.getCount(), needed);
          toMove.setCount(moveAmount);

          if (playerInventory.insertStack(toMove)) {
            stack.decrement(moveAmount);
            containerInventory.setStack(i, stack);
            container.markDirty();
            controller.getItemStorage().registerSlotAction();
          }

          // We took an action, let's re-evaluate next tick.
          return null;
        }
      }
    }

    setDebugState("Waiting for items to appear in container or finishing.");
    return null;
  }

  @Override
  protected void onStop(Task interruptTask) {
  }

  @Override
  public boolean isFinished() {
    return Arrays.stream(_targets).allMatch(target ->
            controller.getItemStorage().getItemCount(target) >= target.getTargetCount()
    );
  }

  @Override
  protected boolean isEqual(Task other) {
    if (other instanceof PickupFromContainerTask task) {
      return Objects.equals(task._containerPos, _containerPos) && Arrays.equals(task._targets, _targets);
    }
    return false;
  }

  @Override
  protected String toDebugString() {
    return "Picking up from container at (" + _containerPos.toShortString() + "): " + Arrays.toString(_targets);
  }
}