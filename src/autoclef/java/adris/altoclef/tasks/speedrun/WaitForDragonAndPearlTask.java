package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasks.movement.GetToYTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasks.resources.GetBuildingMaterialsTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.function.Predicate;

public class WaitForDragonAndPearlTask extends Task {
    private static final double XZ_RADIUS = 30.0D;

    private static final double XZ_RADIUS_TOO_FAR = 38.0D;

    private static final int HEIGHT = 42;

    private static final int CLOSE_ENOUGH_DISTANCE = 15;

    private final int Y_COORDINATE = 75;

    private static final double DRAGON_FIREBALL_TOO_CLOSE_RANGE = 40.0D;

    private final Task buildingMaterialsTask = (Task) new GetBuildingMaterialsTask(52);

    boolean inCenter;

    private Task heightPillarTask;

    private Task throwPearlTask;

    private BlockPos targetToPearl;

    private boolean dragonIsPerching;

    private Task pillarUpFurther;

    private boolean hasPillar = false;

    public void setExitPortalTop(BlockPos top) {
        BlockPos actualTarget = top.down();
        if (!actualTarget.equals(this.targetToPearl)) {
            this.targetToPearl = actualTarget;
            this.throwPearlTask = (Task) new ThrowEnderPearlSimpleProjectileTask(actualTarget);
        }
    }

    public void setPerchState(boolean perching) {
        this.dragonIsPerching = perching;
    }

    protected void onStart() {
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        Optional<Entity> enderMen = mod.getEntityTracker().getClosestEntity(new Class[]{EndermanEntity.class});
        if (enderMen.isPresent()) {
            EndermanEntity endermanEntity = (EndermanEntity) enderMen.get();
            if (endermanEntity instanceof EndermanEntity) {
                EndermanEntity endermanEntity1 = endermanEntity;
                if (endermanEntity1
                        .getTarget() == mod.getPlayer()) {
                    setDebugState("Killing angry endermen");
                    Predicate<Entity> angry = entity -> (endermanEntity.getTarget() == mod.getPlayer());
                    return (Task) new KillEntitiesTask(angry, new Class[]{((Entity) enderMen.get()).getClass()});
                }
            }
        }
        if (this.throwPearlTask != null && this.throwPearlTask.isActive() && !this.throwPearlTask.isFinished()) {
            setDebugState("Throwing pearl!");
            return this.throwPearlTask;
        }
        if (this.pillarUpFurther != null && this.pillarUpFurther.isActive() && !this.pillarUpFurther.isFinished() && mod.getEntityTracker().getClosestEntity(new Class[]{AreaEffectCloudEntity.class}).isPresent()) {
            Optional<Entity> cloud = mod.getEntityTracker().getClosestEntity(new Class[]{AreaEffectCloudEntity.class});
            if (cloud.isPresent() && ((Entity) cloud.get()).isInRange((Entity) mod.getPlayer(), 4.0D)) {
                setDebugState("PILLAR UP FURTHER to avoid dragon's breath");
                return this.pillarUpFurther;
            }
            Optional<Entity> fireball = mod.getEntityTracker().getClosestEntity(new Class[]{DragonFireballEntity.class});
            if (isFireballDangerous(mod, fireball)) {
                setDebugState("PILLAR UP FURTHER to avoid dragon's breath");
                return this.pillarUpFurther;
            }
        }
        if (!mod.getItemStorage().hasItem(new Item[]{Items.ENDER_PEARL}) && this.inCenter) {
            setDebugState("First get ender pearls.");
            return (Task) TaskCatalogue.getItemTask(Items.ENDER_PEARL, 1);
        }
        int minHeight = this.targetToPearl.getY() + 42 - 3;
        int deltaY = minHeight - mod.getPlayer().getBlockPos().getY();
        if (StorageHelper.getBuildingMaterialCount(controller) < Math.min(deltaY - 10, 37) || (this.buildingMaterialsTask.isActive() && !this.buildingMaterialsTask.isFinished())) {
            setDebugState("Collecting building materials...");
            return this.buildingMaterialsTask;
        }
        if (this.dragonIsPerching && canThrowPearl(mod)) {
            Debug.logMessage("THROWING PEARL!!");
            return this.throwPearlTask;
        }
        if (mod.getPlayer().getBlockPos().getY() < minHeight) {
            if (mod.getEntityTracker().entityFound(entity -> mod.getPlayer().getPos().isInRange((Position) entity.getPos(), 4.0D), new Class[]{AreaEffectCloudEntity.class})) {
                if (mod.getEntityTracker().getClosestEntity(new Class[]{EnderDragonEntity.class}).isPresent() &&
                        !mod.getBaritone().getPathingBehavior().isPathing())
                    LookHelper.lookAt(mod, ((Entity) mod.getEntityTracker().getClosestEntity(new Class[]{EnderDragonEntity.class}).get()).getEyePos());
                return null;
            }
            if (this.heightPillarTask != null && this.heightPillarTask.isActive() && !this.heightPillarTask.isFinished()) {
                setDebugState("Pillaring up!");
                this.inCenter = true;
                if (mod.getEntityTracker().entityFound(new Class[]{EndCrystalEntity.class}))
                    return (Task) new DoToClosestEntityTask(toDestroy -> {
                        if (toDestroy.isInRange((Entity) mod.getPlayer(), 7.0D))
                            mod.getControllerExtras().attack(toDestroy);
                        if (mod.getPlayer().getBlockPos().getY() < minHeight)
                            return this.heightPillarTask;
                        if (mod.getEntityTracker().getClosestEntity(new Class[]{EnderDragonEntity.class}).isPresent() && !mod.getBaritone().getPathingBehavior().isPathing())
                            LookHelper.lookAt(mod, ((Entity) mod.getEntityTracker().getClosestEntity(new Class[]{EnderDragonEntity.class}).get()).getEyePos());
                        return null;
                    }, new Class[]{EndCrystalEntity.class});
                return this.heightPillarTask;
            }
        } else {
            setDebugState("We're high enough.");
            Optional<Entity> dragonFireball = mod.getEntityTracker().getClosestEntity(new Class[]{DragonFireballEntity.class});
            if (dragonFireball.isPresent() && ((Entity) dragonFireball.get()).isInRange((Entity) mod.getPlayer(), 40.0D) && LookHelper.cleanLineOfSight((Entity) mod.getPlayer(), ((Entity) dragonFireball.get()).getPos(), 40.0D)) {
                this.pillarUpFurther = (Task) new GetToYTask(mod.getPlayer().getBlockY() + 5);
                Debug.logMessage("HOLDUP");
                return this.pillarUpFurther;
            }
            if (mod.getEntityTracker().entityFound(new Class[]{EndCrystalEntity.class}))
                return (Task) new DoToClosestEntityTask(toDestroy -> {
                    if (toDestroy.isInRange((Entity) mod.getPlayer(), 7.0D))
                        mod.getControllerExtras().attack(toDestroy);
                    if (mod.getPlayer().getBlockPos().getY() < minHeight)
                        return this.heightPillarTask;
                    if (mod.getEntityTracker().getClosestEntity(new Class[]{EnderDragonEntity.class}).isPresent() && !mod.getBaritone().getPathingBehavior().isPathing())
                        LookHelper.lookAt(mod, ((Entity) mod.getEntityTracker().getClosestEntity(new Class[]{EnderDragonEntity.class}).get()).getEyePos());
                    return null;
                }, new Class[]{EndCrystalEntity.class});
            if (mod.getEntityTracker().getClosestEntity(new Class[]{EnderDragonEntity.class}).isPresent() &&
                    !mod.getBaritone().getPathingBehavior().isPathing())
                LookHelper.lookAt(mod, ((Entity) mod.getEntityTracker().getClosestEntity(new Class[]{EnderDragonEntity.class}).get()).getEyePos());
            return null;
        }
        if (!WorldHelper.inRangeXZ((Entity) mod.getPlayer(), this.targetToPearl, 38.0D) && mod.getPlayer().getPos().getY() < minHeight && !this.hasPillar) {
            if (mod.getEntityTracker().entityFound(entity -> mod.getPlayer().getPos().isInRange((Position) entity.getPos(), 4.0D), new Class[]{AreaEffectCloudEntity.class})) {
                if (mod.getEntityTracker().getClosestEntity(new Class[]{EnderDragonEntity.class}).isPresent() &&
                        !mod.getBaritone().getPathingBehavior().isPathing())
                    LookHelper.lookAt(mod, ((Entity) mod.getEntityTracker().getClosestEntity(new Class[]{EnderDragonEntity.class}).get()).getEyePos());
                return null;
            }
            setDebugState("Moving in (too far, might hit pillars)");
            return (Task) new GetToXZTask(0, 0);
        }
        if (!this.hasPillar)
            this.hasPillar = true;
        this.heightPillarTask = (Task) new GetToBlockTask(new BlockPos(0, minHeight, 75));
        return this.heightPillarTask;
    }

