package adris.altoclef.trackers.storage;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.trackers.Tracker;
import adris.altoclef.trackers.TrackerManager;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.function.Predicate;

public class ContainerSubTracker extends Tracker {

  private final HashMap<Dimension, HashMap<BlockPos, ContainerCache>> _containerCaches = new HashMap<>();
  private BlockPos _lastInteractedContainer;

  public ContainerSubTracker(TrackerManager manager) {
    super(manager);
    for (Dimension dimension : Dimension.values()) {
      _containerCaches.put(dimension, new HashMap<>());
    }
  }

  /**
   * This method should be called by a task RIGHT BEFORE it interacts with a container.
   * It forces the tracker to update its cache for that specific container.
   */
  public Optional<ContainerCache> WritableCache(AltoClefController controller, BlockPos pos) {
    BlockEntity be = controller.getWorld().getBlockEntity(pos);
    if (be instanceof Inventory containerInventory) {
      _lastInteractedContainer = pos;
      Block block = controller.getWorld().getBlockState(pos).getBlock();
      ContainerType type = ContainerType.getFromBlock(block);
      if (type == ContainerType.EMPTY) {
        // Not a container we track
        return Optional.empty();
      }

      ContainerCache cache = _containerCaches.get(WorldHelper.getCurrentDimension(controller))
              .computeIfAbsent(pos, p -> new ContainerCache(WorldHelper.getCurrentDimension(controller), p, type));

      // Update cache from the real inventory
      cache.update(containerInventory, (s)->{});
      return Optional.of(cache);
    }
    _containerCaches.get(WorldHelper.getCurrentDimension(controller)).remove(pos);
    return Optional.empty();
  }

  public Optional<ContainerCache> getContainerAtPosition(BlockPos pos) {
    return Optional.ofNullable(_containerCaches.get(WorldHelper.getCurrentDimension(mod)).get(pos));
  }

  public List<ContainerCache> getCachedContainers(Predicate<ContainerCache> accept) {
    List<ContainerCache> result = new ArrayList<>();
    _containerCaches.get(WorldHelper.getCurrentDimension(mod)).values().forEach(cache -> {
      if (accept.test(cache)) {
        result.add(cache);
      }
    });
    return result;
  }

  public List<ContainerCache> getContainersWithItem(Item... items) {
    return getCachedContainers(cache -> cache.hasItem(items));
  }

  public Optional<BlockPos> getLastInteractedContainer() {
    return Optional.ofNullable(_lastInteractedContainer);
  }

  @Override
  protected void updateState() {
    // Updates are now task-driven via WritableCache.
  }

  @Override
  protected void reset() {
    _containerCaches.values().forEach(HashMap::clear);
    _lastInteractedContainer = null;
  }
}