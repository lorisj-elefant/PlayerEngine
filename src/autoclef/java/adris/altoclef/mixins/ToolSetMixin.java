package adris.altoclef.mixins;

import baritone.api.entity.IInventoryProvider;
import baritone.utils.ToolSet;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ToolSet.class})
public class ToolSetMixin {
  @Shadow
  @Final
  private LivingEntity player;
  
  @Inject(method = {"getBestSlot(Lnet/minecraft/block/Block;ZZ)I"}, at = {@At("HEAD")}, cancellable = true)
  public void inject(Block b, boolean preferSilkTouch, boolean pathingCalculation, CallbackInfoReturnable<Integer> cir) {
    if (b.getDefaultState().getBlock().getHardness() == 0.0F)
      cir.setReturnValue((((IInventoryProvider) this.player).getLivingInventory()).selectedSlot);
  }
  
//  @Redirect(method = {"getBestSlot(Lnet/minecraft/block/Block;ZZ)I"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getDamage()I"))
//  public int redirected(ItemStack stack, Block block) {
//    if (StorageHelper.shouldSaveStack(AltoClefController.getInstance(), block, stack))
//      return 100000;
//    return stack.getDamage();
//  }
//
//  @Redirect(method = {"getBestSlot(Lnet/minecraft/block/Block;ZZ)I"}, at = @At(value = "FIELD", target = "Lbaritone/api/Settings;itemSaver:Lbaritone/api/Settings$Setting;"), remap = false)
//  public Settings.Setting<Boolean> redirected(Settings instance) {
//    if (StorageHelper.shouldSaveStack(AltoClefController.getInstance(), block, stack))
//      return trueSetting;
//    return instance.itemSaver;
//  }
}
