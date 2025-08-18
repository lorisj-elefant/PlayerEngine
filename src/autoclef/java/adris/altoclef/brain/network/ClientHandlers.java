package adris.altoclef.brain.network;

public class ClientHandlers {
    public static void register(){
        S2C.NPCStatusPacket.registerHandler((client, statusPacket)-> {
            

        });
    }
}
