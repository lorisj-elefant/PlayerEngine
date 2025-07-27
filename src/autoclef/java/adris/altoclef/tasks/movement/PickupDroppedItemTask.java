//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasks.AbstractDoToClosestObjectTask;
import adris.altoclef.tasks.resources.SatisfyMiningRequirementTask;
import adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StlHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PickupDroppedItemTask extends AbstractDoToClosestObjectTask<ItemEntity> implements ITaskRequiresGrounded {
    private static final Task getPickaxeFirstTask;
    private static boolean isGettingPickaxeFirstFlag;
    private final TimeoutWanderTask wanderTask;
    private final MovementProgressChecker stuckCheck;
    private final MovementProgressChecker progressChecker;
    private final ItemTarget[] itemTargets;
    private final Set<ItemEntity> _blacklist;
    private final boolean _freeInventoryIfFull;
    Block[] annoyingBlocks;
    private Task unstuckTask;
    private AltoClefController _mod;
    private boolean _collectingPickaxeForThisResource;
    private ItemEntity _currentDrop;

    public PickupDroppedItemTask(ItemTarget[] itemTargets, boolean freeInventoryIfFull) {
        this.wanderTask = new TimeoutWanderTask(5.0F, true);
        this.stuckCheck = new MovementProgressChecker();
        this.progressChecker = new MovementProgressChecker();
        this._blacklist = new HashSet();
        this.annoyingBlocks = new Block[]{Blocks.VINE, Blocks.NETHER_SPROUTS, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WEEPING_VINES_PLANT, Blocks.LADDER, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.SMALL_DRIPLEAF, Blocks.TALL_GRASS, Blocks.GRASS};
        this.unstuckTask = null;
        this._collectingPickaxeForThisResource = false;
        this._currentDrop = null;
        this.itemTargets = itemTargets;
        this._freeInventoryIfFull = freeInventoryIfFull;
    }

    public PickupDroppedItemTask(ItemTarget target, boolean freeInventoryIfFull) {
        this(new ItemTarget[]{target}, freeInventoryIfFull);
    }

    public PickupDroppedItemTask(Item item, int targetCount, boolean freeInventoryIfFull) {
        this(new ItemTarget(item, targetCount), freeInventoryIfFull);
    }

    public PickupDroppedItemTask(Item item, int targetCount) {
        this(item, targetCount, true);
    }

    private static BlockPos[] generateSides(BlockPos pos) {
        return new BlockPos[]{pos.add(1, 0, 0), pos.add(-1, 0, 0), pos.add(0, 0, 1), pos.add(0, 0, -1), pos.add(1, 0, -1), pos.add(1, 0, 1), pos.add(-1, 0, -1), pos.add(-1, 0, 1)};
    }

    public static boolean isIsGettingPickaxeFirst(AltoClefController mod) {
        return isGettingPickaxeFirstFlag && mod.getModSettings().shouldCollectPickaxeFirst();
    }

    private boolean isAnnoying(AltoClefController mod, BlockPos pos) {
        if (this.annoyingBlocks != null) {
            Block[] var3 = this.annoyingBlocks;
            int var4 = var3.length;
            byte var5 = 0;
            if (var5 < var4) {
                Block AnnoyingBlocks = var3[var5];
                return mod.getWorld().getBlockState(pos).getBlock() == AnnoyingBlocks || mod.getWorld().getBlockState(pos).getBlock() instanceof DoorBlock || mod.getWorld().getBlockState(pos).getBlock() instanceof FenceBlock || mod.getWorld().getBlockState(pos).getBlock() instanceof FenceGateBlock || mod.getWorld().getBlockState(pos).getBlock() instanceof FlowerBlock;
            }
        }

        return false;
    }

    private BlockPos stuckInBlock(AltoClefController mod) {
        BlockPos p = mod.getPlayer().getBlockPos();
        if (this.isAnnoying(mod, p)) {
            return p;
        } else if (this.isAnnoying(mod, p.up())) {
            return p.up();
        } else {
            BlockPos[] toCheck = generateSides(p);

            for(BlockPos check : toCheck) {
                if (this.isAnnoying(mod, check)) {
                    return check;
                }
            }

            BlockPos[] toCheckHigh = generateSides(p.up());

            for(BlockPos check : toCheckHigh) {
                if (this.isAnnoying(mod, check)) {
                    return check;
                }
            }

            return null;
        }
    }

    private Task getFenceUnstuckTask() {
        return new SafeRandomShimmyTask();
    }

    public boolean isCollectingPickaxeForThis() {
        return this._collectingPickaxeForThisResource;
    }

    protected void onStart() {
        this.wanderTask.reset();
        this.progressChecker.reset();
        this.stuckCheck.reset();
    }

    protected void onStop(Task interruptTask) {
    }

    protected Task onTick() {
        if (this.wanderTask.isActive() && !this.wanderTask.isFinished()) {
            this.setDebugState("Wandering.");
            return this.wanderTask;
        } else {
            AltoClefController mod = controller;
            if (mod.getBaritone().getPathingBehavior().isPathing()) {
                this.progressChecker.reset();
            }

            if (this.unstuckTask != null && this.unstuckTask.isActive() && !this.unstuckTask.isFinished() && this.stuckInBlock(mod) != null) {
                this.setDebugState("Getting unstuck from block.");
                this.stuckCheck.reset();
                mod.getBaritone().getCustomGoalProcess().onLostControl();
                mod.getBaritone().getExploreProcess().onLostControl();
                return this.unstuckTask;
            } else {
                if (!this.progressChecker.check(mod) || !this.stuckCheck.check(mod)) {
                    BlockPos blockStuck = this.stuckInBlock(mod);
                    if (blockStuck != null) {
                        this.unstuckTask = this.getFenceUnstuckTask();
                        return this.unstuckTask;
                    }

                    this.stuckCheck.reset();
                }

                this._mod = mod;
                if (isIsGettingPickaxeFirst(mod) && this._collectingPickaxeForThisResource && !StorageHelper.miningRequirementMetInventory(controller, MiningRequirement.STONE)) {
                    this.progressChecker.reset();
                    this.setDebugState("Collecting pickaxe first");
                    return getPickaxeFirstTask;
                } else {
                    if (StorageHelper.miningRequirementMetInventory(controller, MiningRequirement.STONE)) {
                        isGettingPickaxeFirstFlag = false;
                    }

                    this._collectingPickaxeForThisResource = false;
                    if (!this.progressChecker.check(mod)) {
                        mod.getBaritone().getPathingBehavior().forceCancel();
                        if (this._currentDrop != null && !this._currentDrop.getStack().isEmpty()) {
                            if (!isGettingPickaxeFirstFlag && mod.getModSettings().shouldCollectPickaxeFirst() && !StorageHelper.miningRequirementMetInventory(controller, MiningRequirement.STONE)) {
                                Debug.logMessage("Failed to pick up drop, will try to collect a stone pickaxe first and try again!");
                                this._collectingPickaxeForThisResource = true;
                                isGettingPickaxeFirstFlag = true;
                                return getPickaxeFirstTask;
                            }

                            Debug.logMessage(StlHelper.toString(this._blacklist, (element) -> element == null ? "(null)" : element.getStack().getItem().getTranslationKey()));
                            Debug.logMessage("Failed to pick up drop, suggesting it's unreachable.");
                            this._blacklist.add(this._currentDrop);
                            mod.getEntityTracker().requestEntityUnreachable(this._currentDrop);
                            return this.wanderTask;
                        }
                    }

                    return super.onTick();
                }
            }
        }
    }

    protected boolean isEqual(Task other) {
        if (!(other instanceof PickupDroppedItemTask task)) {
            return false;
        } else {
            return Arrays.equals(task.itemTargets, this.itemTargets) && task._freeInventoryIfFull == this._freeInventoryIfFull;
        }
    }

    protected String toDebugString() {
        StringBuilder result = new StringBuilder();
        result.append("Pickup Dropped Items: [");
        int c = 0;

        for(ItemTarget target : this.itemTargets) {
            result.append(target.toString());
            ++c;
            if (c != this.itemTargets.length) {
                result.append(", ");
            }
        }

        result.append("]");
        return result.toString();
    }

    protected Vec3d getPos(AltoClefController mod, ItemEntity obj) {
        if (!obj.isOnGround() && !obj.isTouchingWater()) {
            BlockPos p = obj.getBlockPos();
            return !WorldHelper.isSolidBlock(controller, p.down(3)) ? obj.getPos().subtract((double)0.0F, (double)2.0F, (double)0.0F) : obj.getPos().subtract((double)0.0F, (double)1.0F, (double)0.0F);
        } else {
            return obj.getPos();
        }
    }

    protected Optional<ItemEntity> getClosestTo(AltoClefController mod, Vec3d pos) {
        return mod.getEntityTracker().getClosestItemDrop(pos, this.itemTargets);
    }

    protected Vec3d getOriginPos(AltoClefController mod) {
        return mod.getPlayer().getPos();
    }

    protected Task getGoalTask(ItemEntity itemEntity) {
        if (!itemEntity.equals(this._currentDrop)) {
            this._currentDrop = itemEntity;
            this.progressChecker.reset();
            if (isGettingPickaxeFirstFlag && this._collectingPickaxeForThisResource) {
                Debug.logMessage("New goal, no longer collecting a pickaxe.");
                this._collectingPickaxeForThisResource = false;
                isGettingPickaxeFirstFlag = false;
            }
        }

        boolean touching = this._mod.getEntityTracker().isCollidingWithPlayer(itemEntity);
        return (Task)(touching && this._freeInventoryIfFull && this._mod.getItemStorage().getSlotsThatCanFitInPlayerInventory(itemEntity.getStack(), false).isEmpty() ? new EnsureFreeInventorySlotTask() : new GetToEntityTask(itemEntity));
    }

    protected boolean isValid(AltoClefController mod, ItemEntity obj) {
        return obj.isAlive() && !this._blacklist.contains(obj);
    }

    static {
        getPickaxeFirstTask = new SatisfyMiningRequirementTask(MiningRequirement.STONE);
        isGettingPickaxeFirstFlag = false;
    }
}
