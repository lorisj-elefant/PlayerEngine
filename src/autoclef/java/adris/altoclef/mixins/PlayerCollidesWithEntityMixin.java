package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.PlayerCollidedWithEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({PlayerEntity.class})
public class PlayerCollidesWithEntityMixin {
  @Redirect(method = {"collideWithEntity"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onPlayerCollision(Lnet/minecraft/entity/player/PlayerEntity;)V"))
  private void onCollideWithEntity(Entity self, PlayerEntity player) {
    if (player instanceof net.minecraft.client.network.ClientPlayerEntity)
      EventBus.publish(new PlayerCollidedWithEntityEvent(player, self)); 
    self.onPlayerCollision(player);
  }
}
