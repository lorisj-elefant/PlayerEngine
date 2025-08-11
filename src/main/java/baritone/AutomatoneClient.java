package baritone;

import baritone.client.CustomFishingBobberRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD, modid= Automatone.MOD_ID, value = Dist.CLIENT)
public final class AutomatoneClient {
   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent
   public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event){
      event.registerEntityRenderer(Automatone.FISHING_BOBBER, CustomFishingBobberRenderer::new);
   }
}
