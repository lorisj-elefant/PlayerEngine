
package com.player2.playerengine.player2api.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.WebSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;

import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioFileFormat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.ResourceLocation;

public class STTUtils {
    public static final Logger LOGGER = LogManager.getLogger();

    // state:
    public static volatile boolean isListening = false;
    private static volatile boolean recordingThreadRunning = false;
    private static TargetDataLine line;
    private static int chunkCount = 0;
    private static WebSocketUtils wsutils;
    private static String clientId;

    // audio format:
    private static final float SAMPLE_RATE = 16000f;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1;
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true,
            false);

    // chunk format (50ms audio, will be amount of audio sent per websocket event)
    private static final int CHUNK_MS = 50;
    private static final int FRAME_SIZE = AUDIO_FORMAT.getFrameSize(); // bytes per frame (2 for 16 bit mono)
    private static final int SAMPLES_PER_CHUNK = (int) (SAMPLE_RATE * (CHUNK_MS / 1000.0));
    private static final int BYTES_PER_CHUNK = SAMPLES_PER_CHUNK * FRAME_SIZE;
    // accumulator for partial reads:
    private static final byte[] accumulateBuffer50ms = new byte[BYTES_PER_CHUNK];
    // how many bytes currently in accumulate buffer
    private static int accumulatePos = 0;

    // bounded queue for chunks awaiting upload:
    private static final int QUEUE_CAPACITY = 256; // tune for your app: 256 * 1600 ≈ 400 KB
    private static final BlockingQueue<byte[]> CHUNK_QUEUE = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    // executor for sending chunks asynchronously
    private static final ExecutorService SENDER_POOL = Executors.newFixedThreadPool(2);

    public static void update() {

    }

    // called on mod init
    public static void onInitialize() {
        try {
            line = AudioSystem.getTargetDataLine(AUDIO_FORMAT);
            line.open(AUDIO_FORMAT);
            LOGGER.info("Audio line opened.");
        } catch (Exception e) {
            LOGGER.error("Failed to open audio line", e);
        }
    }

    // this is called externally when STT is active (i.e. keybind):
    public static void setIsListening(boolean v, String clientId) {
        isListening = v;
        STTUtils.clientId = clientId;
        if (isListening) {
            startRecordingIfNeeded();
        } else {
            stopRecording();
        }
    }

    private static void startRecordingIfNeeded() {
        if (recordingThreadRunning)
            return;
        if (line == null) {
            LOGGER.error("Audio line not initialized. Call onInitialize() first.");
            return;
        }
        LOGGER.info("STT: Starting recording");
        recordingThreadRunning = true;
        line.start();
        LOGGER.info("STT: line started");

        // producer thread: records audio chunks using line
        Thread producer = new Thread(STTUtils::recordLoop, "STT-Audio-Producer");
        producer.setDaemon(true);
        producer.start();

        // consumer thread: relays chunks to sender pool
        Thread consumer = new Thread(STTUtils::consumeLoop, "STT-Chunk-Consumer");
        consumer.setDaemon(true);
        consumer.start();

        LOGGER.info("STT/startRecordingIfNeeded: consumer/producer started.");
    }

    private static void recordLoop() {
        byte[] rawReadBuffer = new byte[4096]; // at 16000 sample rate, 16 bit mono, 2 bytes per sample => 32k bytes/
                                               // sec => 4096 bytes => 128 ms, which is >50ms
        try {
            while (isListening && line != null && line.isOpen()) {
                // note: should block until at least 1 frame availible
                int numBytesRecorded = line.read(rawReadBuffer, 0, rawReadBuffer.length);

                if (numBytesRecorded <= 0)
                    continue;

                // if got >50ms split into 50ms chunks and add to queue
                int offset = 0;
                while (offset < numBytesRecorded) {
                    int need = BYTES_PER_CHUNK - accumulatePos;
                    int numBytesToCopy = Math.min(need, numBytesRecorded - offset);
                    System.arraycopy(rawReadBuffer, offset, accumulateBuffer50ms, accumulatePos, numBytesToCopy);
                    accumulatePos += numBytesToCopy;
                    offset += numBytesToCopy;

                    if (accumulatePos >= BYTES_PER_CHUNK) {
                        // if 50ms chunk ready, queue it and reset accumulateBuffer50ms
                        byte[] chunk = new byte[BYTES_PER_CHUNK];
                        System.arraycopy(accumulateBuffer50ms, 0, chunk, 0, BYTES_PER_CHUNK);
                        boolean queued = CHUNK_QUEUE.offer(chunk);
                        if (!queued) {
                            // queue full, drop oldest
                            LOGGER.warn("STT recordLoop: Chunk queue full. Dropping oldest chunk");
                            CHUNK_QUEUE.poll();
                            CHUNK_QUEUE.offer(chunk);
                        }
                        // reset accumulator
                        accumulatePos = 0;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getStackTrace());
        } finally {
            // process final partial chunk
            if (accumulatePos > 0) {
                LOGGER.info("STT: processing remaining final chunk after stoping recording");
                byte[] last = new byte[accumulatePos];
                System.arraycopy(accumulateBuffer50ms, 0, last, 0, accumulatePos);
                boolean queued = CHUNK_QUEUE.offer(last);
                if (!queued) {
                    LOGGER.warn("STT: queue full, dropping last partial chunk");
                }
                accumulatePos = 0;
            }
            recordingThreadRunning = false;
            try {
                if (line != null)
                    line.stop();
            } catch (Exception ignored) {
            }
            LOGGER.info("STT: recording thread exiting");
        }
    }

    private static void dispatchChunk(byte[] pcmChunk) {
        SENDER_POOL.submit(() -> {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(pcmChunk);
                AudioInputStream ais = new AudioInputStream(bais, AUDIO_FORMAT, SAMPLES_PER_CHUNK);

                File wavOut = new File(String.format("recorded(%d).wav", chunkCount));
                chunkCount++;
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavOut);

                // byte[] wav = AudioUtils.pcmToWav(pcm, (int) sampleRate, channels,
                // sampleSizeInBits);
                // try (FileOutputStream fos = new FileOutputStream(wavOut)) {
                // fos.write(wav);
                // }

                LOGGER.info("WAV saved at: " + wavOut.getAbsolutePath());

                // sendChunkToApi(pcmChunk);
            } catch (Exception e) {
                LOGGER.error("Failed to dispatch chunk", e);
            }
        });
    }

    private static void stopRecording() {
        isListening = false;
        LOGGER.info("Stop requested; waiting for producer/consumer to finish");
    }

    private static void consumeLoop() {
        try {
            while (isListening || !CHUNK_QUEUE.isEmpty()) {
                byte[] chunk = CHUNK_QUEUE.poll(200, TimeUnit.MILLISECONDS);
                if (chunk == null)
                    continue;
                final byte[] toSend = chunk;
                dispatchChunk(toSend);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.info("STT: consumer interrupted", e);
        } finally {
            LOGGER.info("STT: consumer thread exiting");
        }
    }

    public static void shutdown() {
        isListening = false;
        try {
            if (line != null) {
                line.stop();
                line.close();
            }
        } catch (Exception ignored) {
        }
        SENDER_POOL.shutdown();
        try {
            if (!SENDER_POOL.awaitTermination(2, TimeUnit.SECONDS)) {
                SENDER_POOL.shutdownNow();
            }
        } catch (InterruptedException e) {
            SENDER_POOL.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("STTUtils shutdown complete.");
    }

    public static void connect(String token) {
        try {

            wsutils = new WebSocketUtils();
            wsutils.connect(new URI("wss://api.player2.game"), token);
            wsutils.registerHandler("session", (obj) -> {
                LOGGER.info("WSM: session: {}", obj);
            });
            wsutils.registerHandler("open", (obj) -> {
                LOGGER.info("WSM: open: {}", obj);
            });
            wsutils.registerHandler("messsage", (obj) -> {
                LOGGER.info("WSM: message: {}", obj);
            });
            wsutils.registerHandler("speech_started", (obj) -> {
                LOGGER.info("WSM: speech_started: {}", obj);
            });
            wsutils.registerHandler("utterance_end", (obj) -> {
                LOGGER.info("WSM: utternace_end: {}", obj);
            });

            wsutils.registerHandler("close", (obj) -> {
                LOGGER.info("WSM: close: {}", obj);
                wsutils = null;
            });

            wsutils.registerHandler("error", (obj) -> {
                LOGGER.info("WSM: error: {}", obj);
            });
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public void send() {
        if (wsutils == null) {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(),
                    Minecraft.getInstance().player.registryAccess());
            buf.writeUtf(clientId);
            Minecraft.getInstance().getConnection().send(
                    NetworkManager.toPacket(
                            NetworkManager.Side.C2S,
                            ResourceLocation.fromNamespaceAndPath("playerengine", "request_stt"),
                            buf));
        }
    }
}
