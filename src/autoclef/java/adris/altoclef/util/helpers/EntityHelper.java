package adris.altoclef.util.helpers;

import adris.altoclef.AltoClefController;
import adris.altoclef.multiversion.DamageSourceWrapper;
import adris.altoclef.multiversion.MethodWrapper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class EntityHelper {
    public static final double ENTITY_GRAVITY = 0.08D;

    public static boolean isAngryAtPlayer(AltoClefController mod, Entity mob) {
        boolean hostile = isProbablyHostileToPlayer(mod, mob);
        if (mob instanceof MobEntity entity) {
            return (hostile && entity.getTarget() == mod.getPlayer());
        }
        return hostile;
    }

    public static boolean isProbablyHostileToPlayer(AltoClefController mod, Entity entity) {
        if (entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;
            if (mob instanceof SlimeEntity) {
                SlimeEntity slime = (SlimeEntity) mob;
                return (slime.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) > 0.0D);
            }
            if (mob instanceof PiglinEntity) {
                PiglinEntity piglin = (PiglinEntity) mob;
                return (piglin.isAttacking() && !isTradingPiglin((Entity) mob) && piglin.isAdult());
            }
            if (mob instanceof EndermanEntity) {
                EndermanEntity enderman = (EndermanEntity) mob;
                return enderman.isAngry();
            }
            if (mob instanceof ZombifiedPiglinEntity) {
                ZombifiedPiglinEntity zombifiedPiglin = (ZombifiedPiglinEntity) mob;
                return zombifiedPiglin.isAttacking();
            }
            return (mob.isAttacking() || mob instanceof net.minecraft.entity.mob.HostileEntity);
        }
        return false;
    }

    public static boolean isTradingPiglin(Entity entity) {
        if (entity instanceof PiglinEntity) {
            PiglinEntity pig = (PiglinEntity) entity;
            if (pig.getItemsHand() != null)
                for (ItemStack stack : pig.getItemsHand()) {
                    if (stack.getItem().equals(Items.GOLD_INGOT))
                        return true;
                }
        }
        return false;
    }

    public static double calculateResultingPlayerDamage(LivingEntity player, DamageSource src, double damageAmount) {
        DamageSourceWrapper source = DamageSourceWrapper.of(src);
        if (player.isInvulnerableTo(src))
            return 0.0D;
        if (!source.bypassesArmor())
            damageAmount = MethodWrapper.getDamageLeft((LivingEntity) player, damageAmount, src, player.getArmor(), player.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
        if (!source.bypassesShield()) {
            if (player.hasStatusEffect(StatusEffects.RESISTANCE) && source.isOutOfWorld()) {
                float k = ((player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5);
                float j = 25.0F - k;
                double f = damageAmount * j;
                double g = damageAmount;
                damageAmount = Math.max(f / 25.0D, 0.0D);
            }
            if (damageAmount <= 0.0D) {
                damageAmount = 0.0D;
            } else {
                float k = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), src);
                if (k > 0.0F)
                    damageAmount = DamageUtil.getInflictedDamage((float) damageAmount, k);
            }
        }
        damageAmount = Math.max(damageAmount - player.getAbsorptionAmount(), 0.0D);
        return damageAmount;
    }
}
