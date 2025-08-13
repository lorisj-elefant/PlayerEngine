package adris.altoclef.player2api;

import java.util.Deque;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.AltoClefController;
import adris.altoclef.player2api.status.StatusUtils;

public class EventQueueData {
    public static final Logger LOGGER = LogManager.getLogger("Automatone");

    private final Character character;
    private final AltoClefController mod;

    private ConversationHistory conversationHistory;

    private final Deque<Event> eventQueue = new ConcurrentLinkedDeque<>();
    private long lastProcessTime = 0L;    
    private boolean isProcessing = false;
    
    public EventQueueData(AltoClefController mod, Character character){
        this.character = character;
        this.mod = mod;

        
        String systemPrompt = Prompts.getAINPCSystemPrompt(this.character,mod.getCommandExecutor().allCommands());
        this.conversationHistory = new ConversationHistory(systemPrompt, character);
    }

    // ## Processing
    public void process(Consumer<String> extOnUncleanMsg, Consumer<String> extOnErrMsg){
        Consumer<String> onUncleanMsg = msg -> {
            this.isProcessing = false;
            extOnUncleanMsg.accept(msg);
        };
        Consumer<String> onErrMsg = errMsg -> {
            this.isProcessing = false;
            extOnErrMsg.accept(errMsg);
        };

        if(eventQueue.isEmpty()){
            return;
        }

        this.lastProcessTime = System.nanoTime();
        this.isProcessing = true;

        getResponseOrError(onUncleanMsg, onErrMsg);
    }

    private void getResponseOrError(Consumer<String> onMessageResponse, Consumer<String> onErrMessage){
        moveFromQueueToConversationHistory();
        
    }

    
    private void moveFromQueueToConversationHistory(){
        while(!eventQueue.isEmpty()){
            Event event = eventQueue.poll();
            conversationHistory.addUserMessage(event.toString());
        }
    }
    // ## Callbacks:
    public void onUserMessage(Event.UserMessage msg){
        // is duplicate <=> same as most recent msg
        boolean isDuplicate = eventQueue.peekLast() != null && eventQueue.peekLast().equals(msg);
        if(isDuplicate){
            return; // skip
        }
        eventQueue.add(msg);
    }

    public void onAICharacterMessage(Event.CharacterMessage msg){
        boolean comingFromThisCharacter = msg.sendingCharacterData().getUsername().equals(getUsername());
        // is our character <=> dont add because we will already have added assistant msg
        if(comingFromThisCharacter){
            return;
        }
        eventQueue.add(msg);
    }

    // Utils: 
    public String getUsername(){
        return mod.getPlayer().getName().getString();
    }

    public float getDistanceToUserName(String userName){
        return StatusUtils.getUserNameDistance(mod, userName);
    }
    
}
