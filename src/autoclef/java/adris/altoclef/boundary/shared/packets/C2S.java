package adris.altoclef.boundary.shared.packets;

import java.util.UUID;

import adris.altoclef.boundary.shared.PacketHelper;
import adris.altoclef.boundary.shared.PacketHelper.C2SSchema;
import adris.altoclef.brain.client.CharacterUtils;
import adris.altoclef.brain.shared.Character;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class C2S {
    // packets
    private class EntityMessagePacket {
        private static ResourceLocation channel = PacketHelper.ChannelNameOf("C2S/EntityMessage");

        private static record Payload(String message, Character character, UUID entityId) {
        };

        private static final C2SSchema<Payload> schema = new C2SSchema<>(
            channel,
            (payload) -> {
                FriendlyByteBuf buf = PacketHelper.createBuffer();
                CharacterUtils.writeToBuf(buf, payload.character());
                buf.writeUUID(payload.entityId());
                return buf;
            },
            (buf) -> {
                String message = buf.readUtf();
                Character character = CharacterUtils.readFromBuf(buf);
                UUID id = buf.readUUID();
                return new Payload(message, character, id); 
            },
            (server, sendingPlayer, charMsgEvent) -> {
                
            }
        );
    }

    // effects
    public void sendEntityMessage(String message, Character character, UUID entityId){
        EntityMessagePacket.schema.sendFromClientToServer(new EntityMessagePacket.Payload(message, character, entityId));
    }
}
