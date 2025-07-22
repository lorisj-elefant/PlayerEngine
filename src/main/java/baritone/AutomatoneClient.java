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

import baritone.api.selection.ISelectionManager;
import baritone.command.defaults.ClickCommand;
import baritone.utils.GuiClick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@KeepName
public final class AutomatoneClient implements ClientModInitializer {
    public static final Set<Baritone> renderList = Collections.newSetFromMap(new WeakHashMap<>());
    public static final Set<ISelectionManager> selectionRenderList = Collections.newSetFromMap(new WeakHashMap<>());

    public static void onRenderPass(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.currentScreen instanceof GuiClick) {
            ((GuiClick) mc.currentScreen).onRender(context.matrixStack(), context.projectionMatrix());
        }

        if (!mc.isIntegratedServerRunning()) {
            // FIXME we should really be able to render stuff in multiplayer
            return;
        }

        //DefaultCommands.selCommand.renderSelectionBox();
    }

    @Override
    public void onInitializeClient(ModContainer mod) {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(AutomatoneClient::onRenderPass);
        ClientPlayNetworking.registerGlobalReceiver(ClickCommand.OPEN_CLICK_SCREEN, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            client.execute(() -> MinecraftClient.getInstance().setScreen(new GuiClick(uuid)));
        });
    }
}
