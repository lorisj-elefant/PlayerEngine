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
import baritone.api.entity.LivingEntityInventory;
import io.github.ladysnake.otomaton.AutomatoneEntity2;
import io.github.ladysnake.otomaton.Otomaton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.Vec3d;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.UUID;

public class AutomatonSpawnPacket implements FabricPacket {

    public static final PacketType<AutomatonSpawnPacket> TYPE = PacketType.create(
            Otomaton.SPAWN_PACKET_ID,
            AutomatonSpawnPacket::new
    );

    private final int id;
    private final UUID uuid;
    private final Vec3d pos;
    private final Vec3d velocity;
    private final float pitch;
    private final float yaw;
    private final Character character;
    private final LivingEntityInventory inventory;

    private AutomatonSpawnPacket(AutomatoneEntity2 entity) {
        this.id = entity.getId();
        this.uuid = entity.getUuid();
        this.pos = entity.getPos();
        this.velocity = entity.getVelocity();
        this.pitch = entity.getPitch();
        this.yaw = entity.getYaw();

        this.character = entity.getCharacter();
        this.inventory = entity.inventory;
    }

    public AutomatonSpawnPacket(PacketByteBuf buf) {
        this.id = buf.readVarInt();
        this.uuid = buf.readUuid();
        this.pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.velocity = new Vec3d(buf.readShort(), buf.readShort(), buf.readShort());
        this.pitch = (buf.readByte() * 360) / 256.0F;
        this.yaw = (buf.readByte() * 360) / 256.0F;

        this.character = CharacterUtils.readFromBuf(buf);
        this.inventory = new LivingEntityInventory(null);
        this.inventory.readNbt(buf.readNbt().getList("inv", 10));
    }

    public static Packet<ClientPlayPacketListener> create(AutomatoneEntity2 entity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        new AutomatonSpawnPacket(entity).write(buf);
        return ServerPlayNetworking.createS2CPacket(Otomaton.SPAWN_PACKET_ID, buf);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.id);
        buf.writeUuid(this.uuid);
        buf.writeDouble(this.pos.x);
        buf.writeDouble(this.pos.y);
        buf.writeDouble(this.pos.z);
        buf.writeShort((int) (Math.min(3.9, this.velocity.x) * 8000.0));
        buf.writeShort((int) (Math.min(3.9, this.velocity.y) * 8000.0));
        buf.writeShort((int) (Math.min(3.9, this.velocity.z) * 8000.0));
        buf.writeByte((byte) ((int) (this.pitch * 256.0F / 360.0F)));
        buf.writeByte((byte) ((int) (this.yaw * 256.0F / 360.0F)));

        CharacterUtils.writeToBuf(buf, character);
        NbtCompound compound = new NbtCompound();
        compound.put("inv",inventory.writeNbt(new NbtList()));
        buf.writeNbt(compound);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler var2, PacketByteBuf var3, PacketSender var4) {
        AutomatonSpawnPacket packet = new AutomatonSpawnPacket(var3);
        client.execute(() -> {
            ClientWorld world = var2.getWorld();
            AutomatoneEntity2 entity = new AutomatoneEntity2(Otomaton.AUTOMATONE, world);
            entity.setId(packet.id);
            entity.setUuid(packet.uuid);
            entity.syncPacketPositionCodec(packet.pos.x, packet.pos.y, packet.pos.z);
            entity.refreshPositionAfterTeleport(packet.pos.x, packet.pos.y, packet.pos.z);
            entity.setVelocity(packet.velocity);
            entity.setPitch(packet.pitch);
            entity.setYaw(packet.yaw);

            entity.setCharacter(packet.character);
            packet.inventory.player = entity;
            entity.inventory = packet.inventory;

            world.addEntity(packet.id, entity);
        });
    }
}