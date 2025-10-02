package adris.altoclef.util.helpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.AltoClefController;
import adris.altoclef.multiversion.DamageSourceWrapper;
import adris.altoclef.multiversion.MethodWrapper;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;

import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;

import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

import net.minecraft.world.entity.animal.Sheep;

import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EntityHelper {
   public static final double ENTITY_GRAVITY = 0.08;
   private static final Map<String, Class<? extends Entity>> ENTITY_MAP;
   public static final Logger LOGGER = LogManager.getLogger();

   private static String normalize(String s) {
      // iron-golem, ironGolem, IRONGOLEM, ...
      return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
   }

   private static void put(Map<String, Class<? extends Entity>> m, String key, Class<? extends Entity> cls) {
      m.put(normalize(key), cls);
   }

   static {
      Map<String, Class<? extends Entity>> m = new HashMap<>();

      // ---- Animals ----
      put(m, "bee", Bee.class);
      put(m, "cat", Cat.class);
      put(m, "chicken", Chicken.class);
      put(m, "cod", Cod.class);
      put(m, "cow", Cow.class);
      put(m, "dolphin", Dolphin.class);
      put(m, "fox", Fox.class);
      put(m, "iron_golem", IronGolem.class);
      put(m, "mushroom_cow", MushroomCow.class);
      put(m, "ocelot", Ocelot.class);
      put(m, "panda", Panda.class);
      put(m, "parrot", Parrot.class);
      put(m, "pig", Pig.class);
      put(m, "polar_bear", PolarBear.class);
      put(m, "pufferfish", Pufferfish.class);
      put(m, "rabbit", Rabbit.class);
      put(m, "salmon", Salmon.class);
      put(m, "sheep", Sheep.class);
      put(m, "snow_golem", SnowGolem.class);
      put(m, "squid", Squid.class);
      put(m, "tropical_fish", TropicalFish.class);
      put(m, "turtle", Turtle.class);
      put(m, "wolf", Wolf.class);

      // ---- Horses ----
      put(m, "donkey", Donkey.class);
      put(m, "horse", Horse.class);
      put(m, "llama", Llama.class);
      put(m, "mule", Mule.class);

      // ---- Vehicles ----
      put(m, "boat", Boat.class);
      put(m, "minecart", Minecart.class);

      // ---- Hostiles ----
      put(m, "blaze", Blaze.class);
      put(m, "cave_spider", CaveSpider.class);
      put(m, "creeper", Creeper.class);
      put(m, "elder_guardian", ElderGuardian.class);
      put(m, "enderman", EnderMan.class);
      put(m, "endermite", Endermite.class);
      put(m, "evoker", Evoker.class);
      put(m, "ghast", Ghast.class);
      put(m, "giant", Giant.class);
      put(m, "guardian", Guardian.class);
      put(m, "husk", Husk.class);
      put(m, "illusioner", Illusioner.class);
      put(m, "magma_cube", MagmaCube.class);
      put(m, "monster", Monster.class);
      put(m, "phantom", Phantom.class);
      put(m, "ravager", Ravager.class);
      put(m, "shulker", Shulker.class);
      put(m, "silverfish", Silverfish.class);
      put(m, "skeleton", Skeleton.class);
      put(m, "slime", Slime.class);
      put(m, "spellcaster_illager", SpellcasterIllager.class);
      put(m, "spider", Spider.class);
      put(m, "stray", Stray.class);
      put(m, "strider", Strider.class);
      put(m, "vex", Vex.class);
      put(m, "vindicator", Vindicator.class);
      put(m, "witch", Witch.class);
      put(m, "wither_skeleton", WitherSkeleton.class);
      put(m, "zoglin", Zoglin.class);
      put(m, "zombie", Zombie.class);
      put(m, "zombie_villager", ZombieVillager.class);
      put(m, "zombified_piglin", ZombifiedPiglin.class);

      ENTITY_MAP = Collections.unmodifiableMap(m);
   }

   public static boolean isAngryAtPlayer(AltoClefController mod, Entity mob) {
      boolean hostile = isProbablyHostileToPlayer(mod, mob);
      return !(mob instanceof Mob entity) ? hostile : hostile && entity.getTarget() == mod.getPlayer();
   }

   public static boolean isProbablyHostileToPlayer(AltoClefController mod, Entity entity) {
      if (entity instanceof Mob mob) {
         if (mob instanceof Slime slime) {
            return slime.getAttributeValue(Attributes.ATTACK_DAMAGE) > 0.0;
         } else if (mob instanceof Piglin piglin) {
            return piglin.isAggressive() && !isTradingPiglin(mob) && piglin.isAdult();
         } else if (mob instanceof EnderMan enderman) {
            return enderman.isCreepy();
         } else {
            return mob instanceof ZombifiedPiglin zombifiedPiglin ? zombifiedPiglin.isAggressive()
                  : mob.isAggressive() || mob instanceof Monster;
         }
      } else {
         return false;
      }
   }

   public static boolean isTradingPiglin(Entity entity) {
      if (entity instanceof Piglin pig && pig.getHandSlots() != null) {
         for (ItemStack stack : pig.getHandSlots()) {
            if (stack.getItem().equals(Items.GOLD_INGOT)) {
               return true;
            }
         }
      }

      return false;
   }

   public static double calculateResultingPlayerDamage(LivingEntity player, DamageSource src, double damageAmount) {
      DamageSourceWrapper source = DamageSourceWrapper.of(src);
      if (player.isInvulnerableTo(src)) {
         return 0.0;
      } else {
         if (!source.bypassesArmor()) {
            damageAmount = MethodWrapper.getDamageLeft(
                  player, damageAmount, src, (double) player.getArmorValue(),
                  player.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
         }

         if (!source.bypassesShield()) {
            if (player.hasEffect(MobEffects.DAMAGE_RESISTANCE) && source.isOutOfWorld()) {
               float k = (player.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
               float j = 25.0F - k;
               double f = damageAmount * j;
               damageAmount = Math.max(f / 25.0, 0.0);
            }

            if (damageAmount <= 0.0) {
               damageAmount = 0.0;
            } else {
               float k = EnchantmentHelper.getDamageProtection(player.getArmorSlots(), src);
               if (k > 0.0F) {
                  damageAmount = CombatRules.getDamageAfterMagicAbsorb((float) damageAmount, k);
               }
            }
         }

         return Math.max(damageAmount - player.getAbsorptionAmount(), 0.0);
      }
   }

   public static Optional<Class<? extends Entity>> stringToEntityClass(String inputName) {
      if (inputName == null)
         return Optional.empty();
      // look up by normalized input
      Class<? extends Entity> cls = ENTITY_MAP.get(normalize(inputName));
      // fallback: if the user passed a FQCN like "net.minecraft....Sheep"
      if (cls == null) {
         int dot = inputName.lastIndexOf('.');
         String simple = (dot >= 0) ? inputName.substring(dot + 1) : inputName;
         if (simple.endsWith(".class"))
            simple = simple.substring(0, simple.length() - 6);
         cls = ENTITY_MAP.get(normalize(simple));
      }
      return Optional.ofNullable(cls);
   }
}
