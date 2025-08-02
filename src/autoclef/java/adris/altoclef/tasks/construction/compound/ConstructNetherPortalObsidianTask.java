package adris.altoclef.tasks.construction.compound;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.LinkedList;

public class ConstructNetherPortalObsidianTask extends Task {
    private static final Vec3i[] PORTAL_FRAME = new Vec3i[]{new Vec3i(0, 0, -1), new Vec3i(0, 1, -1), new Vec3i(0, 2, -1), new Vec3i(0, 0, 2), new Vec3i(0, 1, 2), new Vec3i(0, 2, 2), new Vec3i(0, 3, 0), new Vec3i(0, 3, 1), new Vec3i(0, -1, 0), new Vec3i(0, -1, 1)};

    private static final Vec3i[] PORTAL_INTERIOR = new Vec3i[]{
            new Vec3i(0, 0, 0), new Vec3i(0, 1, 0), new Vec3i(0, 2, 0), new Vec3i(0, 0, 1), new Vec3i(0, 1, 1), new Vec3i(0, 2, 1), new Vec3i(1, 0, 0), new Vec3i(1, 1, 0), new Vec3i(1, 2, 0), new Vec3i(1, 0, 1),
            new Vec3i(1, 1, 1), new Vec3i(1, 2, 1), new Vec3i(-1, 0, 0), new Vec3i(-1, 1, 0), new Vec3i(-1, 2, 0), new Vec3i(-1, 0, 1), new Vec3i(-1, 1, 1), new Vec3i(-1, 2, 1)};

    private static final Vec3i PORTALABLE_REGION_SIZE = new Vec3i(3, 6, 6);

    private final TimerGame areaSearchTimer = new TimerGame(5.0D);

    private BlockPos origin;

    private BlockPos destroyTarget;

    private BlockPos getBuildableAreaNearby(AltoClefController mod) {
        BlockPos checkOrigin = mod.getPlayer().getBlockPos();
        for (BlockPos toCheck : WorldHelper.scanRegion(checkOrigin, checkOrigin.add(PORTALABLE_REGION_SIZE))) {
            if (controller.getWorld() == null)
                return null;
            BlockState state = controller.getWorld().getBlockState(toCheck);
            boolean validToWorld = (WorldHelper.canPlace(controller, toCheck) || WorldHelper.canBreak(controller, toCheck));
            if (!validToWorld || state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.BEDROCK)
                return null;
        }
        return checkOrigin;
    }

    protected void onStart() {
        AltoClefController mod = controller;
        mod.getBehaviour().push();
        mod.getBehaviour().avoidBlockBreaking(block -> {
            if (this.origin != null)
                for (Vec3i framePosRelative : PORTAL_FRAME) {
                    BlockPos framePos = this.origin.add(framePosRelative);
                    if (block.equals(framePos))
                        return (mod.getWorld().getBlockState(framePos).getBlock() == Blocks.OBSIDIAN);
                }
            return false;
        });
        mod.getBehaviour().addProtectedItems(new Item[]{Items.FLINT_AND_STEEL});
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        if (this.origin != null &&
                mod.getWorld().getBlockState(this.origin.up()).getBlock() == Blocks.NETHER_PORTAL) {
            setDebugState("Done constructing nether portal.");
            mod.getBlockScanner().addBlock(Blocks.NETHER_PORTAL, this.origin.up());
            return null;
        }
        int neededObsidian = 10;
        BlockPos placeTarget = null;
        if (this.origin != null)
            for (Vec3i frameOffs : PORTAL_FRAME) {
                BlockPos framePos = this.origin.add(frameOffs);
                if (!mod.getBlockScanner().isBlockAtPosition(framePos, new Block[]{Blocks.OBSIDIAN})) {
                    placeTarget = framePos;
                    break;
                }
                neededObsidian--;
            }
        if (mod.getItemStorage().getItemCount(new Item[]{Items.OBSIDIAN}) < neededObsidian) {
            setDebugState("Getting obsidian");
            return (Task) TaskCatalogue.getItemTask(Items.OBSIDIAN, neededObsidian);
        }
        if (this.origin == null) {
            if (this.areaSearchTimer.elapsed()) {
                this.areaSearchTimer.reset();
                Debug.logMessage("(Searching for area to build portal nearby...)");
                this.origin = getBuildableAreaNearby(mod);
            }
            setDebugState("Looking for portalable area...");
            return (Task) new TimeoutWanderTask();
        }
        if (!mod.getItemStorage().hasItem(new Item[]{Items.FLINT_AND_STEEL})) {
            setDebugState("Getting flint and steel");
            return (Task) TaskCatalogue.getItemTask(Items.FLINT_AND_STEEL, 1);
        }
        if (placeTarget != null) {
            World clientWorld = mod.getWorld();
            if (surroundedByAir((World) clientWorld, placeTarget)) {
                LinkedList<BlockPos> queue = new LinkedList<>();
                queue.add(placeTarget);
                while (surroundedByAir((World) clientWorld, placeTarget)) {
                    BlockPos pos = queue.removeFirst();
                    if (surroundedByAir((World) clientWorld, pos)) {
                        queue.add(pos.up());
                        queue.add(pos.down());
                        queue.add(pos.east());
                        queue.add(pos.west());
                        queue.add(pos.north());
                        queue.add(pos.south());
                        continue;
                    }
                    return (Task) new PlaceStructureBlockTask(pos);
                }
                mod.logWarning("Did not find any block to place obsidian on");
            }
            if (!clientWorld.getBlockState(placeTarget).isAir() && !clientWorld.getBlockState(placeTarget).getBlock().equals(Blocks.OBSIDIAN))
                return (Task) new DestroyBlockTask(placeTarget);
            setDebugState("Placing frame...");
            return (Task) new PlaceBlockTask(placeTarget, new Block[]{Blocks.OBSIDIAN});
        }
        if (this.destroyTarget != null && !WorldHelper.isAir(controller.getWorld().getBlockState(this.destroyTarget).getBlock()))
            return (Task) new DestroyBlockTask(this.destroyTarget);
        for (Vec3i middleOffs : PORTAL_INTERIOR) {
            BlockPos middlePos = this.origin.add(middleOffs);
            if (!WorldHelper.isAir(controller.getWorld().getBlockState(middlePos).getBlock())) {
                this.destroyTarget = middlePos;
                return (Task) new DestroyBlockTask(this.destroyTarget);
            }
        }
        return (Task) new InteractWithBlockTask(new ItemTarget(Items.FLINT_AND_STEEL, 1), Direction.UP, this.origin.down(), true);
    }

    private boolean surroundedByAir(World world, BlockPos pos) {
        return (world.getBlockState(pos.west()).isAir() && world.getBlockState(pos.south()).isAir() && world.getBlockState(pos.east()).isAir() && world
                .getBlockState(pos.up()).isAir() && world.getBlockState(pos.down()).isAir() && world.getBlockState(pos.north()).isAir());
    }

    protected void onStop(Task interruptTask) {
        controller.getBehaviour().pop();
    }

    protected boolean isEqual(Task other) {
        return other instanceof adris.altoclef.tasks.construction.compound.ConstructNetherPortalObsidianTask;
    }

    protected String toDebugString() {
        return "Building nether portal with obsidian";
    }
}
