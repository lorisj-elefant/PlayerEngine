package adris.altoclef.player2api;

import adris.altoclef.player2api.utils.CharacterUtils;
import adris.altoclef.player2api.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

import static adris.altoclef.player2api.utils.HTTPUtils.sendRequest;

public class Player2APIService {

    public static JsonObject completeConversation(ConversationHistory conversationHistory) throws Exception {
        JsonObject requestBody = new JsonObject();
        JsonArray messagesArray = new JsonArray();
        for (JsonObject msg : conversationHistory.getListJSON())
            messagesArray.add((JsonElement) msg);
        requestBody.add("messages", (JsonElement) messagesArray);
        Map<String, JsonElement> responseMap = sendRequest("/v1/chat/completions", true, requestBody);
        if (responseMap.containsKey("choices")) {
            JsonArray choices = ((JsonElement) responseMap.get("choices")).getAsJsonArray();
            if (choices.size() != 0) {
                JsonObject messageObject = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                if (messageObject != null && messageObject.has("content")) {
                    String content = messageObject.get("content").getAsString();
                    return Utils.parseCleanedJson(content);
                }
            }
        }
        throw new Exception("Invalid response format: " + responseMap.toString());
    }

    public static String completeConversationToString(ConversationHistory conversationHistory) throws Exception {
        JsonObject requestBody = new JsonObject();
        JsonArray messagesArray = new JsonArray();
        for (JsonObject msg : conversationHistory.getListJSON())
            messagesArray.add((JsonElement) msg);
        requestBody.add("messages", (JsonElement) messagesArray);
        Map<String, JsonElement> responseMap = sendRequest("/v1/chat/completions", true, requestBody);
        if (responseMap.containsKey("choices")) {
            JsonArray choices = ((JsonElement) responseMap.get("choices")).getAsJsonArray();
            if (choices.size() != 0) {
                JsonObject messageObject = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                if (messageObject != null && messageObject.has("content")) {
                    String content = messageObject.get("content").getAsString();
                    return content;
                }
            }
        }
        throw new Exception("Invalid response format: " + responseMap.toString());
    }

    public static Character getSelectedCharacter() {
        try {
            Map<String, JsonElement> responseMap = sendRequest("/v1/selected_characters", false, null);
            return CharacterUtils.parseFirstCharacter(responseMap);
        } catch (Exception e) {
            return CharacterUtils.DEFAULT_CHARACTER;
        }
    }

    public static void textToSpeech(String message, Character character) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("play_in_app", Boolean.valueOf(true));
            requestBody.addProperty("speed", Integer.valueOf(1));
            requestBody.addProperty("text", message);
            JsonArray voiceIdsArray = new JsonArray();
            for (String voiceId : character.voiceIds())
                voiceIdsArray.add(voiceId);
            requestBody.add("voice_ids", (JsonElement) voiceIdsArray);
            System.out.println("Sending TTS request: " + message);
            sendRequest("/v1/tts/speak", true, requestBody);
        } catch (Exception exception) {
        }
    }

    public static void startSTT() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("timeout", Integer.valueOf(30));
        try {
            sendRequest("/v1/stt/start", true, requestBody);
        } catch (Exception e) {
            System.err.println("[Player2APIService/startSTT]: Error" + e.getMessage());
        }
    }

    public static String stopSTT() {
        try {
            Map<String, JsonElement> responseMap = sendRequest("/v1/stt/stop", true, null);
            if (!responseMap.containsKey("text"))
                throw new Exception("Could not find key 'text' in response");
            return ((JsonElement) responseMap.get("text")).getAsString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static void sendHeartbeat() {
        try {
            System.out.println("Sending Heartbeat");
            Map<String, JsonElement> responseMap = sendRequest("/v1/health", false, null);
            if (responseMap.containsKey("client_version"))
                System.out.println("Heartbeat Successful");
        } catch (Exception e) {
            System.err.printf("Heartbeat Fail: %s", new Object[]{e.getMessage()});
        }
    }
}
