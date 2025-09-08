package adris.altoclef.player2api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LockManager {
    private static Logger LOGGER = LogManager.getLogger();

    private static boolean ttsLocked = false;
    private static boolean codeGenLocked = false;

    public static boolean isTTSLocked() {
        return ttsLocked;
    }

    public static boolean globalIsLocked() {
        return ttsLocked || codeGenLocked;
    }

    public static void setTTS(boolean onOrOff) {
        LOGGER.info(String.format("TTS: %s lock", onOrOff ? "setting" : "releasing"));
        ttsLocked = onOrOff;
    }

    public static void setCodeGenLock(boolean onOrOff) {
        LOGGER.info(String.format("CodeGen: %s lock", onOrOff ? "setting" : "releasing"));
        codeGenLocked = onOrOff;
    }
}
