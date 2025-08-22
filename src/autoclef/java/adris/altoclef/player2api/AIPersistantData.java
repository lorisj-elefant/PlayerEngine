package adris.altoclef.player2api;

import java.util.Deque;

import adris.altoclef.AltoClefController;
import adris.altoclef.player2api.Event.InfoMessage;

public class AIPersistantData {
    // contains data relating to AI processing, only including data that is
    // permanent,
    // and persists across game state (not queue stuff)

    private ConversationHistory conversationHistory;
    private Character character;

    public AIPersistantData(AltoClefController mod, Character character) {
        String systemPrompt = Prompts.getAINPCSystemPrompt(this.character, mod.getCommandExecutor().allCommands());
        this.conversationHistory = new ConversationHistory(systemPrompt, character.name(), character.shortName());
        this.character = character;
    }

    public void clearHistory() {
        conversationHistory.clear();
    }

    public Event getGreetingEvent() {
        String suffix = " IMPORTANT: SINCE THIS IS THE FIRST MESSAGE, DO NOT SEND A COMMAND!!";
        if (conversationHistory.isLoadedFromFile()) {
            return (new InfoMessage("You want to welcome user back." + suffix));
        } else {
            return (new InfoMessage(character.greetingInfo() + suffix));
        }
    }

    public void dumpEventQueueToConversationHistory(Deque<Event> eventQueue){
        while(!eventQueue.isEmpty()){
            Event event = eventQueue.poll();
            conversationHistory.addUserMessage(event.toString());
        }
    }
    public ConversationHistory getConversationHistoryWrappedWithStatus(String worldStatus, String agentStatus, String altoClefDebugMsgs){
        return this.conversationHistory
                .copyThenWrapLatestWithStatus(worldStatus, agentStatus, altoClefDebugMsgs);
    }
    public void addAssistantMessage(String llmMessage){
        this.conversationHistory.addAssistantMessage(llmMessage);
    }
}
