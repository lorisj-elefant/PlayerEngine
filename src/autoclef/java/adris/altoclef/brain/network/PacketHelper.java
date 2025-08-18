
package adris.altoclef.brain.network;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.logging.log4j.util.TriConsumer;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

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

    public static class PacketSchema<Payload> {
        protected final ResourceLocation channelName;
        protected final Function<Payload, FriendlyByteBuf> encode;
        protected final Function<FriendlyByteBuf, Payload> decode;

        public PacketSchema(
                ResourceLocation channelName,
                Function<Payload, FriendlyByteBuf> encode,
                Function<FriendlyByteBuf, Payload> decode) {
            this.channelName = channelName;
            this.encode = encode;
            this.decode = decode;
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
        public class Client {
            public void onPacket(BiConsumer<Minecraft, Payload> clientHandler) {
                ClientPlayNetworking.registerGlobalReceiver(channelName, (client, handler, buf, responseSender) -> {
                    FriendlyByteBuf copiedBuffer = copyBuffer(buf);
                    client.execute(() -> {
                        Payload payload = decode.apply(copiedBuffer);
                        clientHandler.accept(client, payload);
                    });
                });
            }

            public void send(Payload payload) {
                FriendlyByteBuf buf = encode.apply(payload);
                ClientPlayNetworking.send(channelName, buf);
            }
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public class Server {
            public void onPacket(TriConsumer<MinecraftServer, ServerPlayer, Payload> serverHandler) {
                ServerPlayNetworking.registerGlobalReceiver(channelName,
                        (server, player, netHandler, buf, responseSender) -> {
                            FriendlyByteBuf copiedBuffer = copyBuffer(buf);
                            server.execute(() -> {
                                Payload payload = decode.apply(copiedBuffer);
                                serverHandler.accept(server, player, payload);
                            });
                        });
            }

            public void send(ServerPlayer player, Payload payload) {
                FriendlyByteBuf buf = encode.apply(payload);
                ServerPlayNetworking.send(player, channelName, buf);
            }
        }
    }

    public static final class S2CSchema<Payload> extends PacketSchema<Payload> {
        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
        BiConsumer<Minecraft, Payload> clientHandler;

        public S2CSchema(ResourceLocation channelName,
                Function<Payload, FriendlyByteBuf> encode,
                Function<FriendlyByteBuf, Payload> decode) {
            super(channelName, encode, decode);
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
        public void registerClientHandler(BiConsumer<Minecraft, Payload> clientHandler) {
            this.clientHandler = clientHandler;
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
        public void onPacket(BiConsumer<Minecraft, Payload> clientHandler) {
            ClientPlayNetworking.registerGlobalReceiver(this.channelName, (client, handler, buf, responseSender) -> {
                FriendlyByteBuf copiedBuffer = copyBuffer(buf);
                client.execute(() -> {
                    Payload payload = this.decode.apply(copiedBuffer);
                    clientHandler.accept(client, payload);
                });
            });
        }

        @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
        public void send(ServerPlayer player, Payload payload) {
            FriendlyByteBuf buf = encode.apply(payload);
            ServerPlayNetworking.send(player, channelName, buf);
        }
    }

    // public static final class S2CSchema<Payload> {
    // private final ResourceLocation channelName;
    // private final Function<Payload, FriendlyByteBuf> encode;
    // private final Function<FriendlyByteBuf, Payload> decode;

    // public S2CSchema(
    // ResourceLocation channelName,
    // Function<Payload, FriendlyByteBuf> encode,
    // Function<FriendlyByteBuf, Payload> decode,
    // BiConsumer<Minecraft, Payload> clientHandler) {
    // this.channelName = channelName;
    // this.encode = encode;
    // this.decode = decode;
    // }

    // @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
    // public void registerOnClient(BiConsumer<Minecraft, Payload> clientHandler) {
    // ClientPlayNetworking.registerGlobalReceiver(channelName, (client, handler,
    // buf, responseSender) -> {
    // FriendlyByteBuf copiedBuffer = copyBuffer(buf);
    // client.execute(() -> {
    // Payload payload = decode.apply(copiedBuffer);
    // clientHandler.accept(client, payload);
    // });
    // });
    // }

    // public void sendFromServerToPlayer(ServerPlayer player, Payload payload) {
    // FriendlyByteBuf buf = encode.apply(payload);
    // ServerPlayNetworking.send(player, channelName, buf);
    // }
    // }

    // public static class C2SSchema<Payload> {
    // private final ResourceLocation channelName;
    // private final Function<Payload, FriendlyByteBuf> encode;
    // private final Function<FriendlyByteBuf, Payload> decode;

    // public C2SSchema(ResourceLocation channelName,
    // Function<Payload, FriendlyByteBuf> encode,
    // Function<FriendlyByteBuf, Payload> decode) {
    // this.channelName = channelName;
    // this.encode = encode;
    // this.decode = decode;
    // }

    // @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.SERVER)
    // public void registerOnServer(TriConsumer<MinecraftServer, ServerPlayer,
    // Payload> serverHandler) {
    // ServerPlayNetworking.registerGlobalReceiver(channelName,
    // (server, player, netHandler, buf, responseSender) -> {
    // FriendlyByteBuf copiedBuffer = copyBuffer(buf);
    // server.execute(() -> {
    // Payload payload = decode.apply(copiedBuffer);
    // serverHandler.accept(server, player, payload);
    // });
    // });
    // }

    // public void sendFromClientToServer(Payload payload) {
    // FriendlyByteBuf buf = encode.apply(payload);
    // ClientPlayNetworking.send(channelName, buf);
    // }

    // public static void send(ResourceLocation channelName, FriendlyByteBuf buf) {
    // ClientPlayNetworking.send(channelName, buf);
    // }

    // }

}