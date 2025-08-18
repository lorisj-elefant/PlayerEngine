package adris.altoclef.brain.network.packets;

import adris.altoclef.brain.network.PacketHelper;
import adris.altoclef.brain.network.PacketHelper.S2CSchema;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class S2C {

    // packets
    private class PingPacket {
        private static ResourceLocation channel = PacketHelper.ChannelNameOf("S2C/Ping");
        
        private static record Payload(int number) {
        };

        private static final S2CSchema<Payload> schema = new S2CSchema<>(
                channel,
                (payload) -> {
                    FriendlyByteBuf buf = PacketHelper.createBuffer();
                    buf.writeVarInt(payload.number());
                    return buf;
                },
                (buf) -> new Payload(buf.readVarInt()),
                (client, payload) -> {
                    // do stuff on client here
                });
    }

    // effects
    public static void PingPlayer(ServerPlayer player, int number) {
        PingPacket.schema.sendFromServerToPlayer(player, new PingPacket.Payload(number));
    }
}
