package adris.altoclef.player2api;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.AltoClefController;
public class EventQueueManager {
    public static final Logger LOGGER = LogManager.getLogger("Automatone");
    private static class LLMCompleter {
        private boolean isProcessing = false;

        public boolean isAvailible() {
            return !isProcessing;
        }
        public void process(EventQueueData data, Consumer<String> onUncleanResponse){
            
        }
    }

    public EventQueueManager(AltoClefController mod){
        
    }

    
}
