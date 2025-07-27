package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.EntitySwungEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public abstract class EntityAnimationSwungMixin {
  @Inject(method = {"onEntityAnimation"}, at = {@At("HEAD")})
  private void onEntityAnimation(EntityAnimationS2CPacket packet, CallbackInfo ci) {
    MinecraftClient client = MinecraftClient.getInstance();
    Entity entity = client.world.getEntityById(packet.getId());
    if (entity == null)
      return; 
    int id = packet.getAnimationId();
    if (id == 0 || id == 3)
      EventBus.publish(new EntitySwungEvent(entity)); 
  }
}
