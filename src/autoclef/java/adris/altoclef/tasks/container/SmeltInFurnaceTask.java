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
import adris.altoclef.util.slots.FurnaceSlot;
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

public class SmeltInFurnaceTask extends ResourceTask {

  private final SmeltTarget[] _targets;
  private final TimerGame _smeltTimer = new TimerGame(10);
  private BlockPos _furnacePos = null;
  private boolean _isSmelting = false;

  public SmeltInFurnaceTask(SmeltTarget... targets) {
    super(extractItemTargets(targets));
    _targets = targets;
  }

  public SmeltInFurnaceTask(SmeltTarget target) {
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
    controller.getBehaviour().addProtectedItems(Items.FURNACE);
    for (SmeltTarget target : _targets) {
      controller.getBehaviour().addProtectedItems(target.getMaterial().getMatches());
    }
  }

  @Override
  protected Task onResourceTick(AltoClefController controller) {
    // Check if all smelting is done.
    boolean allDone = Arrays.stream(_targets).allMatch(target ->
            controller.getItemStorage().getItemCount(target.getItem()) >= target.getItem().getTargetCount()
    );
    if (allDone) {
      setDebugState("Done smelting.");
      return null;
    }

    // 3. Find a target to smelt
    SmeltTarget currentTarget = null;
    for (SmeltTarget target : _targets) {
      if (controller.getItemStorage().getItemCount(target.getItem()) < target.getItem().getTargetCount()) {
        currentTarget = target;
        break;
      }
    }
    if (currentTarget == null) {
      Debug.logWarning("Smelting task is running, but all targets are met. This should not happen.");
      return null;
    }

    // 4. Ensure we have materials
    if (!controller.getItemStorage().hasItem(currentTarget.getMaterial())) {
      setDebugState("Collecting materials for smelting: " + currentTarget.getMaterial());
      return TaskCatalogue.getItemTask(currentTarget.getMaterial());
    }

    // 5. Ensure we have fuel
    double fuelNeeded = 1; // Simplification: 1 fuel per operation
    if (StorageHelper.calculateInventoryFuelCount(controller) < fuelNeeded) {
      setDebugState("Collecting fuel.");
      return new CollectFuelTask(fuelNeeded);
    }

    // Find or place a furnace.
    if (_furnacePos == null || !controller.getWorld().getBlockState(_furnacePos).isOf(Blocks.FURNACE)) {
      Optional<BlockPos> nearestFurnace = controller.getBlockScanner().getNearestBlock(Blocks.FURNACE);
      if (nearestFurnace.isPresent()) {
        _furnacePos = nearestFurnace.get();
      } else {
        if (controller.getItemStorage().hasItem(Items.FURNACE)) {
          setDebugState("Placing furnace.");
          return new PlaceBlockNearbyTask(Blocks.FURNACE);
        }
        setDebugState("Obtaining furnace.");
        return TaskCatalogue.getItemTask(Items.FURNACE, 1);
      }
    }

    // Go to the furnace.
    if (!_furnacePos.isWithinDistance(new Vec3i((int) controller.getEntity().getPos().x, (int) controller.getEntity().getPos().y, (int) controller.getEntity().getPos().z), 4.5)) {
      setDebugState("Going to furnace.");
      return new GetToBlockTask(_furnacePos);
    }

    // Interact with furnace (server-side simulation)
    BlockEntity be = controller.getWorld().getBlockEntity(_furnacePos);
    if (!(be instanceof AbstractFurnaceBlockEntity furnace)) {
      Debug.logWarning("Block at furnace position is not a furnace BE. Resetting.");
      _furnacePos = null;
      return new TimeoutWanderTask(1);
    }

    Inventory furnaceInventory = furnace;

    // 1. Take out results if any
    ItemStack outputStack = furnaceInventory.getStack(FurnaceSlot.OUTPUT_SLOT);
    if (!outputStack.isEmpty()) {
      setDebugState("Taking smelted items.");
      LivingEntityInventory playerInv = ((IInventoryProvider) controller.getEntity()).getLivingInventory();
      if (playerInv.insertStack(outputStack)) {
        furnaceInventory.setStack(FurnaceSlot.OUTPUT_SLOT, ItemStack.EMPTY);
        furnace.markDirty();
      } else {
        setDebugState("Inventory is full, cannot take smelted items.");
        return null; // Or a task to clear inventory
      }
    }

    // 2. We are smelting something, wait.
    if (_isSmelting) {
      setDebugState("Waiting for items to smelt...");
      if (_smeltTimer.elapsed()) {
        _isSmelting = false;
      }
      return null;
    }



    // 6. Put items into furnace
    ItemStack materialSlot = furnaceInventory.getStack(FurnaceSlot.INPUT_SLOT_MATERIALS);
    ItemStack fuelSlot = furnaceInventory.getStack(FurnaceSlot.INPUT_SLOT_FUEL);
    LivingEntityInventory playerInv = ((IInventoryProvider) controller.getEntity()).getLivingInventory();

    // Put in fuel
    if (((MixinAbstractFurnaceBlockEntity)furnace).getPropertyDelegate().get(0) <= 1 && fuelSlot.isEmpty()) { // burnTime is property 0
      setDebugState("Adding fuel.");
      Item fuelItem = controller.getModSettings().getSupportedFuelItems()[0]; // Just grab first available
      int fuelSlotIndex = playerInv.getSlotWithStack(new ItemStack(fuelItem));
      if (fuelSlotIndex != -1) {
        furnaceInventory.setStack(FurnaceSlot.INPUT_SLOT_FUEL, playerInv.removeStack(fuelSlotIndex, 1));
        furnace.markDirty();
        return null;
      }
    }

    // Put in material
    if (materialSlot.isEmpty()) {
      setDebugState("Adding material.");
      Item materialItem = currentTarget.getMaterial().getMatches()[0];
      int materialSlotIndex = playerInv.getSlotWithStack(new ItemStack(materialItem));
      if (materialSlotIndex != -1) {
        furnaceInventory.setStack(FurnaceSlot.INPUT_SLOT_MATERIALS, playerInv.removeStack(materialSlotIndex, 1));
        _isSmelting = true;
        _smeltTimer.reset();
        furnace.markDirty();
        return null;
      }
    }

    // If we are here, it means we are waiting for something to happen inside the furnace.
    _isSmelting = true;
    _smeltTimer.reset();
    setDebugState("Waiting for furnace...");
    return null;
  }

  @Override
  protected void onResourceStop(AltoClefController controller, Task interruptTask) {
    controller.getBehaviour().pop();
  }

  @Override
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof SmeltInFurnaceTask task) {
      return Arrays.equals(task._targets, this._targets);
    }
    return false;
  }

  @Override
  protected String toDebugStringName() {
    return "Smelting in Furnace";
  }
}