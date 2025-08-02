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

  private final BlockPos containerPos;
  private final List<Item> targets;
  private final Predicate<ItemStack> check;
  private boolean finished = false;

  public LootContainerTask(BlockPos chestPos, List<Item> items, Predicate<ItemStack> pred) {
    this .containerPos = chestPos;
    this .targets = items;
    this .check = pred;
  }

  public LootContainerTask(BlockPos chestPos, List<Item> items) {
    this(chestPos, items, itemStack -> true);
  }

  @Override
  protected void onStart() {
    // Protect items we want to loot so we don't drop them
    controller.getBehaviour().push();
    controller.getBehaviour().addProtectedItems(targets.toArray(new Item[0]));
  }

  @Override
  protected Task onTick() {
    if (finished) {
      return null;
    }

    // Go to container
    if (!containerPos.isWithinDistance(new Vec3i((int) controller.getEntity().getPos().x, (int) controller.getEntity().getPos().y, (int) controller.getEntity().getPos().z), 4.5)) {
      setDebugState("Going to container");
      return new GetToBlockTask(containerPos);
    }

    // Get inventories
    BlockEntity be = controller.getWorld().getBlockEntity(containerPos);
    if (!(be instanceof LootableContainerBlockEntity container)) {
      Debug.logWarning("Block at " + containerPos + " is not a lootable container. Stopping.");
      finished = true;
      return null;
    }
    Inventory containerInventory = container;
    LivingEntityInventory playerInventory = ((IInventoryProvider) controller.getEntity()).getLivingInventory();

    // Update our cache of this container
    controller.getItemStorage().containers.WritableCache(controller, containerPos);

    boolean somethingToLoot = false;
    // Loot items
    setDebugState("Looting items: " + targets);
    for (int i = 0; i < containerInventory.size(); i++) {
      ItemStack stack = containerInventory.getStack(i);
      if (stack.isEmpty() || !targets.contains(stack.getItem()) || !check.test(stack)) {
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
      finished = true;
    }

    return null;
  }

  @Override
  public boolean isFinished() {
    // Additionally check if the container is gone
    if (finished || !controller.getChunkTracker().isChunkLoaded(containerPos) ||
            !(controller.getWorld().getBlockEntity(containerPos) instanceof LootableContainerBlockEntity)) {
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
      return Objects.equals(task .containerPos, containerPos) &&
              new ArrayList<>(task .targets).equals(new ArrayList<>(targets));
    }
    return false;
  }

  @Override
  protected String toDebugString() {
    return "Looting container at " + containerPos.toShortString();
  }
}