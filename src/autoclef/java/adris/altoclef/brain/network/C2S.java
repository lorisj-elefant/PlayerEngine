package adris.altoclef.brain.network;

import java.util.UUID;

import org.apache.logging.log4j.util.TriConsumer;

import adris.altoclef.brain.shared.Character;
import adris.altoclef.brain.client.CharacterUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class C2S {
    public static class POSTEntityMessage implements FabricPacket {
        public static final ResourceLocation CHANNEL_NAME = PacketHelper.ChannelNameOf("c2s/entity_msg");
        public static final PacketType<C2S.POSTEntityMessage> TYPE = PacketType.create(CHANNEL_NAME,
                C2S.POSTEntityMessage::new);

        public final String message;
        public final Character character;
        public final UUID entityId;
        public final UUID playerSenderId;

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public static TriConsumer<MinecraftServer, ServerPlayer, POSTEntityMessage> onRecieve;

        public POSTEntityMessage(String message, Character character, UUID entityId, UUID playerSenderId) {
            this.message = message;
            this.character = character;
            this.entityId = entityId;
            this.playerSenderId = playerSenderId;
        }

        public POSTEntityMessage(FriendlyByteBuf buf) {
            this.message = buf.readUtf();
            this.character = CharacterUtils.readFromBuf(buf);
            this.entityId = buf.readUUID();
            this.playerSenderId = buf.readUUID();
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeUtf(this.message);
            CharacterUtils.writeToBuf(buf, this.character);
            buf.writeUUID(this.entityId);
            buf.writeUUID(this.playerSenderId);
        }

        @Override
        public PacketType<POSTEntityMessage> getType() {
            return TYPE;
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public static void registerHandler(TriConsumer<MinecraftServer, ServerPlayer, POSTEntityMessage> onRecieve) {
            POSTEntityMessage.onRecieve = onRecieve;
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public static void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
                FriendlyByteBuf buf, PacketSender responseSender) {
            POSTEntityMessage packet = new POSTEntityMessage(buf);

            server.execute(() -> {
                onRecieve.accept(server, player, packet);
            });
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
        public static void send(POSTEntityMessage packet) {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> {
                FriendlyByteBuf buf = PacketHelper.createBuffer();
                packet.write(buf);
                ClientPlayNetworking.send(CHANNEL_NAME, buf);
            });
        }
    }

    public static class GETSystemPrompt implements FabricPacket {
        public static final Map<
        public static final ResourceLocation CHANNEL_NAME = PacketHelper.ChannelNameOf("c2s/get_system_prompt");
        public static final PacketType<C2S.GETSystemPrompt> TYPE = PacketType.create(CHANNEL_NAME,
                C2S.GETSystemPrompt::new);

        public final UUID entityId;
        public final Character character;

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public static TriConsumer<MinecraftServer, ServerPlayer, GETSystemPrompt> onRecieve;

        public GETSystemPrompt(UUID entityId, Character character) {
            this.entityId = entityId;
            this.character = character;
        }

        public GETSystemPrompt(FriendlyByteBuf buf) {
            this.entityId = buf.readUUID();
            this.character = CharacterUtils.readFromBuf(buf);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeUUID(this.entityId);
            CharacterUtils.writeToBuf(buf, this.character);
        }

        @Override
        public PacketType<GETSystemPrompt> getType() {
            return TYPE;
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public static void registerHandler(
                TriConsumer<MinecraftServer, ServerPlayer, GETSystemPrompt> onRecieve) {
            GETSystemPrompt.onRecieve = onRecieve;
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public static void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
                FriendlyByteBuf buf, PacketSender responseSender) {
            GETSystemPrompt packet = new GETSystemPrompt(buf);
            server.execute(() -> {
                onRecieve.accept(server, player, packet);
            });
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
        public static void send(GETSystemPrompt packet) {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> {
                FriendlyByteBuf buf = PacketHelper.createBuffer();
                packet.write(buf);
                ClientPlayNetworking.send(CHANNEL_NAME, buf);
            });
        }
    }

    public static class GETStatus implements FabricPacket {
        public static final ResourceLocation CHANNEL_NAME = PacketHelper.ChannelNameOf("c2s/get_status");
        public static final PacketType<C2S.GETStatus> TYPE = PacketType.create(CHANNEL_NAME,
                C2S.GETStatus::new);

        public final UUID entityId;
        public final String worldStatus;
        public final String agentStatus;
        public final String altoclefStatusMsgs;

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public static TriConsumer<MinecraftServer, ServerPlayer, GETStatus> onRecieve;

        public GETStatus(UUID entityId, String worldStatus, String agentStatus, String altoclefStatusMsgs) {
            this.entityId = entityId;
            this.worldStatus = worldStatus;
            this.agentStatus = agentStatus;
            this.altoclefStatusMsgs = altoclefStatusMsgs;
        }

        public GETStatus(FriendlyByteBuf buf) {
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
        public PacketType<GETStatus> getType() {
            return TYPE;
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public static void registerHandler(
                TriConsumer<MinecraftServer, ServerPlayer, GETStatus> onRecieve) {
            GETStatus.onRecieve = onRecieve;
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public static void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
                FriendlyByteBuf buf, PacketSender responseSender) {
            GETStatus packet = new GETStatus(buf);
            server.execute(() -> {
                onRecieve.accept(server, player, packet);
            });
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
        public static void send(GETStatus packet) {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> {
                FriendlyByteBuf buf = PacketHelper.createBuffer();
                packet.write(buf);
                ClientPlayNetworking.send(CHANNEL_NAME, buf);
            });
        }
    }

}
