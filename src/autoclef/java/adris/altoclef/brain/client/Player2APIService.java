
package adris.altoclef.brain.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import adris.altoclef.brain.server.local.ConversationHistory;
import adris.altoclef.brain.shared.Character;
import adris.altoclef.brain.shared.Utils;

import java.net.HttpURLConnection;
import java.util.Map;

public class Player2APIService {

   private String player2GameID;

   public Player2APIService(String player2GameId){
      this.player2GameID = player2GameId;
      ConversationHistory.setPlayer2APIInstance(this);
      HTTPUtils.extraConnectionProcessing = (connection) -> {
         setGameKeyPropertyIfExists(connection);
      };
   }

   public JsonObject completeConversation(ConversationHistory conversationHistory) throws Exception {
      JsonObject requestBody = new JsonObject();
      JsonArray messagesArray = new JsonArray();

      for (JsonObject msg : conversationHistory.getListJSON()) {
         messagesArray.add(msg);
      }

      requestBody.add("messages", messagesArray);
      Map<String, JsonElement> responseMap = HTTPUtils.sendRequest("/v1/chat/completions", true, requestBody);
      if (responseMap.containsKey("choices")) {
         JsonArray choices = responseMap.get("choices").getAsJsonArray();
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

   public  String completeConversationToString(ConversationHistory conversationHistory) throws Exception {
      JsonObject requestBody = new JsonObject();
      JsonArray messagesArray = new JsonArray();

      for (JsonObject msg : conversationHistory.getListJSON()) {
         messagesArray.add(msg);
      }

      requestBody.add("messages", messagesArray);
      Map<String, JsonElement> responseMap = HTTPUtils.sendRequest("/v1/chat/completions", true, requestBody);
      if (responseMap.containsKey("choices")) {
         JsonArray choices = responseMap.get("choices").getAsJsonArray();
         if (choices.size() != 0) {
            JsonObject messageObject = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (messageObject != null && messageObject.has("content")) {
               return messageObject.get("content").getAsString();
            }
         }
      }

      throw new Exception("Invalid response format: " + responseMap.toString());
   }

   public  Character getSelectedCharacter() {
      try {
         Map<String, JsonElement> responseMap = HTTPUtils.sendRequest(
               "/v1/selected_characters", false, null);
         return CharacterUtils.parseFirstCharacter(responseMap);
      } catch (Exception var2) {
         return CharacterUtils.DEFAULT_CHARACTER;
      }
   }

   public  void textToSpeech(String message, Character character) {
      try {
         JsonObject requestBody = new JsonObject();
         requestBody.addProperty("play_in_app", true);
         requestBody.addProperty("speed", 1);
         requestBody.addProperty("text", message);
         JsonArray voiceIdsArray = new JsonArray();

         for (String voiceId : character.voiceIds()) {
            voiceIdsArray.add(voiceId);
         }

         requestBody.add("voice_ids", voiceIdsArray);
         System.out.println("Sending TTS request: " + message);
         HTTPUtils.sendRequest("/v1/tts/speak", true, requestBody);
      } catch (Exception var9) {
      }
   }

   public void startSTT() {
      JsonObject requestBody = new JsonObject();
      requestBody.addProperty("timeout", 30);

      try {
         HTTPUtils.sendRequest("/v1/stt/start", true, requestBody);
      } catch (Exception var3) {
         System.err.println("[Player2APIService/startSTT]: Error" + var3.getMessage());
      }
   }

   public String stopSTT() {
      try {
         Map<String, JsonElement> responseMap = HTTPUtils.sendRequest("/v1/stt/stop", true, null);
         if (!responseMap.containsKey("text")) {
            throw new Exception("Could not find key 'text' in response");
         } else {
            return responseMap.get("text").getAsString();
         }
      } catch (Exception var2) {
         return var2.getMessage();
      }
   }

   public void sendHeartbeat() {
      try {
         System.out.println("Sending Heartbeat " + player2GameID);
         Map<String, JsonElement> responseMap = HTTPUtils.sendRequest("/v1/health", false, null);
         if (responseMap.containsKey("client_version")) {
            System.out.println("Heartbeat Successful");
         }
      } catch (Exception var2) {
         System.err.printf("Heartbeat Fail: %s", var2.getMessage());
      }
   }

   private void setGameKeyPropertyIfExists(HttpURLConnection connection) {
      if (player2GameID != null) {
         connection.setRequestProperty("player2-game-key", player2GameID);
      }
   }
}
