/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone;

import baritone.client.CustomFishingBobberRenderer;
import baritone.command.defaults.ClickCommand;
import baritone.utils.GuiClick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

import java.util.UUID;

@KeepName
public final class AutomatoneClient implements ClientModInitializer {
    public static void onRenderPass(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.currentScreen instanceof GuiClick) {
            ((GuiClick) mc.currentScreen).onRender(context.matrixStack(), context.projectionMatrix());
        }

    }

    @Override
    public void onInitializeClient(ModContainer mod) {
        EntityRendererRegistry.register(Automatone.FISHING_BOBBER, CustomFishingBobberRenderer::new);
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(AutomatoneClient::onRenderPass);
        ClientPlayNetworking.registerGlobalReceiver(ClickCommand.OPEN_CLICK_SCREEN, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            client.execute(() -> MinecraftClient.getInstance().setScreen(new GuiClick(uuid)));
        });
    }
}
