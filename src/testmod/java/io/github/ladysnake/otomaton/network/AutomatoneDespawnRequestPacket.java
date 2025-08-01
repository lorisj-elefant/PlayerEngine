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

package io.github.ladysnake.otomaton.network;

import adris.altoclef.player2api.Character;
import adris.altoclef.player2api.utils.CharacterUtils;
import io.github.ladysnake.otomaton.companion.AutomatoneEntity;
import io.github.ladysnake.otomaton.Otomaton;
import io.github.ladysnake.otomaton.companion.CompanionManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class AutomatoneDespawnRequestPacket implements FabricPacket {

    public static final PacketType<AutomatonSpawnPacket> TYPE = PacketType.create(
            Otomaton.DESPAWN_REQUEST_PACKET_ID,
            AutomatonSpawnPacket::new
    );

    private final Character character;

    private AutomatoneDespawnRequestPacket(Character character) {
        this.character = character;
    }

    public AutomatoneDespawnRequestPacket(PacketByteBuf buf) {
        this.character = CharacterUtils.readFromBuf(buf);
    }

    public static Packet<ServerPlayPacketListener> create(Character character) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        new AutomatoneDespawnRequestPacket(character).write(buf);
        return ClientPlayNetworking.createC2SPacket(Otomaton.DESPAWN_REQUEST_PACKET_ID, buf);
    }

    @Override
    public void write(PacketByteBuf buf) {
        CharacterUtils.writeToBuf(buf, character);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static void handle(MinecraftServer var1, ServerPlayerEntity var2, ServerPlayNetworkHandler var3, PacketByteBuf var4, PacketSender var5){
        AutomatoneDespawnRequestPacket packet = new AutomatoneDespawnRequestPacket(var4);
        var1.execute(()-> CompanionManager.KEY.get(var2).dismissCompanion(packet.character.name));
    }
}
