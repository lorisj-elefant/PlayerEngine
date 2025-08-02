package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ShootArrowSimpleProjectileTask extends Task {
    private final Entity target;

    private boolean shooting = false;

    private boolean shot = false;

    private final TimerGame shotTimer = new TimerGame(1.0D);

    public ShootArrowSimpleProjectileTask(Entity target) {
        this.target = target;
    }

    protected void onStart() {
        this.shooting = false;
    }

    private static Rotation calculateThrowLook(AltoClefController mod, Entity target) {
        float velocity = (mod.getPlayer().getItemUseTime() - mod.getPlayer().getItemUseTimeLeft()) / 20.0F;
        velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;
        if (velocity > 1.0F)
            velocity = 1.0F;
        Vec3d targetCenter = target.getBoundingBox().getCenter();
        double posX = targetCenter.getX();
        double posY = targetCenter.getY();
        double posZ = targetCenter.getZ();
        posY -= (1.9F - target.getHeight());
        double relativeX = posX - mod.getPlayer().getX();
        double relativeY = posY - mod.getPlayer().getY();
        double relativeZ = posZ - mod.getPlayer().getZ();
        double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        double hDistanceSq = hDistance * hDistance;
        float g = 0.006F;
        float velocitySq = velocity * velocity;
        float pitch = (float) -Math.toDegrees(Math.atan((velocitySq - Math.sqrt((velocitySq * velocitySq) - 0.006000000052154064D * (0.006000000052154064D * hDistanceSq + 2.0D * relativeY * velocitySq))) / 0.006000000052154064D * hDistance));
        if (Float.isNaN(pitch))
            return new Rotation(target.getYaw(), target.getPitch());
        return new Rotation(Vec3dToYaw(mod, new Vec3d(posX, posY, posZ)), pitch);
    }

    private static float Vec3dToYaw(AltoClefController mod, Vec3d vec) {
        return mod.getPlayer().getYaw() +
                MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(vec.getZ() - mod.getPlayer().getZ(), vec.getX() - mod.getPlayer().getX())) - 90.0F - mod.getPlayer().getYaw());
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        setDebugState("Shooting projectile");
        List<Item> requiredArrows = Arrays.asList(new Item[]{Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW});
        Objects.requireNonNull(mod.getItemStorage());
        if (!mod.getItemStorage().hasItem(new Item[]{Items.BOW}) || !requiredArrows.stream().anyMatch(mod.getItemStorage()::hasItem)) {
            Debug.logMessage("Missing items, stopping.");
            return null;
        }
        Rotation lookTarget = calculateThrowLook(mod, this.target);
        LookHelper.lookAt(controller, lookTarget);
        boolean charged = (mod.getPlayer().getItemUseTime() > 20 && mod.getPlayer().getActiveItem().getItem() == Items.BOW);
        mod.getSlotHandler().forceEquipItem(Items.BOW);
        if (LookHelper.isLookingAt(mod, lookTarget) && !this.shooting) {
            mod.getInputControls().hold(Input.CLICK_RIGHT);
            this.shooting = true;
            this.shotTimer.reset();
        }
        if (this.shooting && charged) {
            List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
            for (ArrowEntity arrow : arrows) {
                if (arrow.getOwner() == mod.getPlayer()) {
                    Vec3d velocity = arrow.getVelocity();
                    Vec3d delta = this.target.getPos().subtract(arrow.getPos());
                    boolean isMovingTowardsTarget = (velocity.dotProduct(delta) > 0.0D);
                    if (isMovingTowardsTarget)
                        return null;
                }
            }
            mod.getInputControls().release(Input.CLICK_RIGHT);
            this.shot = true;
        }
        return null;
    }

    protected void onStop(Task interruptTask) {
        controller.getInputControls().release(Input.CLICK_RIGHT);
    }

    public boolean isFinished() {
        return this.shot;
    }

    protected boolean isEqual(Task other) {
        return false;
    }

    protected String toDebugString() {
        return "Shooting arrow at " + this.target.getType().getTranslationKey();
    }
}
