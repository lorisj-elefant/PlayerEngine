package adris.altoclef.brain.server.local;

import java.util.UUID;

import adris.altoclef.AltoClefController;

public class ServerLocalState {
    // what is stored on the server per NPC
    private AltoClefController mod;
    private MessageBuffer altoClefMsgBuffer = new MessageBuffer(10);

    public ServerLocalState(AltoClefController mod){
        this.mod = mod;
    }
}
