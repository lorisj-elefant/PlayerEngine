package adris.altoclef.multiversion;

import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

public class MethodWrapper {
  public static Entity getRenderedEntity(MobSpawnerLogic logic, World world, BlockPos pos) {
    return logic.getRenderedEntity(world, RandomGenerator.createLegacy(), pos);
  }
  
  public static float getDamageLeft(LivingEntity armorWearer, double damage, DamageSource source, double armor, double armorToughness) {
    return getDamageLeft(armorWearer, (float)damage, source, (float)armor, (float)armorToughness);
  }
  
  public static float getDamageLeft(LivingEntity armorWearer, float damage, DamageSource source, float armor, float armorToughness) {
    return DamageUtil.getDamageLeft(damage, armor, armorToughness);
  }
}
