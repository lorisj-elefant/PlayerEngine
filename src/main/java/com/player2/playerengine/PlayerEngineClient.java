package com.player2.playerengine;

import com.player2.playerengine.player2api.utils.AudioUtils;
import com.player2.playerengine.automaton.KeepName;
import com.player2.playerengine.automaton.client.CustomFishingBobberRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

@KeepName
public final class PlayerEngineClient implements ClientModInitializer {
   public void onInitializeClient() {
      EntityRendererRegistry.register(PlayerEngine.FISHING_BOBBER, CustomFishingBobberRenderer::new);

      ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation("com/player2/playerengine", "stream_tts"), (client, handler, buf, responseSender) -> {
         String clientId = buf.readUtf();
         String token = buf.readUtf();
         String text = buf.readUtf();
         double speed = buf.readDouble();
         int voiceIdCount = buf.readVarInt();
         String[] voiceIds = new String[voiceIdCount];
         for (int i = 0; i < voiceIdCount; i++) {
            voiceIds[i] = buf.readUtf();
         }

         CompletableFuture.runAsync(() -> {
            AudioUtils.streamAudio(clientId, token, text, speed, voiceIds);
         });
      });
   }
}
