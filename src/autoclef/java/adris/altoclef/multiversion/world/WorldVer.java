package adris.altoclef.multiversion.world;

import net.minecraft.registry.Holder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class WorldVer {
  public static boolean isBiomeAtPos(World world, RegistryKey<Biome> biome, BlockPos pos) {
    Holder<Biome> b = world.getBiome(pos);
    return b.isRegistryKey(biome);
  }
  
  public static boolean isBiome(Holder<Biome> biome1, RegistryKey<Biome> biome2) {
    return biome1.isRegistryKey(biome2);
  }
  
  public static int getBottomY(World world) {
    return world.getBottomY();
  }
  
  public static int getTopY(World world) {
    return world.getTopY();
  }
  
  private static boolean isOutOfHeightLimit(World world, BlockPos pos) {
    return world.isOutOfHeightLimit(pos);
  }
}
