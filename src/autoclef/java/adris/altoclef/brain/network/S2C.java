package adris.altoclef.brain.network;

import java.util.UUID;
import java.util.function.BiConsumer;

import adris.altoclef.brain.client.CharacterUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class S2C {
    public static class NPCStatusPacket implements FabricPacket {
        public static final ResourceLocation CHANNEL_NAME = PacketHelper.ChannelNameOf("s2c/npc_status");
        public static final PacketType<NPCStatusPacket> TYPE = PacketType.create(CHANNEL_NAME, NPCStatusPacket::new);

        public final UUID entityId;
        public final String worldStatus;
        public final String agentStatus;
        public final String altoclefStatusMsgs;

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
        public static BiConsumer<Minecraft, NPCStatusPacket> onRecieve;

        public NPCStatusPacket(UUID entityId, String worldStatus, String agentStatus, String altoclefStatusMsgs) {
            this.entityId = entityId;
            this.worldStatus = worldStatus;
            this.agentStatus = agentStatus;
            this.altoclefStatusMsgs = altoclefStatusMsgs;
        }

        public NPCStatusPacket(FriendlyByteBuf buf) {
            this.entityId = buf.readUUID();
            this.worldStatus = buf.readUtf();
            this.agentStatus = buf.readUtf();
            this.altoclefStatusMsgs = buf.readUtf();
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeUUID(entityId);
            buf.writeUtf(worldStatus);
            buf.writeUtf(agentStatus);
            buf.writeUtf(altoclefStatusMsgs);
        }

        @Override
        public PacketType<NPCStatusPacket> getType() {
            return TYPE;
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
        public static void registerHandler(BiConsumer<Minecraft, NPCStatusPacket> onRecieve) {
            NPCStatusPacket.onRecieve = onRecieve;
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
        public static void handle(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf,
                PacketSender responseSender) {
            NPCStatusPacket packet = new NPCStatusPacket(buf);
            client.execute(() -> {
                onRecieve.accept(client, packet);
            });
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public static void send(NPCStatusPacket packet, ServerPlayer player) {
            MinecraftServer server = player.getServer();
            server.execute(() -> {
                FriendlyByteBuf buf = PacketHelper.createBuffer();
                packet.write(buf);
            });
        }
    }
    
}
