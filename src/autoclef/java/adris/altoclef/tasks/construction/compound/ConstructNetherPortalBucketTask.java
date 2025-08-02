package adris.altoclef.tasks.construction.compound;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.ClearLiquidTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceObsidianBucketTask;
import adris.altoclef.tasks.movement.GetWithinRangeOfBlockTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

public class ConstructNetherPortalBucketTask extends Task {
    private static final Vec3i[] PORTAL_FRAME = new Vec3i[]{new Vec3i(0, 0, -1), new Vec3i(0, 1, -1), new Vec3i(0, 2, -1), new Vec3i(0, 0, 2), new Vec3i(0, 1, 2), new Vec3i(0, 2, 2), new Vec3i(0, 3, 0), new Vec3i(0, 3, 1), new Vec3i(0, -1, 0), new Vec3i(0, -1, 1)};

    private static final Vec3i[] PORTAL_INTERIOR = new Vec3i[]{
            new Vec3i(0, 0, 0), new Vec3i(0, 1, 0), new Vec3i(0, 2, 0), new Vec3i(0, 0, 1), new Vec3i(0, 1, 1), new Vec3i(0, 2, 1), new Vec3i(1, 0, 0), new Vec3i(1, 1, 0), new Vec3i(1, 2, 0), new Vec3i(1, 0, 1),
            new Vec3i(1, 1, 1), new Vec3i(1, 2, 1), new Vec3i(-1, 0, 0), new Vec3i(-1, 1, 0), new Vec3i(-1, 2, 0), new Vec3i(-1, 0, 1), new Vec3i(-1, 1, 1), new Vec3i(-1, 2, 1)};

    private static final Vec3i PORTALABLE_REGION_SIZE = new Vec3i(4, 6, 6);

    private static final Vec3i PORTAL_ORIGIN_RELATIVE_TO_REGION = new Vec3i(1, 0, 2);

    private final TimerGame lavaSearchTimer = new TimerGame(5.0D);

    private final MovementProgressChecker progressChecker = new MovementProgressChecker();

    private final TimeoutWanderTask wanderTask = new TimeoutWanderTask(5.0F);

    private final Task collectLavaTask = (Task) TaskCatalogue.getItemTask(Items.LAVA_BUCKET, 1);

    private final TimerGame refreshTimer = new TimerGame(11.0D);

    private BlockPos portalOrigin = null;

    private Task getToLakeTask = null;

    private BlockPos currentDestroyTarget = null;

    private boolean firstSearch = false;

