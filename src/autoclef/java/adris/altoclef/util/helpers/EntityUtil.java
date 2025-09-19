package adris.altoclef.util.helpers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.EntityIMixin;
import noppes.npcs.mixin.EntityLivingIMixin;
import noppes.npcs.mixin.WalkAnimationStateMixin;
import noppes.npcs.shared.common.util.LogWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EntityUtil {

    public static void Copy(LivingEntity copied, LivingEntity entity) {
        ((EntityIMixin) entity).setLevel(copied.level());

        entity.deathTime = copied.deathTime;
        entity.walkDist = copied.walkDist;
        entity.walkDistO = copied.walkDist;
        entity.moveDist = copied.moveDist;

        entity.zza = copied.zza;
        entity.xxa = copied.xxa;
        entity.setOnGround(copied.onGround());
        entity.fallDistance = copied.fallDistance;
        entity.setJumping(((EntityLivingIMixin) copied).jumping());

        List<SynchedEntityData.DataItem<?>> copiedData = ((ISynchedEntityData) copied.getEntityData()).getAll();
        List<SynchedEntityData.DataItem<?>> data = ((ISynchedEntityData) entity.getEntityData()).getAll();
        for (SynchedEntityData.DataItem<?> entry : copiedData) {
            if (data.stream().anyMatch(e -> e.getAccessor() == entry.getAccessor())) {
                if (entry.getValue() instanceof SynchedEntityData.DataValue) {
                    entity.getEntityData().set((EntityDataAccessor<Object>) entry.getAccessor(),
                            ((SynchedEntityData.DataValue) entry.getValue()).value());
                }
            }
        }

        entity.xo = copied.xo;
        entity.yo = copied.yo;
        entity.zo = copied.zo;

        entity.setPos(copied.getX(), copied.getY(), copied.getZ());
        // entity.setEntityBoundingBox(copied.getEntityBoundingBox());

        entity.xOld = copied.xOld;
        entity.yOld = copied.yOld;
        entity.zOld = copied.zOld;

        entity.setDeltaMovement(copied.getDeltaMovement());

        entity.setXRot(copied.getXRot());
        entity.setYRot(copied.getYRot());
        entity.xRotO = copied.xRotO;
        entity.yRotO = copied.yRotO;
        entity.yHeadRot = copied.yHeadRot;
        entity.yHeadRotO = copied.yHeadRotO;
        entity.yBodyRot = copied.yBodyRot;
        entity.yBodyRotO = copied.yBodyRotO;

        ((EntityLivingIMixin) entity).useItemRemaining(copied.getUseItemRemainingTicks());

        ((WalkAnimationStateMixin) entity.walkAnimation).setPosition(copied.walkAnimation.position());
        ((EntityLivingIMixin) entity).animStep(((EntityLivingIMixin) copied).animStep());
        ((EntityLivingIMixin) entity).animStepO(((EntityLivingIMixin) copied).animStepO());
        ((EntityLivingIMixin) entity).swimAmount(((EntityLivingIMixin) copied).swimAmount());
        ((EntityLivingIMixin) entity).swimAmountO(((EntityLivingIMixin) copied).swimAmountO());
        entity.swinging = copied.swinging;
        entity.swingTime = copied.swingTime;

        entity.walkAnimation.setSpeed(copied.walkAnimation.speed());
        ((WalkAnimationStateMixin) entity.walkAnimation)
                .setSpeedOld(((WalkAnimationStateMixin) copied.walkAnimation).getSpeedOld());
        entity.attackAnim = copied.attackAnim;
        entity.oAttackAnim = copied.oAttackAnim;

        entity.tickCount = copied.tickCount;

        entity.setHealth(Math.min(copied.getHealth(), entity.getMaxHealth()));
        entity.hurtTime = copied.hurtTime;
        entity.deathTime = copied.deathTime;

        // entity.getPersistentData().merge(copied.getPersistentData());

        // if(entity.getVehicle() != copied.getVehicle())
        // entity.vehicle = copied.vehicle;

        if (entity instanceof Player && copied instanceof Player) {
            Player ePlayer = (Player) entity;
            Player cPlayer = (Player) copied;

            ePlayer.bob = cPlayer.bob;
            ePlayer.oBob = cPlayer.oBob;

            ePlayer.xCloakO = cPlayer.xCloakO;
            ePlayer.yCloakO = cPlayer.yCloakO;
            ePlayer.zCloakO = cPlayer.zCloakO;
            ePlayer.xCloak = cPlayer.xCloak;
            ePlayer.yCloak = cPlayer.yCloak;
            ePlayer.zCloak = cPlayer.zCloak;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            entity.setItemSlot(slot, copied.getItemBySlot(slot));
        }

        if (entity instanceof EnderDragon) {
            entity.setXRot(entity.getXRot() + 180);
        }

        ((EntityIMixin) entity).removal(((EntityIMixin) copied).removal());
        entity.deathTime = copied.deathTime;

        entity.tickCount = copied.tickCount;

        if (entity instanceof EnderDragon) {
            entity.setYRot(entity.getYRot() + 180);
        }
        if (entity instanceof Chicken) {
            ((Chicken) entity).flap = copied.onGround() ? 0 : 1;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            entity.setItemSlot(slot, copied.getItemBySlot(slot));
        }

        if (copied instanceof EntityNPCInterface && entity instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) copied;
            EntityNPCInterface target = (EntityNPCInterface) entity;

            target.textureLocation = npc.textureLocation;
            target.textureGlowLocation = npc.textureGlowLocation;
            target.textureCloakLocation = npc.textureCloakLocation;
            target.display = npc.display;
            target.inventory = npc.inventory;
            if (npc.job.getType() == JobType.PUPPET) {
                target.job = npc.job;
            }
            if (target.currentAnimation != npc.currentAnimation) {
                target.currentAnimation = npc.currentAnimation;
                npc.refreshDimensions();
            }
            target.setDataWatcher(npc.getEntityData());
        }

        if (entity instanceof EntityCustomNpc && copied instanceof EntityCustomNpc) {
            EntityCustomNpc npc = (EntityCustomNpc) copied;
            EntityCustomNpc target = (EntityCustomNpc) entity;

            target.modelData = npc.modelData.copy();
            target.modelData.setEntity(null);
        }
    }

    private <T> void setData(LivingEntity entity, List<SynchedEntityData.DataItem<T>> copiedData,
            List<SynchedEntityData.DataItem<T>> data) {
        for (SynchedEntityData.DataItem<? extends Object> entry : copiedData) {
            if (data.stream().anyMatch(e -> e.getAccessor() == entry.getAccessor())) {
                entity.getEntityData().set((EntityDataAccessor<T>) entry.getAccessor(), (T) entry.getValue());
            }
        }
    }

    public static void setRecentlyHit(LivingEntity entity) {
        ((EntityLivingIMixin) entity).lastHurtByPlayerTime(100);
    }

    private static HashMap<EntityType<? extends Entity>, Class> entityClasses = new HashMap<EntityType<? extends Entity>, Class>();

    public static HashMap<EntityType<? extends Entity>, Class> getAllEntitiesClasses(Level level) {
        if (!entityClasses.isEmpty())
            return entityClasses;
        HashMap<EntityType<? extends Entity>, Class> data = new HashMap<EntityType<? extends Entity>, Class>();

        for (EntityType<? extends Entity> ent : BuiltInRegistries.ENTITY_TYPE) {
            try {
                Entity e = ent.create(level);
                if (e != null) {
                    if (LivingEntity.class.isAssignableFrom(e.getClass())) {
                        data.put(ent, e.getClass());
                    }
                    e.discard();
                }
            } catch (Exception e) {

            }
        }
        return entityClasses = data;
    }

    public static HashMap<EntityType<? extends Entity>, Class> getAllEntitiesClassesNoNpcs(Level level) {
        HashMap<EntityType<? extends Entity>, Class> data = new HashMap<EntityType<? extends Entity>, Class>(
                getAllEntitiesClasses(level));
        Iterator<Map.Entry<EntityType<? extends Entity>, Class>> ita = data.entrySet().iterator();
        while (ita.hasNext()) {
            Map.Entry<EntityType<? extends Entity>, Class> entry = ita.next();
            if (EntityNPCInterface.class.isAssignableFrom(entry.getValue())
                    || !LivingEntity.class.isAssignableFrom(entry.getValue())) {
                ita.remove();
            }
        }
        return data;
    }

    public static HashMap<String, ResourceLocation> getAllEntities(Level level, boolean withNpcs) {
        HashMap<String, ResourceLocation> data = new HashMap<String, ResourceLocation>();

        for (EntityType<? extends Entity> ent : BuiltInRegistries.ENTITY_TYPE) {
            try {
                Entity e = ent.create(level);
                if (e != null) {
                    if (LivingEntity.class.isAssignableFrom(e.getClass())
                            && (withNpcs || !EntityNPCInterface.class.isAssignableFrom(e.getClass()))) {
                        data.put(ent.getDescriptionId(), BuiltInRegistries.ENTITY_TYPE.getKey(ent));
                    }
                    e.discard();
                }
            } catch (Throwable e) {
                LogWriter.except(e);
            }
        }

        return data;
    }
}