package adris.altoclef.player2api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import adris.altoclef.player2api.Player2APIService;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HTTPUtils {
   private static final String BASE_URL = "http://127.0.0.1:4315";

   public static void processDataToRawOutputStream(
         String endpoint,
         boolean postRequest,
         JsonObject requestBody,
         OutputStream outputToWriteTo) throws Exception {
      HttpURLConnection connection = null;
      try {
         URL url = new URI(BASE_URL + endpoint).toURL();
         connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod(postRequest ? "POST" : "GET");
         connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
         connection.setRequestProperty("Accept", "application/json; charset=utf-8");

         connection.setConnectTimeout(15_000);
         connection.setReadTimeout(60_000);

         Player2APIService.player2ProcessConnection(connection);

         if (postRequest) {
            connection.setDoOutput(true);
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = connection.getOutputStream()) {
               os.write(input);
            }
         }

         int code = connection.getResponseCode();
         InputStream bodyStream = (code >= 200 && code < 300)
               ? connection.getInputStream()
               : connection.getErrorStream();

         ObjectMapper mapper = new ObjectMapper();
         JsonNode root = mapper.readTree(new InputStreamReader(bodyStream, StandardCharsets.UTF_8));
         String dataField = root.path("data").asText(null);

         if (dataField == null) {
            throw new IOException("Response JSON missing 'data' field: " + root.toString());
         }

         String base64Part;
         if (dataField.startsWith("data:")) {
            int comma = dataField.indexOf(',');
            if (comma < 0)
               throw new IOException("Invalid data URI (no comma): " + dataField);
            base64Part = dataField.substring(comma + 1);
         } else {
            base64Part = dataField;
         }

         // base64 -> raw bytes, also write to output
         if (outputToWriteTo != null) {
            byte[] ascii = base64Part.getBytes(StandardCharsets.US_ASCII);
            try (InputStream b64In = new ByteArrayInputStream(ascii);
                  InputStream decoded = Base64.getDecoder().wrap(b64In)) {
               decoded.transferTo(outputToWriteTo);
            }
         }
         if (code < 200 || code >= 300) {
            throw new IOException("bad response code: " + code);
         }
      } finally {
         if (connection != null)
            connection.disconnect();
      }
   }

   public static Map<String, JsonElement> sendRequest(String endpoint, boolean postRequest, JsonObject requestBody)
         throws Exception {
      URL url = new URI(BASE_URL + endpoint).toURL();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod(postRequest ? "POST" : "GET");
      connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
      connection.setRequestProperty("Accept", "application/json; charset=utf-8");
      Player2APIService.player2ProcessConnection(connection);
      if (postRequest && requestBody != null) {
         connection.setDoOutput(true);

         try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
         } catch (Throwable v) {
         }
      }

      JsonObject jsonResponse = getJsonObject(connection);
      Map<String, JsonElement> responseMap = new HashMap<>();

      for (Entry<String, JsonElement> entry : jsonResponse.entrySet()) {
         responseMap.put(entry.getKey(), entry.getValue());
      }

      return responseMap;
   }

   private static JsonObject getJsonObject(HttpURLConnection connection) throws IOException {
      int responseCode = connection.getResponseCode();
      if (responseCode != 200) {
         throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
      } else {
         BufferedReader reader = new BufferedReader(
               new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
         StringBuilder response = new StringBuilder();

         String line;
         while ((line = reader.readLine()) != null) {
            response.append(line);
         }

         reader.close();
         return JsonParser.parseString(response.toString()).getAsJsonObject();
      }
   }
}
