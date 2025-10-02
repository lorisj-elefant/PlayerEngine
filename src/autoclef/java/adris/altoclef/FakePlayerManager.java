package adris.altoclef;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class FakePlayerManager {
    FakePlayer playerCopy;

    public FakePlayerManager(ServerLevel world, UUID uuid, String name) {
        this.playerCopy = FakePlayer.get(world, new GameProfile(uuid, name));
        // this.playerCopy = new ServerPlayer(world.getServer(), world,
        // new GameProfile(UUID.fromString("TEMP"), name));
    }

    public void update(AltoClefController controller) {
        LivingEntity original = controller.getPlayer();

        // ((EntityIMixin) entity).setLevel(copied.level());
        // ->
        playerCopy.setServerLevel(controller.getWorld().getLevel());

        playerCopy.deathTime = original.deathTime;
        playerCopy.walkDist = original.walkDist;
        playerCopy.walkDistO = original.walkDist;
        playerCopy.moveDist = original.moveDist;

        playerCopy.zza = original.zza;
        playerCopy.xxa = original.xxa;
        playerCopy.setOnGround(original.onGround());
        playerCopy.fallDistance = original.fallDistance;

        // entity.setJumping(((EntityLivingIMixin) copied).jumping());

        // List<SynchedEntityData.DataItem<?>> copiedData = ((ISynchedEntityData)
        // copied.getEntityData()).getAll();
        // List<SynchedEntityData.DataItem<?>> data = ((ISynchedEntityData)
        // entity.getEntityData()).getAll();
        // for (SynchedEntityData.DataItem<?> entry : copiedData) {
        // if (data.stream().anyMatch(e -> e.getAccessor() == entry.getAccessor())) {
        // if (entry.getValue() instanceof SynchedEntityData.DataValue) {
        // entity.getEntityData().set((EntityDataAccessor<Object>) entry.getAccessor(),
        // ((SynchedEntityData.DataValue) entry.getValue()).value());
        // }
        // }
        // }

        playerCopy.xo = original.xo;
        playerCopy.yo = original.yo;
        playerCopy.zo = original.zo;

        playerCopy.setPos(original.getX(), original.getY(), original.getZ());
        // entity.setEntityBoundingBox(copied.getEntityBoundingBox());

        playerCopy.xOld = original.xOld;
        playerCopy.yOld = original.yOld;
        playerCopy.zOld = original.zOld;

        playerCopy.setDeltaMovement(original.getDeltaMovement());

        playerCopy.setXRot(original.getXRot());
        playerCopy.setYRot(original.getYRot());
        playerCopy.xRotO = original.xRotO;
        playerCopy.yRotO = original.yRotO;
        playerCopy.yHeadRot = original.yHeadRot;
        playerCopy.yHeadRotO = original.yHeadRotO;
        playerCopy.yBodyRot = original.yBodyRot;
        playerCopy.yBodyRotO = original.yBodyRotO;

        // ((EntityLivingIMixin)
        // entity).useItemRemaining(copied.getUseItemRemainingTicks());

        // ((WalkAnimationStateMixin)
        // entity.walkAnimation).setPosition(copied.walkAnimation.position());
        // ((EntityLivingIMixin) entity).animStep(((EntityLivingIMixin)
        // copied).animStep());
        // ((EntityLivingIMixin) entity).animStepO(((EntityLivingIMixin)
        // copied).animStepO());
        // ((EntityLivingIMixin) entity).swimAmount(((EntityLivingIMixin)
        // copied).swimAmount());
        // ((EntityLivingIMixin) entity).swimAmountO(((EntityLivingIMixin)
        // copied).swimAmountO());
        playerCopy.swinging = original.swinging;
        playerCopy.swingTime = original.swingTime;

        playerCopy.walkAnimation.setSpeed(original.walkAnimation.speed());

        // ((WalkAnimationStateMixin) entity.walkAnimation)
        // .setSpeedOld(((WalkAnimationStateMixin) copied.walkAnimation).getSpeedOld());
        playerCopy.attackAnim = original.attackAnim;
        playerCopy.oAttackAnim = original.oAttackAnim;

        playerCopy.tickCount = original.tickCount;

        playerCopy.setHealth(Math.min(original.getHealth(), playerCopy.getMaxHealth()));
        playerCopy.hurtTime = original.hurtTime;
        playerCopy.deathTime = original.deathTime;

        // entity.getPersistentData().merge(copied.getPersistentData());

        // if(entity.getVehicle() != copied.getVehicle())
        // entity.vehicle = copied.vehicle;

        if (playerCopy instanceof Player && original instanceof Player) {
            Player ePlayer = (Player) playerCopy;
            Player cPlayer = (Player) original;

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
            playerCopy.setItemSlot(slot, original.getItemBySlot(slot));
        }

        // if (entity instanceof EnderDragon) {
        // entity.setXRot(entity.getXRot() + 180);
        // }

        // ((EntityIMixin) entity).removal(((EntityIMixin) copied).removal());
        playerCopy.deathTime = original.deathTime;

        playerCopy.tickCount = original.tickCount;

        // if (entity instanceof EnderDragon) {
        // entity.setYRot(entity.getYRot() + 180);
        // }
        // if (entity instanceof Chicken) {
        // ((Chicken) entity).flap = copied.onGround() ? 0 : 1;
        // }

    }

    public FakePlayer getPlayerCopy() {
        return playerCopy;
    }

    public void updateOriginalItem(AltoClefController mod) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            mod.getPlayer().setItemSlot(slot, playerCopy.getItemBySlot(slot));
        }
    }

}
