package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class KillEnderDragonWithBedsTask extends Task {
    private final WaitForDragonAndPearlTask whenNotPerchingTask;

    TimerGame placeBedTimer = new TimerGame(0.6D);

    TimerGame waiTimer = new TimerGame(0.3D);

    TimerGame waitBeforePlaceTimer = new TimerGame(0.5D);

    boolean waited = false;

    double prevDist = 100.0D;

    private BlockPos endPortalTop;

    private Task freePortalTopTask = null;

    private Task placeObsidianTask = null;

    private boolean dragonDead = false;

    public KillEnderDragonWithBedsTask() {
        this.whenNotPerchingTask = new WaitForDragonAndPearlTask();
    }

    public static BlockPos locateExitPortalTop(AltoClefController mod) {
        if (!mod.getChunkTracker().isChunkLoaded(new BlockPos(0, 64, 0)))
            return null;
        int height = WorldHelper.getGroundHeight(mod, 0, 0, new Block[]{Blocks.BEDROCK});
        if (height != -1)
            return new BlockPos(0, height, 0);
        return null;
    }

    protected void onStart() {
        controller.getBehaviour().avoidBlockPlacing(pos -> (pos.getZ() == 0 && Math.abs(pos.getX()) < 5));
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        if (this.endPortalTop == null) {
            this.endPortalTop = locateExitPortalTop(mod);
            if (this.endPortalTop != null)
                this.whenNotPerchingTask.setExitPortalTop(this.endPortalTop);
        }
        if (this.endPortalTop == null) {
            setDebugState("Searching for end portal top.");
            return (Task) new GetToXZTask(0, 0);
        }
        BlockPos obsidianTarget = this.endPortalTop.up().offset(Direction.NORTH);
        if (!mod.getWorld().getBlockState(obsidianTarget).getBlock().equals(Blocks.OBSIDIAN)) {
            if (WorldHelper.inRangeXZ(mod.getPlayer().getPos(), new Vec3d(0.0D, 0.0D, 0.0D), 10.0D)) {
                if (this.placeObsidianTask == null)
                    this.placeObsidianTask = (Task) new PlaceBlockTask(obsidianTarget, new Block[]{Blocks.OBSIDIAN});
                return this.placeObsidianTask;
            }
            return (Task) new GetToXZTask(0, 0);
        }
        BlockState stateAtPortal = mod.getWorld().getBlockState(this.endPortalTop.up());
        if (!stateAtPortal.isAir() && !stateAtPortal.getBlock().equals(Blocks.FIRE) &&
                !Arrays.<Block>stream(ItemHelper.itemsToBlocks(ItemHelper.BED)).toList().contains(stateAtPortal.getBlock())) {
            if (this.freePortalTopTask == null)
                this.freePortalTopTask = (Task) new DestroyBlockTask(this.endPortalTop.up());
            return this.freePortalTopTask;
        }
        if (this.dragonDead) {
            setDebugState("Waiting for overworld portal to spawn.");
            return (Task) new GetToBlockTask(this.endPortalTop.down(4).west());
        }
        if (!mod.getEntityTracker().entityFound(new Class[]{EnderDragonEntity.class}) || this.dragonDead) {
            setDebugState("No dragon found.");
            if (!WorldHelper.inRangeXZ((Entity) mod.getPlayer(), this.endPortalTop, 1.0D)) {
                setDebugState("Going to end portal top at" + this.endPortalTop.toString() + ".");
                return (Task) new GetToBlockTask(this.endPortalTop);
            }
        }
        List<EnderDragonEntity> dragons = mod.getEntityTracker().getTrackedEntities(EnderDragonEntity.class);
        for (EnderDragonEntity dragon : dragons) {
            Phase dragonPhase = dragon.getPhaseManager().getCurrent();
            if (dragonPhase.getType() == PhaseType.DYING) {
                Debug.logMessage("Dragon is dead.");
                if (mod.getPlayer().getPitch() != -90.0F)
                    mod.getPlayer().setPitch(-90.0F);
                this.dragonDead = true;
                return null;
            }
            boolean perching = (dragonPhase instanceof net.minecraft.entity.boss.dragon.phase.LandingPhase || dragonPhase instanceof net.minecraft.entity.boss.dragon.phase.LandingApproachPhase || dragonPhase.isSittingOrHovering());
            if (dragon.getY() < (this.endPortalTop.getY() + 2))
                perching = false;
            this.whenNotPerchingTask.setPerchState(perching);
            if (this.whenNotPerchingTask.isActive() && !this.whenNotPerchingTask.isFinished()) {
                setDebugState("Dragon not perching, performing special behavior...");
                return (Task) this.whenNotPerchingTask;
            }
            if (perching)
                return performOneCycle(mod, dragon);
        }
        mod.getFoodChain().shouldStop(false);
        return (Task) this.whenNotPerchingTask;
    }

    private Task performOneCycle(AltoClefController mod, EnderDragonEntity dragon) {
        mod.getFoodChain().shouldStop(true);
        if (mod.getInputControls().isHeldDown(Input.SNEAK))
            mod.getInputControls().release(Input.SNEAK);
        mod.getSlotHandler().forceEquipItemToOffhand(Items.AIR);
        BlockPos endPortalTop = locateExitPortalTop(mod).up();
        BlockPos obsidian = null;
        Direction dir = null;
        for (Direction direction : new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}) {
            if (mod.getWorld().getBlockState(endPortalTop.offset(direction)).getBlock().equals(Blocks.OBSIDIAN)) {
                obsidian = endPortalTop.offset(direction);
                dir = direction.getOpposite();
                break;
            }
        }
        if (dir == null) {
            mod.log("no obisidan? :(");
            return null;
        }
        Direction offsetDir = (dir.getAxis() == Direction.Axis.X) ? Direction.SOUTH : Direction.WEST;
        BlockPos targetBlock = endPortalTop.down(3).offset(offsetDir, 3).offset(dir);
        double d = distanceIgnoreY(WorldHelper.toVec3d(targetBlock), mod.getPlayer().getPos());
        if (d > 0.7D || mod.getPlayer().getBlockPos().down().getY() > endPortalTop.getY() - 4) {
            mod.log("" + d);
            return (Task) new GetToBlockTask(targetBlock);
        }
        if (!this.waited) {
            this.waited = true;
            this.waitBeforePlaceTimer.reset();
        }
        if (!this.waitBeforePlaceTimer.elapsed()) {
            mod.log("" + this.waitBeforePlaceTimer.getDuration() + " waiting...");
            return null;
        }
        LookHelper.lookAt(mod, obsidian, dir);
        BlockPos bedHead = WorldHelper.getBedHead(mod, endPortalTop);
        mod.getSlotHandler().forceEquipItem(ItemHelper.BED);
        if (bedHead == null) {
            if (this.placeBedTimer.elapsed() && Math.abs(dragon.getY() - endPortalTop.getY()) < 10.0D) {
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                this.waiTimer.reset();
            }
            return null;
        }
        if (!this.waiTimer.elapsed())
            return null;
        Vec3d dragonHeadPos = dragon.head.getBoundingBox().getCenter();
        Vec3d bedHeadPos = WorldHelper.toVec3d(bedHead);
        double dist = dragonHeadPos.distanceTo(bedHeadPos);
        double distXZ = distanceIgnoreY(dragonHeadPos, bedHeadPos);
        EnderDragonPart body = dragon.getBodyParts()[2];
        double destroyDistance = Math.abs(body.getBoundingBox().getMin(Direction.Axis.Y) - bedHeadPos.getY());
        boolean tooClose = (destroyDistance < 1.1D);
        boolean skip = (destroyDistance > 3.0D && dist > 4.5D && distXZ > 2.5D);
        mod.log("" + destroyDistance + " : " + destroyDistance + " : " + dist);
        if (dist < 1.5D || (this.prevDist < distXZ && destroyDistance < 4.0D && this.prevDist < 2.9D) || (destroyDistance < 2.0D && dist < 4.0D) || (destroyDistance < 1.7D && dist < 4.5D) || tooClose || (destroyDistance < 2.4D && distXZ < 3.7D) || (destroyDistance < 3.5D && distXZ < 2.4D))
            if (!skip) {
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                this.placeBedTimer.reset();
            }
        this.prevDist = distXZ;
        return null;
    }

    public double distanceIgnoreY(Vec3d vec, Vec3d vec1) {
        double d = vec.x - vec1.x;
        double f = vec.z - vec1.z;
        return Math.sqrt(d * d + f * f);
    }

    protected void onStop(Task interruptTask) {
        controller.getFoodChain().shouldStop(false);
    }

    public boolean isFinished() {
        return super.isFinished();
    }

    protected boolean isEqual(Task other) {
        return other instanceof adris.altoclef.tasks.speedrun.KillEnderDragonWithBedsTask;
    }

    protected String toDebugString() {
        return "Bedding the Ender Dragon";
    }
}
