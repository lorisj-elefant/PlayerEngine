package baritone;

import baritone.client.CustomFishingBobberRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@KeepName
public final class AutomatoneClient implements ClientModInitializer {
   public void onInitializeClient() {
      EntityRendererRegistry.register(Automatone.FISHING_BOBBER, CustomFishingBobberRenderer::new);
   }
}
