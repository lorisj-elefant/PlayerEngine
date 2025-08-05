package adris.altoclef.tasks.container;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.mixins.MixinAbstractFurnaceBlockEntity;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasks.movement.GetCloseToBlockTask;
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

    private final SmeltTarget[] targets;
    private final TimerGame smeltTimer = new TimerGame(10);
    private BlockPos furnacePos = null;
    private boolean isSmelting = false;

    public SmeltInFurnaceTask(SmeltTarget... targets) {
        super(extractItemTargets(targets));
        this.targets = targets;
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
        controller.getBehaviour().push();
        controller.getBehaviour().addProtectedItems(Items.FURNACE);
        for (SmeltTarget target : targets) {
            controller.getBehaviour().addProtectedItems(target.getMaterial().getMatches());
        }
    }

    @Override
    protected Task onResourceTick(AltoClefController controller) {
        boolean allDone = Arrays.stream(targets).allMatch(target ->
                controller.getItemStorage().getItemCount(target.getItem()) >= target.getItem().getTargetCount()
        );
        if (allDone) {
            setDebugState("Done smelting.");
            return null;
        }

        SmeltTarget currentTarget = null;
        for (SmeltTarget target : targets) {
            if (controller.getItemStorage().getItemCount(target.getItem()) < target.getItem().getTargetCount()) {
                currentTarget = target;
                break;
            }
        }
        if (currentTarget == null) {
            Debug.logWarning("Smelting task is running, but all targets are met. This should not happen.");
            return null;
        }

        smeltTimer.setInterval(10 * currentTarget.getItem().getTargetCount());
        int fuelNeeded = (int) Math.ceil((double) currentTarget.getItem().getTargetCount() / 8d);
        if (!isSmelting) {
            if (controller.getItemStorage().getItemCount(currentTarget.getMaterial()) < currentTarget.getMaterial().getTargetCount()) {
                setDebugState("Collecting materials for smelting: " + currentTarget.getMaterial());
                return TaskCatalogue.getItemTask(currentTarget.getMaterial());
            }

            if (StorageHelper.calculateInventoryFuelCount(controller) < fuelNeeded) {
                setDebugState("Collecting fuel.");
                return new CollectFuelTask(fuelNeeded);
            }
        }
        if (furnacePos == null || !controller.getWorld().getBlockState(furnacePos).isOf(Blocks.FURNACE)) {
            Optional<BlockPos> nearestFurnace = controller.getBlockScanner().getNearestBlock(Blocks.FURNACE);
            if (nearestFurnace.isPresent()) {
                furnacePos = nearestFurnace.get();
            } else {
                if (controller.getItemStorage().hasItem(Items.FURNACE)) {
                    setDebugState("Placing furnace.");
                    return new PlaceBlockNearbyTask(Blocks.FURNACE);
                }
                setDebugState("Obtaining furnace.");
                return TaskCatalogue.getItemTask(Items.FURNACE, 1);
            }
        }

        if (!furnacePos.isWithinDistance(new Vec3i((int) controller.getEntity().getPos().x, (int) controller.getEntity().getPos().y, (int) controller.getEntity().getPos().z), 4.5)) {
            setDebugState("Going to furnace.");
            return new GetCloseToBlockTask(furnacePos);
        }

        BlockEntity be = controller.getWorld().getBlockEntity(furnacePos);
        if (!(be instanceof AbstractFurnaceBlockEntity furnace)) {
            Debug.logWarning("Block at furnace position is not a furnace BE. Resetting.");
            furnacePos = null;
            return new TimeoutWanderTask(1);
        }

        Inventory furnaceInventory = furnace;

        ItemStack outputStack = furnaceInventory.getStack(FurnaceSlot.OUTPUT_SLOT);
        if (!outputStack.isEmpty()) {
            setDebugState("Taking smelted items.");
            LivingEntityInventory playerInv = ((IInventoryProvider) controller.getEntity()).getLivingInventory();
            if (playerInv.insertStack(outputStack)) {
                furnaceInventory.setStack(FurnaceSlot.OUTPUT_SLOT, ItemStack.EMPTY);
                furnace.markDirty();
            } else {
                setDebugState("Inventory is full, cannot take smelted items.");
                return null;
            }
        }

        if (isSmelting) {
            setDebugState("Waiting for items to smelt...");
            if (smeltTimer.elapsed()) {
                isSmelting = false;
            }
            return null;
        }

        ItemStack materialSlot = furnaceInventory.getStack(FurnaceSlot.INPUT_SLOT_MATERIALS);
        ItemStack fuelSlot = furnaceInventory.getStack(FurnaceSlot.INPUT_SLOT_FUEL);
        LivingEntityInventory playerInv = ((IInventoryProvider) controller.getEntity()).getLivingInventory();

        if (((MixinAbstractFurnaceBlockEntity) furnace).getPropertyDelegate().get(0) <= 1 && fuelSlot.isEmpty()) {
            setDebugState("Adding fuel.");
            Item fuelItem = controller.getModSettings().getSupportedFuelItems()[0];
            int fuelSlotIndex = playerInv.getSlotWithStack(new ItemStack(fuelItem));
            if (fuelSlotIndex != -1) {
                furnaceInventory.setStack(FurnaceSlot.INPUT_SLOT_FUEL, playerInv.removeStack(fuelSlotIndex, fuelNeeded));
                furnace.markDirty();
                return null;
            }
        }

        if (materialSlot.isEmpty()) {
            setDebugState("Adding material.");
            Item materialItem = currentTarget.getMaterial().getMatches()[0];
            int materialSlotIndex = playerInv.getSlotWithStack(new ItemStack(materialItem));
            if (materialSlotIndex != -1) {
                furnaceInventory.setStack(FurnaceSlot.INPUT_SLOT_MATERIALS, playerInv.removeStack(materialSlotIndex, currentTarget.getMaterial().getTargetCount()));
                isSmelting = true;
                smeltTimer.reset();
                furnace.markDirty();
                return null;
            }
        }

        isSmelting = true;
        smeltTimer.reset();
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
            return Arrays.equals(task.targets, this.targets);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "Smelting in Furnace";
    }
}