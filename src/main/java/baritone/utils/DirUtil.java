package baritone.utils;

import java.nio.file.Path;
import net.minecraftforge.fml.loading.FMLPaths;

public class DirUtil {
   public static Path getGameDir() {
      return FMLPaths.GAMEDIR.get();
   }

   public static Path getConfigDir() {
      return FMLPaths.CONFIGDIR.get();
   }
}
