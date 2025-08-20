package adris.altoclef.player2api.client;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;


public class CustomEntityBoundSoundInstance implements SoundInstance {
    private final SingleWeighedSoundEvents weighedSoundEvents;
    private final ResourceLocation location;
    private final Entity entity;
    private final Sound sound;

    public CustomEntityBoundSoundInstance(Entity entity, ResourceLocation location, SingleWeighedSoundEvents weighedSoundEvents, Sound sound) {
        this.entity = entity;
        this.weighedSoundEvents = weighedSoundEvents;
        this.location = location;
        this.sound = sound;
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager){
        // TODO: dont know what to do here, maybe have to remove sound?

        return weighedSoundEvents;
    }

    @Override
    public SoundSource getSource(){
        return SoundSource.PLAYERS;
    }

    @Override
    public boolean isLooping(){
        return false;
    }

    @Override
    public Attenuation getAttenuation() {
        return Attenuation.LINEAR;
    }
    @Override
    public int getDelay() {
        return 0;
    }
    @Override
    public ResourceLocation getLocation() {
        return location;
    }
    @Override
    public float getPitch() {
        return 1;
    }

    @Override
    public Sound getSound() {
        return sound;
    }
    @Override
    public float getVolume() {
        return 1;
    }



    @Override
    public double getX() {
        return entity.getX();
    }
    @Override
    public double getY() {
        return entity.getY();
    }
    @Override
    public double getZ() {
        return entity.getZ();
    }

    @Override
    public boolean isRelative() {
        // TODO: dont know
        return false;
    }
}