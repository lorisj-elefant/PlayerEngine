package adris.altoclef.tasks.container;

import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import baritone.api.entity.IInventoryProvider;
import baritone.api.entity.LivingEntityInventory;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class StoreInContainerTask extends Task {

  public static final Block[] CONTAINER_BLOCKS = Stream.concat(
          Arrays.stream(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL}),
          Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.SHULKER_BOXES))
  ).toArray(Block[]::new);

  private final BlockPos _containerPos;
  private final boolean _getIfNotPresent;
  private final ItemTarget[] _toStore;

  public StoreInContainerTask(BlockPos targetContainer, boolean getIfNotPresent, ItemTarget... toStore) {
    this._containerPos = targetContainer;
    this._getIfNotPresent = getIfNotPresent;
    this._toStore = toStore;
  }

  @Override
  protected void onStart() {
    // Protect items we want to store
    for (ItemTarget target : _toStore) {
      controller.getBehaviour().addProtectedItems(target.getMatches());
    }
  }

  @Override
  protected Task onTick() {
    // Are we done?
    if (isFinished()) {
      return null;
    }

    // Do we need to collect items first?
    if (_getIfNotPresent) {
      for (ItemTarget target : _toStore) {
        int needed = target.getTargetCount(); // Simplified: just ensure the total amount is available.
        if (controller.getItemStorage().getItemCount(target) < needed) {
          setDebugState("Collecting " + target + " first.");
          return TaskCatalogue.getItemTask(target);
        }
      }
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
      return null;
    }
    Inventory containerInventory = container;
    LivingEntityInventory playerInventory = ((IInventoryProvider) controller.getEntity()).getLivingInventory();

    // Update our cache of this container
    controller.getItemStorage().containers.WritableCache(controller, _containerPos);

    // Store items
    setDebugState("Storing items");
    for (ItemTarget target : _toStore) {
      int currentInContainer = countItem(containerInventory, target);
      if (currentInContainer >= target.getTargetCount()) {
        continue; // This target is satisfied
      }
      int neededInContainer = target.getTargetCount() - currentInContainer;

      // Find this item in our inventory and move it
      for (int i = 0; i < playerInventory.size(); i++) {
        ItemStack playerStack = playerInventory.getStack(i);
        if (target.matches(playerStack.getItem())) {
          int toMove = Math.min(neededInContainer, playerStack.getCount());

          // Try to move `toMove` items from player to container.
          ItemStack toInsert = playerStack.copy();
          toInsert.setCount(toMove);

          if (insertStack(containerInventory, toInsert, true).getCount()!=toInsert.getCount()) {
            ItemStack remainder = insertStack(containerInventory, toInsert, false);
            int moved = toMove - remainder.getCount();
            if (moved > 0) {
              playerStack.decrement(moved);
              playerInventory.setStack(i, playerStack);
              container.markDirty();
              controller.getItemStorage().registerSlotAction();
              // Action taken, restart tick to re-evaluate state.
              return null;
            }
          }
        }
      }
    }

    // If we get here, all possible items have been stored.
    return null;
  }

  @Override
  public boolean isFinished() {
    // Check if all targets are met inside the container
    BlockEntity be = controller.getWorld().getBlockEntity(_containerPos);
    if (be instanceof Inventory containerInv) {
      return Arrays.stream(_toStore).allMatch(target ->
              countItem(containerInv, target) >= target.getTargetCount()
      );
    }
    // If we can't access the container, assume we're not done unless we have no items to store.
    return Arrays.stream(_toStore).allMatch(target ->
            controller.getItemStorage().getItemCount(target) == 0
    );
  }

  @Override
  protected void onStop(Task interruptTask) {
    controller.getBehaviour().pop();
  }

  @Override
  protected boolean isEqual(Task other) {
    if (other instanceof StoreInContainerTask task) {
      return Objects.equals(task._containerPos, _containerPos) &&
              task._getIfNotPresent == _getIfNotPresent &&
              Arrays.equals(task._toStore, _toStore);
    }
    return false;
  }

  @Override
  protected String toDebugString() {
    return "Storing in container[" + _containerPos.toShortString() + "] " + Arrays.toString(_toStore);
  }

  // Helper to count items in an inventory
  private int countItem(Inventory inventory, ItemTarget target) {
    int count = 0;
    for (int i = 0; i < inventory.size(); i++) {
      ItemStack stack = inventory.getStack(i);
      if (target.matches(stack.getItem())) {
        count += stack.getCount();
      }
    }
    return count;
  }

  // Helper for inserting into an inventory
  private ItemStack insertStack(Inventory inventory, ItemStack stack, boolean simulate) {
    if(simulate){
      stack = stack.copy();
    }
    // Try merging with existing stacks first
    for (int i = 0; i < inventory.size(); i++) {
      if (stack.isEmpty()) break;
      ItemStack slotStack = inventory.getStack(i);
      if (ItemStack.canCombine(stack, slotStack)) {
        int space = slotStack.getMaxCount() - slotStack.getCount();
        int toTransfer = Math.min(stack.getCount(), space);
        if(toTransfer > 0) {
          slotStack.increment(toTransfer);
          stack.decrement(toTransfer);
          if(!simulate)
            inventory.setStack(i, slotStack);
        }
      }
    }
    // Put remainder in empty slots
    for (int i = 0; i < inventory.size(); i++) {
      if (stack.isEmpty()) break;
      if (inventory.getStack(i).isEmpty()) {
        if(!simulate)
          inventory.setStack(i, stack.copy());
        stack.setCount(0);
      }
    }
    return stack;
  }
}