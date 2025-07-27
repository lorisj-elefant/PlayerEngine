package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.PlayerDamageEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerEntity.class})
public class PlayerDamageMixin {
  @Inject(method = {"damage"}, at = {@At("HEAD")})
  public void applyDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
    EventBus.publish(new PlayerDamageEvent(source, amount));
  }
}
