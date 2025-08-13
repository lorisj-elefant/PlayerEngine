package adris.altoclef.player2api;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import adris.altoclef.AltoClefController;

public class EventQueueData {
    private Character character;
    private final AltoClefController mod;
    private final Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();

    public EventQueueData(AltoClefController mod){
        this.mod = mod;
    }

    
}
