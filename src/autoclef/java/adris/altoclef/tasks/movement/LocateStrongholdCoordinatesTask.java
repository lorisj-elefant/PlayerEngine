package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.data.server.tag.StructureTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.Optional;

public class LocateStrongholdCoordinatesTask extends Task {
    private static final int EYE_RETHROW_DISTANCE = 10;

    private static final int SECOND_EYE_THROW_DISTANCE = 30;

    private final int targetEyes;

    private final int minimumEyes;

    private final TimerGame throwTimer = new TimerGame(5.0D);

    private EyeDirection cachedEyeDirection = null;

    private EyeDirection cachedEyeDirection2 = null;

    private Entity currentThrownEye = null;

    private Vec3i strongholdEstimatePos = null;

    public LocateStrongholdCoordinatesTask(int targetEyes, int minimumEyes) {
        this.targetEyes = targetEyes;
        this.minimumEyes = minimumEyes;
    }

    public LocateStrongholdCoordinatesTask(int targetEyes) {
        this(targetEyes, 12);
    }

    static Vec3i calculateIntersection(Vec3d start1, Vec3d direction1, Vec3d start2, Vec3d direction2) {
        Vec3d s1 = start1;
        Vec3d s2 = start2;
        Vec3d d1 = direction1;
        Vec3d d2 = direction2;
        double t2 = (d1.z * s2.x - d1.z * s1.x - d1.x * s2.z + d1.x * s1.z) / (d1.x * d2.z - d1.z * d2.x);
        BlockPos blockPos = BlockPosVer.ofFloored((Position) start2.add(direction2.multiply(t2)));
        return new Vec3i(blockPos.getX(), 0, blockPos.getZ());
    }

    protected void onStart() {
    }

