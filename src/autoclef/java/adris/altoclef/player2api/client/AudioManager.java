package adris.altoclef.player2api.client;

import java.lang.StackWalker.Option;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellij.lang.annotations.Identifier;

import adris.altoclef.player2api.ResourceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.Sound.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.SampledFloat;
import net.minecraft.world.entity.Entity;

public class AudioManager {
    private static Logger LOGGER = LogManager.getLogger();
    private static ExecutorService apiExecutor = Executors.newSingleThreadExecutor();
    private static SoundInstance currentlyPlaying = null;

    public static SoundInstance getSound(Entity entity, ResourceLocation soundLocation) {
        Sound sound = new Sound(
                soundLocation.getPath(),
                ConstantFloat.of(1.0f),
                ConstantFloat.of(1.0f),
                1,
                Sound.Type.FILE,
                false,
                false,
                16);
        SingleWeighedSoundEvents weightedSoundEvents = new SingleWeighedSoundEvents(sound, soundLocation, "");
        return new CustomEntityBoundSoundInstance(entity, soundLocation, weightedSoundEvents, sound);
    }

    private static void playSoundOnClient(Minecraft client, Entity entity, ResourceLocation soundLocation) {
        client.execute(() -> {
            client.getSoundManager().play(getSound(entity, soundLocation));
        });
    }

    public static String cleanPhrase(String p) {
        p = p.replaceAll("\\*.*\\*", "");
        p = p.replace("%supporter%", "someone");
        p = p.replace("%Supporter%", "someone");
        p = p.replace("some %2$s", "something");
        p = p.replace("at %2$s", "somewhere here");
        p = p.replace("At %2$s", "Somewhere here");
        p = p.replace(" to %2$s", " to here");
        p = p.replace(", %1$s.", ".");
        p = p.replace(", %1$s!", "!");
        p = p.replace(" %1$s!", "!");
        p = p.replace(", %1$s.", ".");
        p = p.replace("%1$s!", " ");
        p = p.replace("%1$s, ", " ");
        p = p.replace("%1$s", " ");
        p = p.replace("avoid %2$s", "avoid that location");
        p = p.replace(" Should be around %2$s.", "");
        p = p.replace("  ", " ");
        p = p.replace(" ,", ",");
        p = p.replace("Bahaha! ", "");
        p = p.replace("Run awaaaaaay! ", "Run!");
        p = p.replace("Aaaaaaaahhh! ", "");
        p = p.replace("Aaaaaaahhh! ", "");
        p = p.replace("Aaaaaaaaaaahhh! ", "");
        p = p.replace("AAAAAAAAAAAAAAAAAAAHHHHHH!!!!!! ", "");
        p = p.trim();
        return p;
    }

    public static void TTS(String message, Entity entity, String[] voiceIds) {
        Minecraft client = Minecraft.getInstance();
        client.execute(()-> {
            if (currentlyPlaying != null) {
                if (client.getSoundManager().isActive(currentlyPlaying)) {
                    LOGGER.error("Already playing entity, should not call TTS");
                    return;
                }
            }
            apiExecutor.submit(() -> {
                String text = cleanPhrase(message);
                String hash = String.format("%s/%s", AudioCache.getHash(voiceIds.toString()), AudioCache.getHash(text));
                if (AudioCache.get(hash, output -> {
                    downloadAudio(output, voiceIds, text);
                }, true)) {
                    ResourceLocation soundLocation = ResourceHelper.getResourceLocation("ttscache/hash");
                    playSoundOnClient(client, entity, soundLocation);
                }
            });
        });

    }
}
