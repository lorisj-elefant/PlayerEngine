package adris.altoclef.multiversion;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public class BlockTagVer {
  public static boolean isWool(Block block) {
    return Registries.BLOCK.getKey(block).map(e -> Registries.BLOCK.getHolderOrThrow(e).streamTags().anyMatch((t -> t == BlockTags.WOOL))).orElse(Boolean.FALSE);
  }
}
