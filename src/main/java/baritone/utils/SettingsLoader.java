package baritone.utils;

import baritone.Automatone;
import baritone.api.Settings;
import baritone.api.utils.SettingsUtil;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsLoader {
   private static final Pattern SETTING_PATTERN = Pattern.compile("^(?<setting>[^ ]+) +(?<value>.+)");
   private static final Path SETTINGS_PATH = DirUtil.getConfigDir().resolve("automatone").resolve("settings.txt");

   public static void readAndApply(Settings settings) {
      try {
         Files.lines(SETTINGS_PATH).filter(line -> !line.trim().isEmpty() && !isComment(line)).forEach(line -> {
            Matcher matcher = SETTING_PATTERN.matcher(line);
            if (!matcher.matches()) {
               Automatone.LOGGER.error("Invalid syntax in setting file: " + line);
            } else {
               String settingName = matcher.group("setting").toLowerCase();
               String settingValue = matcher.group("value");

               try {
                  SettingsUtil.parseAndApply(settings, settingName, settingValue);
               } catch (Exception var6) {
                  Automatone.LOGGER.error("Unable to parse line " + line, var6);
               }
            }
         });
      } catch (NoSuchFileException var4) {
         Automatone.LOGGER.info("Automatone settings file not found, resetting.");

         try {
            Files.createFile(SETTINGS_PATH);
         } catch (IOException var3) {
         }
      } catch (Exception var5) {
         Automatone.LOGGER.error("Exception while reading Automatone settings, some settings may be reset to default values!", var5);
      }
   }

   private static boolean isComment(String line) {
      return line.startsWith("#") || line.startsWith("//");
   }

   public static synchronized void save(Settings settings) {
      try (BufferedWriter out = Files.newBufferedWriter(SETTINGS_PATH)) {
         for (Settings.Setting<?> setting : SettingsUtil.modifiedSettings(settings)) {
            out.write(SettingsUtil.settingToString(setting) + "\n");
         }
      } catch (Exception var6) {
         Automatone.LOGGER.error("Exception thrown while saving Automatone settings!", var6);
      }
   }
}
