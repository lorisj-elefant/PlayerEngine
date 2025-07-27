// File: adris/altoclef/tasks/container/SmeltInSmokerTask.java
package adris.altoclef.tasks.container;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.mixins.MixinAbstractFurnaceBlockEntity;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.resources.CollectFuelTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.SmeltTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.SmokerSlot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.entity.IInventoryProvider;
import baritone.api.entity.LivingEntityInventory;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SmeltInSmokerTask extends ResourceTask {

  private final SmeltTarget[] _targets;
  private final TimerGame _smeltTimer = new TimerGame(5); // 5 секунд на плавку в коптильне
  private BlockPos _smokerPos = null;
  private boolean _isSmelting = false;

  public SmeltInSmokerTask(SmeltTarget... targets) {
    super(extractItemTargets(targets));
    _targets = targets;
  }

  public SmeltInSmokerTask(SmeltTarget target) {
    this(new SmeltTarget[]{target});
  }

  private static ItemTarget[] extractItemTargets(SmeltTarget[] recipeTargets) {
    List<ItemTarget> result = new ArrayList<>(recipeTargets.length);
    for (SmeltTarget target : recipeTargets) {
      result.add(target.getItem());
    }
    return result.toArray(ItemTarget[]::new);
  }

  @Override
  protected boolean shouldAvoidPickingUp(AltoClefController controller) {
    return false;
  }

  @Override
  protected void onResourceStart(AltoClefController controller) {
    controller.getBehaviour().addProtectedItems(Items.SMOKER);
    for (SmeltTarget target : _targets) {
      controller.getBehaviour().addProtectedItems(target.getMaterial().getMatches());
    }
  }

  @Override
  protected Task onResourceTick(AltoClefController controller) {
    boolean allDone = Arrays.stream(_targets).allMatch(target ->
            controller.getItemStorage().getItemCount(target.getItem()) >= target.getItem().getTargetCount()
    );
    if (allDone) {
      setDebugState("Done smoking.");
      return null;
    }

    if (_smokerPos == null || !controller.getWorld().getBlockState(_smokerPos).isOf(Blocks.SMOKER)) {
      Optional<BlockPos> nearestSmoker = controller.getBlockScanner().getNearestBlock(Blocks.SMOKER);
      if (nearestSmoker.isPresent()) {
        _smokerPos = nearestSmoker.get();
      } else {
        if (controller.getItemStorage().hasItem(Items.SMOKER)) {
          setDebugState("Placing smoker.");
          return new PlaceBlockNearbyTask(Blocks.SMOKER);
        }
        setDebugState("Obtaining smoker.");
        return TaskCatalogue.getItemTask(Items.SMOKER, 1);
      }
    }

    if (!_smokerPos.isWithinDistance(new Vec3i((int) controller.getEntity().getPos().x, (int) controller.getEntity().getPos().y, (int) controller.getEntity().getPos().z), 4.5)) {
      setDebugState("Going to smoker.");
      return new GetToBlockTask(_smokerPos);
    }

    BlockEntity be = controller.getWorld().getBlockEntity(_smokerPos);
    if (!(be instanceof AbstractFurnaceBlockEntity smoker)) {
      Debug.logWarning("Block at smoker position is not a smoker BE. Resetting.");
      _smokerPos = null;
      return new TimeoutWanderTask(1);
    }

    Inventory smokerInventory = smoker;

    ItemStack outputStack = smokerInventory.getStack(SmokerSlot.OUTPUT_SLOT);
    if (!outputStack.isEmpty()) {
      setDebugState("Taking smoked items.");
      LivingEntityInventory playerInv = ((IInventoryProvider) controller.getEntity()).getLivingInventory();
      if (playerInv.insertStack(outputStack)) {
        smokerInventory.setStack(SmokerSlot.OUTPUT_SLOT, ItemStack.EMPTY);
        smoker.markDirty();
      } else {
        setDebugState("Inventory full.");
        return null;
      }
    }

    if (_isSmelting) {
      setDebugState("Waiting for items to smoke...");
      if (_smeltTimer.elapsed()) {
        _isSmelting = false;
      }
      return null;
    }

    SmeltTarget currentTarget = Arrays.stream(_targets).filter(t -> controller.getItemStorage().getItemCount(t.getItem()) < t.getItem().getTargetCount()).findFirst().orElse(null);
    if (currentTarget == null) return null;

    if (!controller.getItemStorage().hasItem(currentTarget.getMaterial())) {
      setDebugState("Collecting raw food: " + currentTarget.getMaterial());
      return TaskCatalogue.getItemTask(currentTarget.getMaterial());
    }

    if (((MixinAbstractFurnaceBlockEntity)smoker).getPropertyDelegate().get(0) <= 0 && StorageHelper.calculateInventoryFuelCount(controller) < 1) {
      setDebugState("Collecting fuel.");
      return new CollectFuelTask(1.0);
    }

    LivingEntityInventory playerInv = ((IInventoryProvider) controller.getEntity()).getLivingInventory();

    if (((MixinAbstractFurnaceBlockEntity)smoker).getPropertyDelegate().get(0) <= 1 && smokerInventory.getStack(SmokerSlot.INPUT_SLOT_FUEL).isEmpty()) {
      setDebugState("Adding fuel.");
      Item fuelItem = controller.getModSettings().getSupportedFuelItems()[0];
      int fuelSlotIndex = playerInv.getSlotWithStack(new ItemStack(fuelItem));
      if (fuelSlotIndex != -1) {
        smokerInventory.setStack(SmokerSlot.INPUT_SLOT_FUEL, playerInv.removeStack(fuelSlotIndex, 1));
        smoker.markDirty();
        return null;
      }
    }

    if (smokerInventory.getStack(SmokerSlot.INPUT_SLOT_MATERIALS).isEmpty()) {
      setDebugState("Adding raw food.");
      Item materialItem = currentTarget.getMaterial().getMatches()[0];
      int materialSlotIndex = playerInv.getSlotWithStack(new ItemStack(materialItem));
      if (materialSlotIndex != -1) {
        smokerInventory.setStack(SmokerSlot.INPUT_SLOT_MATERIALS, playerInv.removeStack(materialSlotIndex, 1));
        _isSmelting = true;
        _smeltTimer.reset();
        smoker.markDirty();
        return null;
      }
    }

    _isSmelting = true;
    _smeltTimer.reset();
    setDebugState("Waiting for smoker...");
    return null;
  }

  @Override
  protected void onResourceStop(AltoClefController controller, Task interruptTask) {
    controller.getBehaviour().pop();
  }

  @Override
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof SmeltInSmokerTask task) {
      return Arrays.equals(task._targets, this._targets);
    }
    return false;
  }

  @Override
  protected String toDebugStringName() {
    return "Smelting in Smoker";
  }
}