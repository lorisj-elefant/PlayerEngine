// File: adris/altoclef/tasks/container/LootContainerTask.java
package adris.altoclef.tasks.container;

import adris.altoclef.Debug;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.Task;
import baritone.api.entity.IInventoryProvider;
import baritone.api.entity.LivingEntityInventory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class LootContainerTask extends Task {

  private final BlockPos _containerPos;
  private final List<Item> _targets;
  private final Predicate<ItemStack> _check;
  private boolean _finished = false;

  public LootContainerTask(BlockPos chestPos, List<Item> items, Predicate<ItemStack> pred) {
    this._containerPos = chestPos;
    this._targets = items;
    this._check = pred;
  }

  public LootContainerTask(BlockPos chestPos, List<Item> items) {
    this(chestPos, items, itemStack -> true);
  }

  @Override
  protected void onStart() {
    // Protect items we want to loot so we don't drop them
    controller.getBehaviour().push();
    controller.getBehaviour().addProtectedItems(_targets.toArray(new Item[0]));
  }

  @Override
  protected Task onTick() {
    if (_finished) {
      return null;
    }

    // Go to container
    if (!_containerPos.isWithinDistance(new Vec3i((int) controller.getEntity().getPos().x, (int) controller.getEntity().getPos().y, (int) controller.getEntity().getPos().z), 4.5)) {
      setDebugState("Going to container");
      return new GetToBlockTask(_containerPos);
    }

    // Get inventories
    BlockEntity be = controller.getWorld().getBlockEntity(_containerPos);
    if (!(be instanceof LootableContainerBlockEntity container)) {
      Debug.logWarning("Block at " + _containerPos + " is not a lootable container. Stopping.");
      _finished = true;
      return null;
    }
    Inventory containerInventory = container;
    LivingEntityInventory playerInventory = ((IInventoryProvider) controller.getEntity()).getLivingInventory();

    // Update our cache of this container
    controller.getItemStorage().containers.WritableCache(controller, _containerPos);

    boolean somethingToLoot = false;
    // Loot items
    setDebugState("Looting items: " + _targets);
    for (int i = 0; i < containerInventory.size(); i++) {
      ItemStack stack = containerInventory.getStack(i);
      if (stack.isEmpty() || !_targets.contains(stack.getItem()) || !_check.test(stack)) {
        continue;
      }

      somethingToLoot = true;

      // Ensure we have space.
      if (!playerInventory.insertStack(new ItemStack(stack.getItem()))) {
        setDebugState("Inventory is full, ensuring space.");
        return new EnsureFreeInventorySlotTask();
      }

      // Loot the entire stack.
      if (playerInventory.insertStack(stack.copy())) {
        containerInventory.setStack(i, ItemStack.EMPTY);
        container.markDirty();
        controller.getItemStorage().registerSlotAction();
        // Action taken, restart tick to re-evaluate state.
        return null;
      } else {
        Debug.logWarning("Failed to insert stack even after checking for space.");
      }
    }

    // If we iterated through the whole container and found nothing, we're done.
    if (!somethingToLoot) {
      setDebugState("Container empty or has no desired items.");
      _finished = true;
    }

    return null;
  }

  @Override
  public boolean isFinished() {
    // Additionally check if the container is gone
    if (_finished || !controller.getChunkTracker().isChunkLoaded(_containerPos) ||
            !(controller.getWorld().getBlockEntity(_containerPos) instanceof LootableContainerBlockEntity)) {
      return true;
    }
    return false;
  }

  @Override
  protected void onStop(Task interruptTask) {
    controller.getBehaviour().pop();
  }

  @Override
  protected boolean isEqual(Task other) {
    if (other instanceof LootContainerTask task) {
      return Objects.equals(task._containerPos, _containerPos) &&
              new ArrayList<>(task._targets).equals(new ArrayList<>(_targets));
    }
    return false;
  }

  @Override
  protected String toDebugString() {
    return "Looting container at " + _containerPos.toShortString();
  }
}