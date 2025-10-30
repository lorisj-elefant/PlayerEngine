package com.player2.playerengine.player2api.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class STTUtils {
    public static volatile boolean isListening = false;
    private static float sampleRate = 16000f;
    private static int sampleSizeInBits = 16; // bit rate?
    private static int channels = 1;

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(sampleRate, sampleSizeInBits, channels, true,
            false);
    private static TargetDataLine line;
    private static ByteArrayOutputStream buffer;

    public static void update() {
        if (isListening && line == null) {
            startRecording();
        }
        if (!isListening && line != null) {
            stopAndTranscribe();
        }
    }

    private static void startRecording() {
        try {
            line = AudioSystem.getTargetDataLine(AUDIO_FORMAT);
            line.open(AUDIO_FORMAT);
            line.start();
            buffer = new ByteArrayOutputStream();

            Thread t = new Thread(() -> {
                byte[] chunk = new byte[4096];
                while (isListening && line != null) {
                    int read = line.read(chunk, 0, chunk.length);
                    System.out.println("STT-read: " + read);
                    if (read > 0) {
                        buffer.write(chunk, 0, read);
                    }
                }
            }, "STT-Audio");
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void stopAndTranscribe() {
        try {
            line.stop();
            line.close();
        } catch (Exception ignore) {
        }
        line = null;

        byte[] pcm = buffer.toByteArray();
        buffer = null;

        // Fire async transcription task
        new Thread(() -> {
            try {
                byte[] wav = AudioUtils.pcmToWav(pcm, (int) sampleRate, channels, sampleSizeInBits);

                File out = new File("recorded.wav");
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    fos.write(wav);
                }

                System.out.println("WAV saved at: " + out.getAbsolutePath());
                // String text = SttApi.transcribeWav(wav);
                // System.out.println("Recognized: " + text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "STT-Transcribe").start();
    }

}
