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

package io.github.ladysnake.otomaton;

import io.github.ladysnake.otomaton.client.render.RenderAutomaton;
import io.github.ladysnake.otomaton.network.AutomatonSpawnPacket;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class OtomatonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        EntityRendererRegistry.register(Otomaton.FAKE_PLAYER, ZombieEntityRenderer::new);
        EntityRendererRegistry.register(Otomaton.AUTOMATONE, RenderAutomaton::new);

        ClientPlayNetworking.registerGlobalReceiver(Otomaton.SPAWN_PACKET_ID, AutomatonSpawnPacket::handle);
    }
}
