package adris.altoclef.tasks.container;

import adris.altoclef.AltoClefController;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.util.BlockRange;
import adris.altoclef.util.ItemTarget;

import java.util.Arrays;
import java.util.Optional;

import net.minecraft.util.math.BlockPos;

public class StoreInStashTask extends Task {

  private final ItemTarget[] _toStore;
  private final boolean _getIfNotPresent;
  private final BlockRange _stashRange;

  public StoreInStashTask(boolean getIfNotPresent, BlockRange stashRange, ItemTarget... toStore) {
    this._getIfNotPresent = getIfNotPresent;
    this._stashRange = stashRange;
    this._toStore = toStore;
  }

  @Override
  protected void onStart() {
    // No specific setup needed.
  }

  @Override
  protected Task onTick() {
    ItemTarget[] itemsToStore = getItemsToStore(controller);
    if (itemsToStore.length == 0) {
      return null; // We are done.
    }

    // Do we need to collect items first?
    if (_getIfNotPresent) {
      for (ItemTarget target : _toStore) {
        if (controller.getItemStorage().getItemCount(target) < target.getTargetCount()) {
          setDebugState("Collecting " + target + " before stashing.");
          return TaskCatalogue.getItemTask(target);
        }
      }
    }

    // Find a valid container within the stash range.
    Optional<BlockPos> closestContainer = controller.getBlockScanner().getNearestBlock(
            pos -> {
              if (!_stashRange.contains(controller, pos)) return false;
              Optional<ContainerCache> cache = controller.getItemStorage().getContainerAtPosition(pos);
              // Valid if it's not full.
              return cache.map(containerCache -> !containerCache.isFull()).orElse(true);
            },
            StoreInContainerTask.CONTAINER_BLOCKS
    );

    // If a container is found in the stash, use it.
    if (closestContainer.isPresent()) {
      setDebugState("Storing in closest stash container.");
      return new StoreInContainerTask(closestContainer.get(), false, itemsToStore);
    }

    // If we are not in the stash area, go there.
    if (!_stashRange.contains(controller, controller.getEntity().getBlockPos())) {
      setDebugState("Traveling to stash area.");
      BlockPos centerStash = _stashRange.getCenter();
      return new GetToXZTask(centerStash.getX(), centerStash.getZ());
    }

    // We are in the stash area, but no non-full container was found.
    // The task should probably end here, as it can't fulfill its goal.
    setDebugState("Inside stash, but no non-full containers found. Cannot store items.");
    return null;
  }

  @Override
  public boolean isFinished() {
    return getItemsToStore(controller).length == 0;
  }

  private ItemTarget[] getItemsToStore(AltoClefController controller) {
    return Arrays.stream(_toStore)
            .filter(target -> controller.getItemStorage().hasItem(target.getMatches()))
            .toArray(ItemTarget[]::new);
  }

  @Override
  protected void onStop(Task interruptTask) {
    // Nothing to do.
  }

  @Override
  protected boolean isEqual(Task other) {
    if (other instanceof StoreInStashTask task) {
      return task._stashRange.equals(this._stashRange) &&
              task._getIfNotPresent == this._getIfNotPresent &&
              Arrays.equals(task._toStore, this._toStore);
    }
    return false;
  }

  @Override
  protected String toDebugString() {
    return "Storing in stash " + _stashRange + ": " + Arrays.toString(_toStore);
  }
}