    private boolean canThrowPearl(AltoClefController mod) {
        Vec3d targetPosition = WorldHelper.toVec3d(targetToPearl.up());

        // Perform a raycast from the entity's camera position to the target position with the specified max range
        BlockHitResult hitResult = LookHelper.raycast(mod.getPlayer(), LookHelper.getCameraPos(mod.getPlayer()), targetPosition, 300);

        if (hitResult == null) {
            // No hit result, clear line of sight
            return true;
        } else {
            return switch (hitResult.getType()) {
                case MISS ->
                    // Missed the target, clear line of sight
                        true;
                case BLOCK ->
                    // Hit a block, check if it's the same as the target block
                        hitResult.getBlockPos().isWithinDistance(targetToPearl.up(), 10);
                case ENTITY ->
                    // Hit an entity, line of sight blocked
                        false;
            };
        }
    }

    private boolean isFireballDangerous(AltoClefController mod, Optional<Entity> fireball) {
        if (fireball.isEmpty())
            return false;
        boolean fireballTooClose = ((Entity) fireball.get()).isInRange((Entity) mod.getPlayer(), 40.0D);
        boolean fireballInSight = LookHelper.cleanLineOfSight((Entity) mod.getPlayer(), ((Entity) fireball.get()).getPos(), 40.0D);
        return (fireballTooClose && fireballInSight);
    }

    protected void onStop(Task interruptTask) {
    }

    protected boolean isEqual(Task other) {
        return other instanceof adris.altoclef.tasks.speedrun.WaitForDragonAndPearlTask;
    }

    public boolean isFinished() {
        return (this.dragonIsPerching && (this.throwPearlTask == null || (this.throwPearlTask
                .isActive() && this.throwPearlTask.isFinished()) ||
                WorldHelper.inRangeXZ((Entity) controller.getPlayer(), this.targetToPearl, 15.0D)));
    }

    protected String toDebugString() {
        return "Waiting for Dragon Perch + Pearling";
    }
}
