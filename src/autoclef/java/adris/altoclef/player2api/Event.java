package adris.altoclef.player2api;

public sealed interface Event // tagged union basically of the below events
        permits Event.UserMessage, Event.CharacterMessage {
    String message();

    public record UserMessage(String message, String userName) implements Event {
    }

    public record CharacterMessage(String message, Character character) implements Event {
    }
}