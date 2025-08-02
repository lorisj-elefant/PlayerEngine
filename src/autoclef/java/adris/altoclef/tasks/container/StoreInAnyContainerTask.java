package adris.altoclef.tasks.container;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StoreInAnyContainerTask extends Task {

  private final ItemTarget[] toStore;
  private final boolean getIfNotPresent;

  public StoreInAnyContainerTask(boolean getIfNotPresent, ItemTarget... toStore) {
    this .getIfNotPresent = getIfNotPresent;
    this .toStore = toStore;
  }

  @Override
  protected void onStart() {
    // No specific setup needed.
  }

  @Override
  protected Task onTick() {
    ItemTarget[] itemsToStore = getItemsToStore(controller);
    if (itemsToStore.length == 0) {
      return null; // We are done
    }

    // Do we need to collect items first?
    if (getIfNotPresent) {
      for (ItemTarget target : toStore) {
        // Check against what we have in our inventory right now.
        if (controller.getItemStorage().getItemCount(target) < target.getTargetCount()) {
          setDebugState("Collecting " + target + " before storing.");
          return TaskCatalogue.getItemTask(target);
        }
      }
    }

    // Find a valid container
    Predicate<BlockPos> isValidContainer = pos -> {
      // Chest must be openable
      if (WorldHelper.isChest(controller, pos) && WorldHelper.isSolidBlock(controller, pos.up()) && !WorldHelper.canBreak(controller, pos.up())) {
        return false;
      }
      // Container must not be full
      Optional<ContainerCache> cache = controller.getItemStorage().getContainerAtPosition(pos);
      if (cache.isPresent() && cache.get().isFull()) {
        return false;
      }
      // Avoid dungeon chests if configured
      if (WorldHelper.isChest(controller, pos) && controller.getModSettings().shouldAvoidSearchingForDungeonChests()) {
        return !isDungeonChest(controller, pos);
      }
      return true;
    };

    Optional<BlockPos> closestContainer = controller.getBlockScanner().getNearestBlock(isValidContainer, StoreInContainerTask.CONTAINER_BLOCKS);

    // If a container is found, go and store items.
    if (closestContainer.isPresent()) {
      setDebugState("Found a container; storing items.");
      return new StoreInContainerTask(closestContainer.get(), false, itemsToStore);
    }

    // If no container found, place one.
    for (Block containerBlock : StoreInContainerTask.CONTAINER_BLOCKS) {
      if (controller.getItemStorage().hasItem(containerBlock.asItem())) {
        setDebugState("Placing a container nearby.");
        return new PlaceBlockNearbyTask(pos -> !WorldHelper.isChest(controller, pos) || WorldHelper.isAir(controller, pos.up()), containerBlock);
      }
    }

    // If we have no container to place, get one.
    setDebugState("Obtaining a chest to store items.");
    return TaskCatalogue.getItemTask(Items.CHEST, 1);
  }

  @Override
  public boolean isFinished() {
    return getItemsToStore(controller).length == 0;
  }

  private ItemTarget[] getItemsToStore(AltoClefController controller) {
    // Return a list of items that the player has and wants to store.
    return Arrays.stream(toStore)
            .filter(target -> controller.getItemStorage().hasItem(target.getMatches()))
            .toArray(ItemTarget[]::new);
  }

  private boolean isDungeonChest(AltoClefController controller, BlockPos pos) {
    // Simple check for a spawner nearby
    int range = 6;
    for (int dx = -range; dx <= range; dx++) {
      for (int dz = -range; dz <= range; dz++) {
        if (controller.getWorld().getBlockState(pos.add(dx, 0, dz)).isOf(Blocks.SPAWNER)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void onStop(Task interruptTask) {
    // Nothing to do.
  }

  @Override
  protected boolean isEqual(Task other) {
    if (other instanceof StoreInAnyContainerTask task) {
      return task .getIfNotPresent == this .getIfNotPresent && Arrays.equals(task .toStore, this .toStore);
    }
    return false;
  }

  @Override
  protected String toDebugString() {
    return "Storing in any container: " + Arrays.toString(toStore);
  }
}