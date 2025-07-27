package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import adris.altoclef.multiversion.MessageTypeVer;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.ChatListener;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatListener.class})
public final class ChatReadMixin {
  @Inject(method = {"onChatMessage"}, at = {@At("HEAD")})
  private void onChatMessage(SignedChatMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
    ChatMessageEvent evt = new ChatMessageEvent(message.getUnsignedContent().getString(), sender.getName(), MessageTypeVer.getMessageType(params));
    EventBus.publish(evt);
  }
}