    protected void onStart() {
        this.currentDestroyTarget = null;
        AltoClefController mod = controller;
        mod.getBehaviour().push();
        mod.getBehaviour().avoidBlockBreaking(block -> {
            if (this.portalOrigin != null)
                for (Vec3i framePosRelative : PORTAL_FRAME) {
                    BlockPos framePos = this.portalOrigin.add(framePosRelative);
                    if (block.equals(framePos))
                        return (mod.getWorld().getBlockState(framePos).getBlock() == Blocks.OBSIDIAN);
                }
            return false;
        });
        mod.getBehaviour().addProtectedItems(new Item[]{Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.FLINT_AND_STEEL, Items.FIRE_CHARGE});
        this.progressChecker.reset();
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        if (this.portalOrigin != null &&
                mod.getWorld().getBlockState(this.portalOrigin.up()).getBlock() == Blocks.NETHER_PORTAL) {
            setDebugState("Done constructing nether portal.");
            mod.getBlockScanner().addBlock(Blocks.NETHER_PORTAL, this.portalOrigin.up());
            return null;
        }
        if (mod.getBaritone().getPathingBehavior().isPathing())
            this.progressChecker.reset();
        if (this.wanderTask.isActive() && !this.wanderTask.isFinished()) {
            setDebugState("Trying again.");
            this.progressChecker.reset();
            return (Task) this.wanderTask;
        }
        if (!this.progressChecker.check(mod)) {
            mod.getBaritone().getPathingBehavior().forceCancel();
            if (this.portalOrigin != null && this.currentDestroyTarget != null) {
                mod.getBlockScanner().requestBlockUnreachable(this.portalOrigin);
                mod.getBlockScanner().requestBlockUnreachable(this.currentDestroyTarget);
                if (mod.getBlockScanner().isUnreachable(this.portalOrigin) && mod.getBlockScanner().isUnreachable(this.currentDestroyTarget)) {
                    this.portalOrigin = null;
                    this.currentDestroyTarget = null;
                }
                return (Task) this.wanderTask;
            }
        }
        if (this.refreshTimer.elapsed()) {
            Debug.logMessage("Duct tape: Refreshing inventory again just in case");
            this.refreshTimer.reset();
        }
        if (this.portalOrigin != null && !this.portalOrigin.isCenterWithinDistance((Position) mod.getPlayer().getPos(), 2000.0D)) {
            this.portalOrigin = null;
            this.currentDestroyTarget = null;
        }
        if (this.currentDestroyTarget != null)
            if (!WorldHelper.isSolidBlock(controller, this.currentDestroyTarget)) {
                this.currentDestroyTarget = null;
            } else {
                return (Task) new DestroyBlockTask(this.currentDestroyTarget);
            }
        if (!mod.getItemStorage().hasItem(new Item[]{Items.FLINT_AND_STEEL}) && !mod.getItemStorage().hasItem(new Item[]{Items.FIRE_CHARGE})) {
            setDebugState("Getting flint & steel");
            this.progressChecker.reset();
            return (Task) TaskCatalogue.getItemTask(Items.FLINT_AND_STEEL, 1);
        }
        int bucketCount = mod.getItemStorage().getItemCount(new Item[]{Items.BUCKET, Items.LAVA_BUCKET, Items.WATER_BUCKET});
        if (bucketCount < 2) {
            setDebugState("Getting buckets");
            this.progressChecker.reset();
            if (mod.getItemStorage().hasItem(new Item[]{Items.LAVA_BUCKET}))
                return (Task) TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
            if (mod.getItemStorage().hasItem(new Item[]{Items.WATER_BUCKET}))
                return (Task) TaskCatalogue.getItemTask(Items.LAVA_BUCKET, 1);
            if (mod.getEntityTracker().itemDropped(new Item[]{Items.WATER_BUCKET, Items.LAVA_BUCKET}))
                return (Task) new PickupDroppedItemTask(new ItemTarget(new Item[]{Items.WATER_BUCKET, Items.LAVA_BUCKET}, 1), true);
            return (Task) TaskCatalogue.getItemTask(Items.BUCKET, 2);
        }
        boolean needsToLookForPortal = (this.portalOrigin == null);
        if (needsToLookForPortal) {
            this.progressChecker.reset();
            if (!mod.getItemStorage().hasItem(new Item[]{Items.WATER_BUCKET})) {
                setDebugState("Getting water");
                this.progressChecker.reset();
                return (Task) TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
            }
            boolean foundSpot = false;
            if (this.firstSearch || this.lavaSearchTimer.elapsed()) {
                this.firstSearch = false;
                this.lavaSearchTimer.reset();
                Debug.logMessage("(Searching for lava lake with portalable spot nearby...)");
                BlockPos lavaPos = findLavaLake(mod, mod.getPlayer().getBlockPos());
                if (lavaPos != null) {
                    BlockPos foundPortalRegion = getPortalableRegion(mod, lavaPos, mod.getPlayer().getBlockPos(), new Vec3i(-1, 0, 0), PORTALABLE_REGION_SIZE, 20);
                    if (foundPortalRegion == null) {
                        Debug.logWarning("Failed to find portalable region nearby. Consider increasing the search timeout range");
                    } else {
                        this.portalOrigin = foundPortalRegion.add(PORTAL_ORIGIN_RELATIVE_TO_REGION);
                        foundSpot = true;
                        this.getToLakeTask = (Task) new GetWithinRangeOfBlockTask(this.portalOrigin, 7);
                        return this.getToLakeTask;
                    }
                } else {
                    Debug.logMessage("(lava lake not found)");
                }
            }
            if (!foundSpot) {
                setDebugState("(timeout: Looking for lava lake)");
                return (Task) new TimeoutWanderTask();
            }
        }
        if (BeatMinecraftTask.isTaskRunning(mod, this.getToLakeTask))
            return this.getToLakeTask;
        for (Vec3i framePosRelative : PORTAL_FRAME) {
            BlockPos framePos = this.portalOrigin.add(framePosRelative);
            Block frameBlock = mod.getWorld().getBlockState(framePos).getBlock();
            if (frameBlock == Blocks.OBSIDIAN) {
                BlockPos waterCheck = framePos.up();
                if (mod.getWorld().getBlockState(waterCheck).getBlock() == Blocks.WATER && WorldHelper.isSourceBlock(controller, waterCheck, true)) {
                    setDebugState("Clearing water from cast");
                    return (Task) new ClearLiquidTask(waterCheck);
                }
            } else {
                if (!mod.getItemStorage().hasItem(new Item[]{Items.LAVA_BUCKET}) && frameBlock != Blocks.LAVA) {
                    setDebugState("Collecting lava");
                    this.progressChecker.reset();
                    return this.collectLavaTask;
                }
                if (mod.getBlockScanner().isUnreachable(framePos))
                    this.portalOrigin = null;
                return (Task) new PlaceObsidianBucketTask(framePos);
            }
        }
        for (Vec3i offs : PORTAL_INTERIOR) {
            BlockPos p = this.portalOrigin.add(offs);
            assert controller.getWorld() != null;
            if (!controller.getWorld().getBlockState(p).isAir()) {
                setDebugState("Clearing inside of portal");
                this.currentDestroyTarget = p;
                return null;
            }
        }
        setDebugState("Flinting and Steeling");
        return (Task) new InteractWithBlockTask(new ItemTarget(new Item[]{Items.FLINT_AND_STEEL, Items.FIRE_CHARGE}, 1), Direction.UP, this.portalOrigin.down(), true);
    }

