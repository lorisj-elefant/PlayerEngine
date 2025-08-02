package adris.altoclef.util.helpers;

import adris.altoclef.Debug;
import adris.altoclef.util.serialization.BlockPosDeserializer;
import adris.altoclef.util.serialization.BlockPosSerializer;
import adris.altoclef.util.serialization.ChunkPosDeserializer;
import adris.altoclef.util.serialization.ChunkPosSerializer;
import adris.altoclef.util.serialization.IFailableConfigFile;
import adris.altoclef.util.serialization.IListConfigFile;
import adris.altoclef.util.serialization.Vec3dDeserializer;
import adris.altoclef.util.serialization.Vec3dSerializer;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

public class ConfigHelper {
  private static final String ALTO_FOLDER = "altoclef";
  
  private static final HashMap<String, Runnable> loadedConfigs = new HashMap<>();
  
  private static File getConfigFile(String path) {
    String fullPath = "altoclef" + File.separator + path;
    return new File(fullPath);
  }
  
  public static void reloadAllConfigs() {
    for (Runnable config : loadedConfigs.values())
      config.run(); 
  }
  
  private static <T> T getConfig(String path, Supplier<T> getDefault, Class<T> classToLoad) {
    T result = getDefault.get();
    File loadFrom = getConfigFile(path);
    if (!loadFrom.exists()) {
      saveConfig(path, result);
      return result;
    } 
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Vec3d.class, new Vec3dDeserializer());
    module.addDeserializer(ChunkPos.class, new ChunkPosDeserializer());
    module.addDeserializer(BlockPos.class, new BlockPosDeserializer());
    mapper.registerModule((Module)module);
    try {
      result = (T)mapper.readValue(loadFrom, classToLoad);
    } catch (JsonMappingException ex) {
      Debug.logError("Failed to parse Config file of type " + classToLoad.getSimpleName() + "at " + path + ". JSON Error Message: " + ex.getMessage() + ".\n JSON Error STACK TRACE:\n\n");
      ex.printStackTrace();
      if (result instanceof IFailableConfigFile) {
        IFailableConfigFile failable = (IFailableConfigFile)result;
        failable.failedToLoad();
      } 
      return result;
    } catch (IOException e) {
      Debug.logError("Failed to read Config at " + path + ".");
      e.printStackTrace();
      if (result instanceof IFailableConfigFile) {
        IFailableConfigFile failable = (IFailableConfigFile)result;
        failable.failedToLoad();
      } 
      return result;
    } 
    saveConfig(path, result);
    return result;
  }
  
  public static <T> void loadConfig(String path, Supplier<T> getDefault, Class<T> classToLoad, Consumer<T> onReload) {
    T config = getConfig(path, getDefault, classToLoad);
    loadedConfigs.put(path, () -> onReload.accept(config));
    onReload.accept(config);
  }
  
  public static <T> void saveConfig(String path, T config) {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(Vec3d.class, (JsonSerializer)new Vec3dSerializer());
    module.addSerializer(BlockPos.class, (JsonSerializer)new BlockPosSerializer());
    module.addSerializer(ChunkPos.class, (JsonSerializer)new ChunkPosSerializer());
    mapper.registerModule((Module)module);
    File configFile = getConfigFile(path);
    createParentDirectories(configFile);
    try {
      enablePrettyPrinting(mapper);
      writeConfigToFile(mapper, configFile, config);
    } catch (IOException e) {
      handleIOException(e);
    } 
  }
  
  private static void createParentDirectories(File file) {
    try {
      Path parentPath = file.getParentFile().toPath();
      Files.createDirectories(parentPath, (FileAttribute<?>[])new FileAttribute[0]);
    } catch (IOException e) {
      System.err.println("Failed to create parent directories: " + e.getMessage());
    } 
  }
  
  private static void enablePrettyPrinting(ObjectMapper mapper) {
    if (mapper != null) {
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
      prettyPrinter.indentArraysWith((DefaultPrettyPrinter.Indenter)DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
      mapper.writer((PrettyPrinter)prettyPrinter);
    } 
  }
  
  private static <T> void writeConfigToFile(ObjectMapper objectMapper, File configFile, T configData) throws IOException {
    Writer writer = new FileWriter(configFile);
    try {
      objectMapper.writeValue(writer, configData);
      writer.close();
    } catch (Throwable throwable) {
      try {
        writer.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  private static void handleIOException(IOException exception) {
    String errorMessage = "An IOException occurred: " + exception.getMessage();
    System.err.println(errorMessage);
  }
  
  private static <T extends IListConfigFile> T getListConfig(String path, Supplier<T> getDefault) {
    IListConfigFile iListConfigFile = (IListConfigFile)getDefault.get();
    iListConfigFile.onLoadStart();
    File configFile = getConfigFile(path);
    if (!configFile.exists())
      return (T)iListConfigFile; 
    try {
      FileInputStream fis = new FileInputStream(configFile);
      try {
        Scanner scanner = new Scanner(fis);
        try {
          while (scanner.hasNextLine()) {
            String line = trimComment(scanner.nextLine()).trim();
            if (line.isEmpty())
              continue; 
            iListConfigFile.addLine(line);
          } 
          scanner.close();
        } catch (Throwable throwable) {
          try {
            scanner.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
        fis.close();
      } catch (Throwable throwable) {
        try {
          fis.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } 
    return (T)iListConfigFile;
  }
  
  public static <T extends IListConfigFile> void loadListConfig(String path, Supplier<T> getDefault, Consumer<T> onReload) {
    T result = getListConfig(path, getDefault);
    loadedConfigs.put(path, () -> onReload.accept(result));
    onReload.accept(result);
  }
  
  private static String trimComment(String line) {
    int poundIndex = line.indexOf('#');
    if (poundIndex == -1)
      return line; 
    return line.substring(0, poundIndex);
  }
  
  public static void ensureCommentedListFileExists(String path, String startingComment) {
    File configFile = getConfigFile(path);
    if (configFile.exists())
      return; 
    StringBuilder commentBuilder = new StringBuilder();
    for (String line : startingComment.split("\\r?\\n")) {
      if (!line.isEmpty())
        commentBuilder.append("# ").append(line).append("\n"); 
    } 
    try {
      Files.write(configFile.toPath(), commentBuilder.toString().getBytes(), new java.nio.file.OpenOption[0]);
    } catch (IOException e) {
      handleException(e);
    } 
  }
  
  private static void handleException(IOException exception) {
    System.err.println("An error occurred: " + exception.getMessage());
  }
}
