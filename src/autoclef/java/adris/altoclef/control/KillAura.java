package adris.altoclef.control;

import adris.altoclef.AltoClefController;
import adris.altoclef.chains.MobDefenseChain;
import adris.altoclef.mixins.LivingEntityMixin;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StlHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import baritone.api.utils.input.Input;
import baritone.behavior.PathingBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.mob.warden.WardenEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KillAura {
    private final List<Entity> targets = new ArrayList<>();

    boolean shielding = false;

    private double forceFieldRange = Double.POSITIVE_INFINITY;

    private Entity forceHit = null;

    public boolean attackedLastTick = false;

    public static void equipWeapon(AltoClefController mod) {
        ToolItem toolItem = MobDefenseChain.getBestWeapon(mod);
        if (toolItem != null)
            mod.getSlotHandler().forceEquipItem((Item) toolItem);
    }

    public void tickStart() {
        this.targets.clear();
        this.forceHit = null;
        this.attackedLastTick = false;
    }

    public void applyAura(Entity entity) {
        this.targets.add(entity);
        if (entity instanceof net.minecraft.entity.projectile.FireballEntity)
            this.forceHit = entity;
    }

    public void setRange(double range) {
        this.forceFieldRange = range;
    }

    public void tickEnd(AltoClefController mod) {
        Optional<Entity> entities = this.targets.stream().min(StlHelper.compareValues(entity -> Double.valueOf(entity.squaredDistanceTo((Entity) mod.getPlayer()))));
        if (entities.isPresent() &&
                !mod.getEntityTracker().entityFound(new Class[]{PotionEntity.class}) && (Double.isInfinite(this.forceFieldRange) || ((Entity) entities.get()).squaredDistanceTo((Entity) mod.getPlayer()) < this.forceFieldRange * this.forceFieldRange || ((Entity) entities
                .get()).squaredDistanceTo((Entity) mod.getPlayer()) < 40.0D) &&
                !mod.getMLGBucketChain().isFalling(mod) && mod.getMLGBucketChain().doneMLG() &&
                !mod.getMLGBucketChain().isChorusFruiting()) {
            Slot offhandSlot = PlayerSlot.getOffhandSlot(mod.getInventory());
            Item offhandItem = StorageHelper.getItemStackInSlot(offhandSlot).getItem();
            if (((Entity) entities.get()).getClass() != CreeperEntity.class && ((Entity) entities.get()).getClass() != HoglinEntity.class && ((Entity) entities
                    .get()).getClass() != ZoglinEntity.class && ((Entity) entities.get()).getClass() != WardenEntity.class && ((Entity) entities
                    .get()).getClass() != WitherEntity.class && (mod
                    .getItemStorage().hasItem(new Item[]{Items.SHIELD}) || mod.getItemStorage().hasItemInOffhand(mod, Items.SHIELD)) &&
                    //TODO !mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandItem) &&
                    mod.getBaritone().getPathingBehavior().isSafeToCancel()) {
                LookHelper.lookAt(mod, ((Entity) entities.get()).getEyePos());
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.getOffhandSlot(mod.getInventory()));
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else if (!WorldHelper.isSurroundedByHostiles(mod)) {
                    startShielding(mod);
                }
            }
            performDelayedAttack(mod);
        } else {
            stopShielding(mod);
        }
        switch (mod.getModSettings().getForceFieldStrategy().ordinal()) {
            case 1:
                performFastestAttack(mod);
                break;
            case 3:
                if (this.forceHit != null) {
                    attack(mod, this.forceHit, true);
                    break;
                }
                if (!mod.getFoodChain().needsToEat() && !mod.getMLGBucketChain().isFalling(mod) && mod
                        .getMLGBucketChain().doneMLG() && !mod.getMLGBucketChain().isChorusFruiting())
                    performDelayedAttack(mod);
                break;
            case 2:
                performDelayedAttack(mod);
                break;
        }
    }

    private void performDelayedAttack(AltoClefController mod) {
        if (!mod.getFoodChain().needsToEat() && !mod.getMLGBucketChain().isFalling(mod) && mod
                .getMLGBucketChain().doneMLG() && !mod.getMLGBucketChain().isChorusFruiting()) {
            if (this.forceHit != null)
                attack(mod, this.forceHit, true);
            if (this.targets.isEmpty())
                return;
            Optional<Entity> toHit = this.targets.stream().min(StlHelper.compareValues(entity -> Double.valueOf(entity.squaredDistanceTo((Entity) mod.getPlayer()))));
            if (mod.getPlayer() == null || getAttackCooldownProgress(mod.getPlayer(), 0.0F) < 1.0F)
                return;
            toHit.ifPresent(entity -> attack(mod, entity, true));
        }
    }

    public float getAttackCooldownProgressPerTick(LivingEntity entity) {
        return (float) ((double) 1.0F / 4f * (double) 20.0F);
    }

    public float getAttackCooldownProgress(LivingEntity entity, float baseTime) {
        return MathHelper.clamp(((float) ((LivingEntityMixin) entity).getLastAttackedTicks() + baseTime) / this.getAttackCooldownProgressPerTick(entity), 0.0F, 1.0F);
    }


    private void performFastestAttack(AltoClefController mod) {
        if (!mod.getFoodChain().needsToEat() && !mod.getMLGBucketChain().isFalling(mod) && mod
                .getMLGBucketChain().doneMLG() && !mod.getMLGBucketChain().isChorusFruiting())
            for (Entity entity : this.targets)
                attack(mod, entity);
    }

    private void attack(AltoClefController mod, Entity entity) {
        attack(mod, entity, false);
    }

    private void attack(AltoClefController mod, Entity entity, boolean equipWeapon) {
        if (entity == null)
            return;
        if (!(entity instanceof net.minecraft.entity.projectile.FireballEntity)) {
            double xAim = entity.getX();
            double yAim = entity.getY() + entity.getHeight() / 1.4D;
            double zAim = entity.getZ();
            LookHelper.lookAt(mod, new Vec3d(xAim, yAim, zAim));
        }
        if (Double.isInfinite(this.forceFieldRange) || entity.squaredDistanceTo((Entity) mod.getPlayer()) < this.forceFieldRange * this.forceFieldRange || entity
                .squaredDistanceTo((Entity) mod.getPlayer()) < 40.0D) {
            boolean canAttack;
            if (entity instanceof net.minecraft.entity.projectile.FireballEntity)
                mod.getControllerExtras().attack(entity);
            if (equipWeapon) {
                equipWeapon(mod);
                canAttack = true;
            } else {
                canAttack = mod.getSlotHandler().forceDeequipHitTool();
            }
            if (canAttack && (
                    mod.getPlayer().isOnGround() || mod.getPlayer().getVelocity().getY() < 0.0D || mod.getPlayer().isTouchingWater())) {
                this.attackedLastTick = true;
                mod.getControllerExtras().attack(entity);
            }
        }
    }

    public void startShielding(AltoClefController mod) {
        this.shielding = true;
        ((PathingBehavior) mod.getBaritone().getPathingBehavior()).requestPause();
        mod.getExtraBaritoneSettings().setInteractionPaused(true);
        if (!mod.getPlayer().isBlocking()) {
            ItemStack handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot(mod.getInventory()));
            if (ItemVer.isFood(handItem)) {
                List<ItemStack> spaceSlots = mod.getItemStorage().getItemStacksPlayerInventory(false);
                if (!spaceSlots.isEmpty())
                    for (ItemStack spaceSlot : spaceSlots) {
                        if (spaceSlot.isEmpty()) {
                            mod.getSlotHandler().clickSlot(PlayerSlot.getEquipSlot(mod.getInventory()), 0, SlotActionType.QUICK_MOVE);
                            return;
                        }
                    }
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                garbage.ifPresent(slot -> mod.getSlotHandler().forceEquipItem(StorageHelper.getItemStackInSlot(slot).getItem()));
            }
        }
        mod.getInputControls().hold(Input.SNEAK);
        mod.getInputControls().hold(Input.CLICK_RIGHT);
    }

    public void stopShielding(AltoClefController mod) {
        if (this.shielding) {
            ItemStack cursor = StorageHelper.getItemStackInCursorSlot(mod);
            if (ItemVer.isFood(cursor)) {
                Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false).or(() -> StorageHelper.getGarbageSlot(mod));
                if (toMoveTo.isPresent()) {
                    Slot garbageSlot = toMoveTo.get();
                    mod.getSlotHandler().clickSlot(garbageSlot, 0, SlotActionType.PICKUP);
                }
            }
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.CLICK_RIGHT);
            mod.getInputControls().release(Input.JUMP);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
            this.shielding = false;
        }
    }

    public boolean isShielding() {
        return this.shielding;
    }

    public enum Strategy {
        OFF,
        FASTEST,
        DELAY,
        SMART
    }
}
