package adris.altoclef.tasks;

import adris.altoclef.AltoClefController;
import adris.altoclef.BotBehaviour;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.container.PickupFromContainerTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.resources.MineAndCollectTask;
import adris.altoclef.tasksystem.ITaskCanForce;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StlHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class ResourceTask extends Task implements ITaskCanForce {

  protected final ItemTarget[] _itemTargets;

  private final PickupDroppedItemTask _pickupTask;

  private Block[] mineIfPresent = null;
  private BlockPos mineLastClosest = null;
  // Not all resource tasks need these, but they are common.
  private boolean _forceDimension = false;
  private Dimension _targetDimension;
  private ContainerCache currentContainer;
  private boolean allowContainers = false;

  public ResourceTask(ItemTarget... itemTargets) {
    this._itemTargets = itemTargets;
    this._pickupTask = new PickupDroppedItemTask(this._itemTargets, true);
  }

  public ResourceTask(Item item, int targetCount) {
    this(new ItemTarget(item, targetCount));
  }

  @Override
  public boolean isFinished() {
    return StorageHelper.itemTargetsMet(controller, _itemTargets);
  }

  @Override
  public boolean shouldForce(Task interruptingCandidate) {
    // If we have items on cursor and they are our target, don't get interrupted.
    if (StorageHelper.itemTargetsMet(controller, _itemTargets) && !isFinished()) {
      ItemStack cursorStack = controller.getSlotHandler().getCursorStack();
      return Arrays.stream(_itemTargets).anyMatch(target -> target.matches(cursorStack.getItem()));
    }
    return false;
  }

  @Override
  protected void onStart() {
    BotBehaviour botBehaviour = controller.getBehaviour();
    botBehaviour.push();
    botBehaviour.addProtectedItems(ItemTarget.getMatches(_itemTargets));
    onResourceStart(controller);
  }

  @Override
  protected Task onTick() {
    AltoClefController mod = controller;
    if (isFinished()) {
      return null;
    }

    // If items are on the ground, pick them up.
    if (!shouldAvoidPickingUp(mod)) {
      // Check if items are on the floor. If so, pick em up.
      if (mod.getEntityTracker().itemDropped(_itemTargets)) {

        // If we're picking up a pickaxe (we can't go far underground or mine much)
        if (PickupDroppedItemTask.isIsGettingPickaxeFirst(mod)) {
          if (_pickupTask.isCollectingPickaxeForThis()) {
            setDebugState("Picking up (pickaxe first!)");
            // Our pickup task is the one collecting the pickaxe, keep it going.
            return _pickupTask;
          }
          // Only get items that are CLOSE to us.
          Optional<ItemEntity> closest = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), _itemTargets);
          if (closest.isPresent() && !closest.get().isInRange(mod.getPlayer(), 10)) {
            return onResourceTick(mod);
          }
        }

        double range = getPickupRange(mod);
        Optional<ItemEntity> closest = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), _itemTargets);
        if (range < 0 || (closest.isPresent() && closest.get().isInRange(mod.getPlayer(), range)) || (_pickupTask.isActive() && !_pickupTask.isFinished())) {
          setDebugState("Picking up");
          return _pickupTask;
        }
      }
    }

    // Check for chests and grab resources from them.
    if (currentContainer == null && allowContainers) {
      List<ContainerCache> containersWithItem = mod.getItemStorage().getContainersWithItem(Arrays.stream(_itemTargets).reduce(new Item[0], (items, target) -> ArrayUtils.addAll(items, target.getMatches()), ArrayUtils::addAll));
      if (!containersWithItem.isEmpty()) {
        ContainerCache closest = containersWithItem.stream().min(StlHelper.compareValues(container -> BlockPosVer.getSquaredDistance(container.getBlockPos(),mod.getPlayer().getPos()))).get();
        if (closest.getBlockPos().isWithinDistance(new Vec3i((int) mod.getPlayer().getPos().x, (int) mod.getPlayer().getPos().y, (int) mod.getPlayer().getPos().z), mod.getModSettings().getResourceChestLocateRange())) {
          currentContainer = closest;
        }
      }
    }
    if (currentContainer != null) {
      Optional<ContainerCache> container = mod.getItemStorage().getContainerAtPosition(currentContainer.getBlockPos());
      if (container.isPresent()) {
        if (Arrays.stream(_itemTargets).noneMatch(target -> container.get().hasItem(target.getMatches()))) {
          currentContainer = null;
        } else {
          // We have a current chest, grab from it.
          setDebugState("Picking up from container");
          return new PickupFromContainerTask(currentContainer.getBlockPos(), _itemTargets);
        }
      } else {
        currentContainer = null;
      }
    }


    // We may just mine if a block is found.
    if (mineIfPresent != null) {
      ArrayList<Block> satisfiedReqs = new ArrayList<>(Arrays.asList(mineIfPresent));
      satisfiedReqs.removeIf(block -> !StorageHelper.miningRequirementMet(mod, MiningRequirement.getMinimumRequirementForBlock(block)));
      if (!satisfiedReqs.isEmpty()) {
        if (mod.getBlockScanner().anyFound(satisfiedReqs.toArray(Block[]::new))) {
          Optional<BlockPos> closest = mod.getBlockScanner().getNearestBlock(mineIfPresent);
          if (closest.isPresent() && closest.get().isWithinDistance(new Vec3i((int) mod.getPlayer().getPos().x, (int) mod.getPlayer().getPos().y, (int) mod.getPlayer().getPos().z), mod.getModSettings().getResourceMineRange())) {
            mineLastClosest = closest.get();
          }
          if (mineLastClosest != null) {
            if (mineLastClosest.isWithinDistance(new Vec3i((int) mod.getPlayer().getPos().x, (int) mod.getPlayer().getPos().y, (int) mod.getPlayer().getPos().z), mod.getModSettings().getResourceMineRange() * 1.5 + 20)) {
              return new MineAndCollectTask(_itemTargets, mineIfPresent, MiningRequirement.HAND);
            }
          }
        }
      }
    }

    // Dimension check
    if (isInWrongDimension(controller)) {
      setDebugState("Traveling to correct dimension");
      return getToCorrectDimensionTask(controller);
    }

    return onResourceTick(controller);
  }

  private boolean isPickupTaskValid(AltoClefController controller) {
    double range = getPickupRange(controller);
    if (range < 0) return true;
    return controller.getEntityTracker()
            .getClosestItemDrop(controller.getEntity().getPos(), _itemTargets)
            .map(itemEntity -> itemEntity.isInRange(controller.getEntity(), range) || (_pickupTask.isActive() && !_pickupTask.isFinished()))
            .orElse(false);
  }

  protected double getPickupRange(AltoClefController controller) {
    return controller.getModSettings().getResourcePickupRange();
  }

  @Override
  protected void onStop(Task interruptTask) {
    controller.getBehaviour().pop();
    onResourceStop(controller, interruptTask);
  }

  @Override
  protected boolean isEqual(Task other) {
    if (other instanceof ResourceTask task) {
      return Arrays.equals(task._itemTargets, this._itemTargets) && isEqualResource(task);
    }
    return false;
  }

  @Override
  protected String toDebugString() {
    return toDebugStringName() + ": " + Arrays.toString(_itemTargets);
  }

  protected boolean isInWrongDimension(AltoClefController controller) {
    if (_forceDimension) {
      return WorldHelper.getCurrentDimension(controller) != _targetDimension;
    }
    return false;
  }

  protected Task getToCorrectDimensionTask(AltoClefController controller) {
    return new DefaultGoToDimensionTask(_targetDimension);
  }

  public ResourceTask forceDimension(Dimension dimension) {
    _forceDimension = true;
    _targetDimension = dimension;
    return this;
  }

  public ItemTarget[] getItemTargets() {
    return _itemTargets;
  }

  public ResourceTask mineIfPresent(Block[] toMine) {
    mineIfPresent = toMine;
    return this;
  }

  protected abstract boolean shouldAvoidPickingUp(AltoClefController controller);

  protected abstract void onResourceStart(AltoClefController controller);

  protected abstract Task onResourceTick(AltoClefController controller);

  protected abstract void onResourceStop(AltoClefController controller, Task interruptTask);

  protected abstract boolean isEqualResource(ResourceTask other);

  protected abstract String toDebugStringName();
}