    public boolean isSearching() {
        return (this.cachedEyeDirection != null);
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        if (WorldHelper.getCurrentDimension(controller) != Dimension.OVERWORLD) {
            setDebugState("Going to overworld");
            return (Task) new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }
        if (mod.getItemStorage().getItemCount(new Item[]{Items.ENDER_EYE}) < this.minimumEyes && mod.getEntityTracker().itemDropped(new Item[]{Items.ENDER_EYE}) && !mod.getEntityTracker().entityFound(new Class[]{EyeOfEnderEntity.class})) {
            setDebugState("Picking up dropped ender eye.");
            return (Task) new PickupDroppedItemTask(Items.ENDER_EYE, this.targetEyes);
        }
        if (mod.getEntityTracker().entityFound(new Class[]{EyeOfEnderEntity.class})) {
            if (this.currentThrownEye == null || !this.currentThrownEye.isAlive()) {
                Debug.logMessage("New eye direction");
                Debug.logMessage((this.currentThrownEye == null) ? "null" : "is not alive");
                List<EyeOfEnderEntity> enderEyes = mod.getEntityTracker().getTrackedEntities(EyeOfEnderEntity.class);
                if (!enderEyes.isEmpty())
                    for (EyeOfEnderEntity enderEye : enderEyes)
                        this.currentThrownEye = (Entity) enderEye;
                if (this.cachedEyeDirection2 != null) {
                    this.cachedEyeDirection = null;
                    this.cachedEyeDirection2 = null;
                } else if (this.cachedEyeDirection == null) {
                    this.cachedEyeDirection = new EyeDirection(this.currentThrownEye.getPos());
                } else {
                    this.cachedEyeDirection2 = new EyeDirection(this.currentThrownEye.getPos());
                }
            }
            if (this.cachedEyeDirection2 != null) {
                this.cachedEyeDirection2.updateEyePos(this.currentThrownEye.getPos());
            } else if (this.cachedEyeDirection != null) {
                this.cachedEyeDirection.updateEyePos(this.currentThrownEye.getPos());
            }
            if (mod.getEntityTracker().getClosestEntity(new Class[]{EyeOfEnderEntity.class}).isPresent() &&
                    !mod.getBaritone().getPathingBehavior().isPathing())
                LookHelper.lookAt(mod, ((Entity) mod
                        .getEntityTracker().getClosestEntity(new Class[]{EyeOfEnderEntity.class}).get()).getEyePos());
            setDebugState("Waiting for eye to travel.");
            return null;
        }
        if (this.cachedEyeDirection2 != null && !mod.getEntityTracker().entityFound(new Class[]{EyeOfEnderEntity.class}) && this.strongholdEstimatePos == null)
            if (this.cachedEyeDirection2.getAngle() >= this.cachedEyeDirection.getAngle()) {
                Debug.logMessage("2nd eye thrown at wrong position, or points to different stronghold. Rethrowing");
                this.cachedEyeDirection = this.cachedEyeDirection2;
                this.cachedEyeDirection2 = null;
            } else {
                Vec3d throwOrigin = this.cachedEyeDirection.getOrigin();
                Vec3d throwOrigin2 = this.cachedEyeDirection2.getOrigin();
                Vec3d throwDelta = this.cachedEyeDirection.getDelta();
                Vec3d throwDelta2 = this.cachedEyeDirection2.getDelta();
                this.strongholdEstimatePos = calculateIntersection(throwOrigin, throwDelta, throwOrigin2, throwDelta2);
                Debug.logMessage("Stronghold is at " + this.strongholdEstimatePos.getX() + ", " + this.strongholdEstimatePos.getZ() + " (" + (int) mod.getPlayer().getPos().distanceTo(Vec3d.of(this.strongholdEstimatePos)) + " blocks away)");
            }
        if (this.strongholdEstimatePos != null &&
                mod.getPlayer().getPos().distanceTo(Vec3d.of(this.strongholdEstimatePos)) < 10.0D && WorldHelper.getCurrentDimension(controller) == Dimension.OVERWORLD) {
            this.strongholdEstimatePos = null;
            this.cachedEyeDirection = null;
            this.cachedEyeDirection2 = null;
        }
        if (!mod.getEntityTracker().entityFound(new Class[]{EyeOfEnderEntity.class}) && this.strongholdEstimatePos == null) {
            if (WorldHelper.getCurrentDimension(controller) == Dimension.NETHER) {
                setDebugState("Going to overworld.");
                return (Task) new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
            if (!mod.getItemStorage().hasItem(new Item[]{Items.ENDER_EYE})) {
                setDebugState("Collecting eye of ender.");
                return (Task) TaskCatalogue.getItemTask(Items.ENDER_EYE, 1);
            }
            if (this.cachedEyeDirection == null) {
                setDebugState("Throwing first eye.");
            } else {
                setDebugState("Throwing second eye.");
                double sqDist = mod.getPlayer().squaredDistanceTo(this.cachedEyeDirection.getOrigin());
                if (sqDist < 900.0D && this.cachedEyeDirection != null)
                    return (Task) new GoInDirectionXZTask(this.cachedEyeDirection.getOrigin(), this.cachedEyeDirection.getDelta().rotateY(1.5707964F), 1.0D);
            }
            if (mod.getSlotHandler().forceEquipItem(Items.ENDER_EYE)) {
                throwEye(controller.getWorld(), controller.getEntity());
            } else {
                Debug.logWarning("Failed to equip eye of ender to throw.");
            }
            return null;
        }
        if ((this.cachedEyeDirection != null && !this.cachedEyeDirection.hasDelta()) || (this.cachedEyeDirection2 != null &&
                !this.cachedEyeDirection2.hasDelta())) {
            setDebugState("Waiting for thrown eye to appear...");
            return null;
        }
        return null;
    }

    private void throwEye(ServerWorld world, LivingEntity user) {
        BlockPos blockPos = world.findFirstPos(StructureTags.EYE_OF_ENDER_LOCATED, user.getBlockPos(), 100, false);
        if (blockPos != null) {
            EyeOfEnderEntity eyeOfEnderEntity = new EyeOfEnderEntity(world, user.getX(), user.getBodyY((double) 0.5F), user.getZ());
            eyeOfEnderEntity.setItem(user.getMainHandStack());
            eyeOfEnderEntity.initTargetPos(blockPos);
            world.emitGameEvent(GameEvent.PROJECTILE_SHOOT, eyeOfEnderEntity.getPos(), GameEvent.Context.create(user));
            world.spawnEntity(eyeOfEnderEntity);

            world.playSound((PlayerEntity) null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            world.syncWorldEvent((PlayerEntity) null, 1003, user.getBlockPos(), 0);
            user.getMainHandStack().decrement(1);

            user.swingHand(Hand.MAIN_HAND, true);
        }
    }

    protected void onStop(Task interruptTask) {
    }

    public Optional<BlockPos> getStrongholdCoordinates() {
        if (this.strongholdEstimatePos == null)
            return Optional.empty();
        return Optional.of(new BlockPos(this.strongholdEstimatePos));
    }

    protected boolean isEqual(Task other) {
        return other instanceof adris.altoclef.tasks.movement.LocateStrongholdCoordinatesTask;
    }

    protected String toDebugString() {
        return "Locating stronghold coordinates";
    }

    public boolean isFinished() {
        return (this.strongholdEstimatePos != null);
    }

    private static class EyeDirection {
        private final Vec3d start;
        private Vec3d end;

        public EyeDirection(Vec3d startPos) {
            start = startPos;
        }

        public void updateEyePos(Vec3d endPos) {
            end = endPos;
        }

        public Vec3d getOrigin() {
            return start;
        }

        public Vec3d getDelta() {
            if (end == null) return Vec3d.ZERO;
            return end.subtract(start);
        }

        public double getAngle() {
            if (end == null) return 0;
            return Math.atan2(getDelta().getX(), getDelta().getZ());
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean hasDelta() {
            return end != null && getDelta().lengthSquared() > 0.00001;
        }
    }
}
