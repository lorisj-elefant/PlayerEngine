/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package altoclef.player2api.utils;

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
import java.util.Map.Entry;

public class HTTPUtils {
   private static final String BASE_URL = "http://127.0.0.1:4315";

   public static Map<String, JsonElement> sendRequest(String player2GameId, String endpoint, boolean postRequest, JsonObject requestBody) throws Exception {
      URL url = new URI("http://127.0.0.1:4315" + endpoint).toURL();
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod(postRequest ? "POST" : "GET");
      connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
      connection.setRequestProperty("Accept", "application/json; charset=utf-8");
      connection.setRequestProperty("player2-game-key", player2GameId);
      if (postRequest && requestBody != null) {
         connection.setDoOutput(true);

         try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
         } catch (Throwable var12) {
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
         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
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
