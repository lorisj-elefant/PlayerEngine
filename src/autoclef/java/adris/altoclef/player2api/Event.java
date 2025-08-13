package adris.altoclef.player2api;

public sealed interface Event // tagged union basically of the below events
        permits Event.UserMessage, Event.CharacterMessage {
    String message();

    public record UserMessage(String message, String userName) implements Event {
        public String toString(){
            return String.format("Other Player Message: [%s]: %s", userName, message);
        }
    }

    public record CharacterMessage(String message, EventQueueData sendingCharacterData) implements Event {
        public String toString(){
            return String.format("Other Agent Message: [%s]: %s", sendingCharacterData.getUsername(), message);
        }
    }
}