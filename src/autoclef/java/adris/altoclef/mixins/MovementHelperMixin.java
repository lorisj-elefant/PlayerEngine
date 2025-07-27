package adris.altoclef.mixins;

import baritone.pathing.movement.MovementHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.InfestedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({MovementHelper.class})
public interface MovementHelperMixin {
  @Redirect(method = {"avoidBreaking"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;", ordinal = 1))
  private static Block allowInfested(BlockState instance) {
    Block block = instance.getBlock();
    if (block instanceof InfestedBlock) {
      InfestedBlock infestedBlock = (InfestedBlock)block;
      return infestedBlock.getRegularBlock();
    } 
    return instance.getBlock();
  }
}
