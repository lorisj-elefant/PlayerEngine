package adris.altoclef.player2api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LockManager {
    private static Logger LOGGER = LogManager.getLogger();

    private static boolean ttsLocked = false; // make sure we dont start processing until tts has finished (including
                                              // estimated wait)
    private static boolean onLLMResponseLock = false; // make sure we dont start processing until onLLMResponse has
                                                      // finished
    private static boolean codeGenLocked = false; // make sure we don't start processing until code gen has finished

    public static boolean isTTSLocked() {
        return ttsLocked;
    }

    // should we wait before processing next queue element.
    public static boolean processingNextQueueLock() {
        return ttsLocked || codeGenLocked || onLLMResponseLock;
    }

    public static void setTTS(boolean onOrOff) {
        LOGGER.info(String.format("TTS: %s lock", onOrOff ? "setting" : "releasing"));
        ttsLocked = onOrOff;
    }

    public static void setCodeGenLock(boolean onOrOff) {
        LOGGER.info(String.format("CodeGen: %s lock", onOrOff ? "setting" : "releasing"));
        codeGenLocked = onOrOff;
    }

    public static void setOnLLMResponseLock(boolean onOrOff) {
        LOGGER.info(String.format("llmResponse: %s lock", onOrOff ? "setting" : "releasing"));
        onLLMResponseLock = onOrOff;
    }

    public static boolean getCodeGenLock() {
        return codeGenLocked;
    }
}
