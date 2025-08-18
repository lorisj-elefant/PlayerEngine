
package adris.altoclef.brain.network;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class PacketHelper {
    private static final String MOD_ID = "PlayerEngine"; // may have to be lowercase?

    public static FriendlyByteBuf createBuffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    public static FriendlyByteBuf copyBuffer(FriendlyByteBuf buf) {
        return new FriendlyByteBuf(buf.copy());
    }

    public static ResourceLocation ChannelNameOf(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
}