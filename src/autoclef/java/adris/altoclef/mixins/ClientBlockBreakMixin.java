package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockBreakingCancelEvent;
import adris.altoclef.eventbus.events.BlockBreakingEvent;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public final class ClientBlockBreakMixin {
    @Unique
    private static int breakCancelFrames;

    @Inject(method = {"updateBlockBreakingProgress"}, at = {@At("HEAD")})
    private void onBreakUpdate(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> ci) {
        EventBus.publish(new BlockBreakingEvent(pos));
    }

    @Inject(method = {"cancelBlockBreaking"}, at = {@At("HEAD")})
    private void cancelBlockBreaking(CallbackInfo ci) {
        if (breakCancelFrames-- == 0)
            EventBus.publish(new BlockBreakingCancelEvent());
    }
}
