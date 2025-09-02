package altoclef.player2api;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEventHandler {
    public ForgeEventHandler(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChatMessage(ServerChatEvent evt){
        EventQueueManager.onUserChatMessage(new Event.UserMessage(evt.getMessage().getString(), evt.getUsername()));
    }
}
