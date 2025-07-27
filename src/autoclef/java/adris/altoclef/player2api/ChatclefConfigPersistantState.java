package adris.altoclef.player2api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class ChatclefConfigPersistantState {
  private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
  
  private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("chatclef_config.json");
  
  private static adris.altoclef.player2api.ChatclefConfigPersistantState config = load();
  
  private boolean sttHintEnabled = true;
  
  public static boolean isSttHintEnabled() {
    return (instance()).sttHintEnabled;
  }
  
  public static void updateSttHint(boolean value) {
    System.out.println("[ChatclefConfigPersistantState]: updateSttHint called with: " + value);
    (instance()).sttHintEnabled = value;
    save();
  }
  
  private static adris.altoclef.player2api.ChatclefConfigPersistantState load() {
    if (Files.exists(CONFIG_PATH, new java.nio.file.LinkOption[0]))
      try {
        String json = Files.readString(CONFIG_PATH);
        System.out.println("[ChatclefConfigPersistantState]: Reading from file...");
        return (adris.altoclef.player2api.ChatclefConfigPersistantState)GSON.fromJson(json, adris.altoclef.player2api.ChatclefConfigPersistantState.class);
      } catch (IOException e) {
        e.printStackTrace();
      }  
    System.out.println("[ChatclefConfigPersistantState]: Could not load file, using default.");
    return new adris.altoclef.player2api.ChatclefConfigPersistantState();
  }
  
  private static void save() {
    System.out.println("[ChatclefConfigPersistantState]: save() called");
    try {
      Files.writeString(CONFIG_PATH, GSON.toJson(config), new java.nio.file.OpenOption[0]);
      System.out.println("[ChatclefConfigPersistantState]: Writing to file...");
    } catch (IOException e) {
      System.err.println("[ChatclefConfigPersistantState]: Writing to file FAILED");
      e.printStackTrace();
    } 
  }
  
  private static adris.altoclef.player2api.ChatclefConfigPersistantState instance() {
    return config;
  }
}