    protected void onStop(Task interruptTask) {
        controller.getBehaviour().pop();
    }

    protected boolean isEqual(Task other) {
        return other instanceof adris.altoclef.tasks.construction.compound.ConstructNetherPortalBucketTask;
    }

    protected String toDebugString() {
        return "Construct Nether Portal";
    }

    private BlockPos findLavaLake(AltoClefController mod, BlockPos playerPos) {
        HashSet<BlockPos> alreadyExplored = new HashSet<>();
        double nearestSqDistance = Double.POSITIVE_INFINITY;
        BlockPos nearestLake = null;
        List<BlockPos> lavas = mod.getBlockScanner().getKnownLocations(new Block[]{Blocks.LAVA});
        if (!lavas.isEmpty())
            for (BlockPos pos : lavas) {
                if (alreadyExplored.contains(pos))
                    continue;
                double sqDist = playerPos.getSquaredDistance((Vec3i) pos);
                if (sqDist < nearestSqDistance) {
                    int depth = getNumberOfBlocksAdjacent(alreadyExplored, pos);
                    if (depth != 0) {
                        Debug.logMessage("Found with depth " + depth);
                        if (depth >= 12) {
                            nearestSqDistance = sqDist;
                            nearestLake = pos;
                        }
                    }
                }
            }
        return nearestLake;
    }

    private int getNumberOfBlocksAdjacent(HashSet<BlockPos> alreadyExplored, BlockPos start) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        int bonus = 0;
        while (!queue.isEmpty()) {
            BlockPos origin = queue.poll();
            if (alreadyExplored.contains(origin))
                continue;
            alreadyExplored.add(origin);
            assert controller.getWorld() != null;
            BlockState s = controller.getWorld().getBlockState(origin);
            if (s.getBlock() != Blocks.LAVA)
                continue;
            if (!s.getFluidState().isSource())
                continue;
            int level = s.getFluidState().getLevel();
            if (level != 8)
                continue;
            queue.addAll(List.of(origin.north(), origin.south(), origin.east(), origin.west(), origin.up(), origin.down()));
            bonus++;
        }
        return bonus;
    }

    private BlockPos getPortalableRegion(AltoClefController mod, BlockPos lava, BlockPos playerPos, Vec3i sizeOffset, Vec3i sizeAllocation, int timeoutRange) {
        Vec3i[] directions = {new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, -1)};
        double minDistanceToPlayer = Double.POSITIVE_INFINITY;
        BlockPos bestPos = null;
        for (Vec3i direction : directions) {
            for (int offs = 1; offs < timeoutRange; offs++) {
                Vec3i offset = new Vec3i(direction.getX() * offs, direction.getY() * offs, direction.getZ() * offs);
                boolean found = true;
                boolean solidFound = false;
                int dx;
                label46:
                for (dx = -1; dx < sizeAllocation.getX() + 1; dx++) {
                    for (int dz = -1; dz < sizeAllocation.getZ() + 1; dz++) {
                        for (int dy = -1; dy < sizeAllocation.getY(); dy++) {
                            BlockPos toCheck = lava.add(offset).add(sizeOffset).add(dx, dy, dz);
                            assert controller.getWorld() != null;
                            BlockState state = controller.getWorld().getBlockState(toCheck);
                            if (state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.BEDROCK) {
                                found = false;
                                break label46;
                            }
                            if (dy <= 1 && !solidFound && WorldHelper.isSolidBlock(controller, toCheck))
                                solidFound = true;
                        }
                    }
                }
                if (!solidFound)
                    break;
                if (found) {
                    BlockPos foundBoxCorner = lava.add(offset).add(sizeOffset);
                    double sqDistance = foundBoxCorner.getSquaredDistance((Vec3i) playerPos);
                    if (sqDistance < minDistanceToPlayer) {
                        minDistanceToPlayer = sqDistance;
                        bestPos = foundBoxCorner;
                    }
                    break;
                }
            }
        }
        return bestPos;
    }
}
