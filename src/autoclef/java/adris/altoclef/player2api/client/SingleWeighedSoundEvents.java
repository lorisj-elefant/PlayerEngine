
package adris.altoclef.player2api.client;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class SingleWeighedSoundEvents extends WeighedSoundEvents {
    private final Sound sound;

    public SingleWeighedSoundEvents(Sound sound, ResourceLocation identifier, String string) {
        super(identifier, string);
        this.sound = sound;
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public Sound getSound(RandomSource randomSource) {
        return sound;
    }

    public Sound getSound() {
        return sound;
    }
}