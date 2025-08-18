package adris.altoclef.brain.network.packets;

import java.util.UUID;

import adris.altoclef.brain.client.CharacterUtils;
import adris.altoclef.brain.network.PacketHelper;
import adris.altoclef.brain.network.PacketHelper.C2SSchema;
import adris.altoclef.brain.shared.Character;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class C2S {
    // packets
    private class EntityMessagePacket {
        private static ResourceLocation channel = PacketHelper.ChannelNameOf("c2s/entity_message");

        private static record Payload(String message, Character character, UUID entityId) {
        };

        private static final C2SSchema<Payload> schema = new C2SSchema<>(
            channel,
            (payload) -> {
                FriendlyByteBuf buf = PacketHelper.createBuffer();
                buf.writeUtf(payload.message);
                CharacterUtils.writeToBuf(buf, payload.character());
                buf.writeUUID(payload.entityId());
                return buf;
            },
            (buf) -> {
                String message = buf.readUtf();
                Character character = CharacterUtils.readFromBuf(buf);
                UUID id = buf.readUUID();
                return new Payload(message, character, id); 
            }
        );

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
        private static 6
        

    }

    // effects
    public void sendEntityMessage(String message, Character character, UUID entityId){
        EntityMessagePacket.schema.sendFromClientToServer(new EntityMessagePacket.Payload(message, character, entityId));
    }
}
