package adris.altoclef.brain.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import adris.altoclef.brain.shared.Character;

public class TTSQueue {
    private static final ExecutorService ttsThread = Executors.newSingleThreadExecutor();
    public static boolean isSpeaking = false;

    public static void TTS(String text, Character character){
        ttsThread.submit(() -> {
            isSpeaking = true;
            Player2APIService.textToSpeech(text, character);
            // for now, wait 5 sec. Later on wait for audio file to complete
            try {
                Thread.sleep(5000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            isSpeaking = false;
        });
    }
}
