package com.player2.playerengine.player2api.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.player2.playerengine.PlayerEngine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebSocketUtils {
    public static final Logger LOGGER = LogManager.getLogger(PlayerEngine.MOD_NAME);
    private final HttpClient client = HttpClient.newHttpClient();
    private volatile WebSocket ws;
    private final Map<String, Consumer<JsonObject>> handlers = new ConcurrentHashMap<>();

    public void registerHandler(String type, Consumer<JsonObject> handler) {
        handlers.put(type, handler);
    }

    public void connect(URI uri, String bearerToken) {
        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .header("Authorization", bearerToken != null ? "Bearer " + bearerToken : "")
                .buildAsync(uri, new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        LOGGER.info("Connected to " + uri);
                        ws = webSocket;
                        webSocket.request(1);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        try {
                            JsonObject json = JsonParser.parseString(data.toString()).getAsJsonObject();
                            if (json.has("type")) {
                                String type = json.get("type").getAsString();
                                Consumer<JsonObject> handler = handlers.get(type);
                                if (handler != null)
                                    handler.accept(json);
                                else
                                    LOGGER.info("No handler for type: " + type);
                            } else {
                                LOGGER.error("Message missing 'type': " + data);
                            }
                        } catch (Exception e) {
                            LOGGER.error("Failed to parse JSON: " + e.getMessage());
                        } finally {
                            webSocket.request(1); // request the next message
                        }
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        LOGGER.info("Closed: " + statusCode + " " + reason);
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        LOGGER.error("Error: " + error.getMessage());
                    }
                });
    }

    public boolean sendMsg(JsonObject obj) {
        if (ws == null)
            return false;
        ws.sendText(obj.toString(), true);
        return true;
    }

}
