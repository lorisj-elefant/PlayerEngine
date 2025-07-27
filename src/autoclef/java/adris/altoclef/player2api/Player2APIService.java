package adris.altoclef.player2api;

import adris.altoclef.player2api.Character;
import adris.altoclef.player2api.ConversationHistory;
import adris.altoclef.player2api.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Player2APIService {
  private static final String BASE_URL = "http://127.0.0.1:4315";
  
  private static Map<String, JsonElement> sendRequest(String endpoint, boolean postRequest, JsonObject requestBody) throws Exception {
    URL url = (new URI("http://127.0.0.1:4315" + endpoint)).toURL();
    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
    connection.setRequestMethod(postRequest ? "POST" : "GET");
    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
    connection.setRequestProperty("Accept", "application/json; charset=utf-8");
    connection.setRequestProperty("player2-game-key", "chatclef");
    if (postRequest && requestBody != null) {
      connection.setDoOutput(true);
      OutputStream os = connection.getOutputStream();
      try {
        byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        if (os != null)
          os.close(); 
      } catch (Throwable throwable) {
        if (os != null)
          try {
            os.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } 
    int responseCode = connection.getResponseCode();
    if (responseCode != 200)
      throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage()); 
    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null)
      response.append(line); 
    reader.close();
    JsonParser parser = new JsonParser();
    JsonObject jsonResponse = parser.parse(response.toString()).getAsJsonObject();
    Map<String, JsonElement> responseMap = new HashMap<>();
    for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)jsonResponse.entrySet())
      responseMap.put(entry.getKey(), entry.getValue()); 
    return responseMap;
  }
  
  public static JsonObject completeConversation(ConversationHistory conversationHistory) throws Exception {
    JsonObject requestBody = new JsonObject();
    JsonArray messagesArray = new JsonArray();
    for (JsonObject msg : conversationHistory.getListJSON())
      messagesArray.add((JsonElement)msg); 
    requestBody.add("messages", (JsonElement)messagesArray);
    Map<String, JsonElement> responseMap = sendRequest("/v1/chat/completions", true, requestBody);
    if (responseMap.containsKey("choices")) {
      JsonArray choices = ((JsonElement)responseMap.get("choices")).getAsJsonArray();
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
      messagesArray.add((JsonElement)msg); 
    requestBody.add("messages", (JsonElement)messagesArray);
    Map<String, JsonElement> responseMap = sendRequest("/v1/chat/completions", true, requestBody);
    if (responseMap.containsKey("choices")) {
      JsonArray choices = ((JsonElement)responseMap.get("choices")).getAsJsonArray();
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
      if (!responseMap.containsKey("characters"))
        throw new Exception("No characters found in API response."); 
      JsonArray charactersArray = ((JsonElement)responseMap.get("characters")).getAsJsonArray();
      if (charactersArray.size() == 0)
        throw new Exception("Character list is empty."); 
      JsonObject firstCharacter = charactersArray.get(0).getAsJsonObject();
      String name = Utils.getStringJsonSafely(firstCharacter, "name");
      if (name == null)
        throw new Exception("Character is missing 'name'."); 
      String shortName = Utils.getStringJsonSafely(firstCharacter, "short_name");
      if (shortName == null)
        throw new Exception("Character is missing 'short_name'."); 
      String greeting = Utils.getStringJsonSafely(firstCharacter, "greeting");
      String description = Utils.getStringJsonSafely(firstCharacter, "description");
      String[] voiceIds = Utils.getStringArrayJsonSafely(firstCharacter, "voice_ids");
      return new Character(name, shortName, greeting, description, voiceIds);
    } catch (Exception e) {
      System.err.println("Warning, getSelectedCharacter failed, reverting to default. Error message: " + e.getMessage());
      return new Character("AI agent", "AI", "Greetings", "You are a helpful AI Agent", new String[0]);
    } 
  }
  
  public static void textToSpeech(String message, Character character) {
    try {
      JsonObject requestBody = new JsonObject();
      requestBody.addProperty("play_in_app", Boolean.valueOf(true));
      requestBody.addProperty("speed", Integer.valueOf(1));
      requestBody.addProperty("text", message);
      JsonArray voiceIdsArray = new JsonArray();
      for (String voiceId : character.voiceIds)
        voiceIdsArray.add(voiceId); 
      requestBody.add("voice_ids", (JsonElement)voiceIdsArray);
      System.out.println("Sending TTS request: " + message);
      sendRequest("/v1/tts/speak", true, requestBody);
    } catch (Exception exception) {}
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
      return ((JsonElement)responseMap.get("text")).getAsString();
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
      System.err.printf("Heartbeat Fail: %s", new Object[] { e.getMessage() });
    } 
  }
}
