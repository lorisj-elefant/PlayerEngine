package adris.altoclef.util.baritone;

import adris.altoclef.util.time.TimerGame;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Type;

public class CachedProjectile {
    private final TimerGame lastCache = new TimerGame(2.0D);

    public Vec3d velocity;

    public Vec3d position;

    public double gravity;

    public Type projectileType;

    private Vec3d cachedHit;

    private boolean cacheHeld = false;

    public Vec3d getCachedHit() {
        return this.cachedHit;
    }

    public void setCacheHit(Vec3d cache) {
        this.cachedHit = cache;
        this.cacheHeld = true;
        this.lastCache.reset();
    }

    public boolean needsToRecache() {
        return (!this.cacheHeld || this.lastCache.elapsed());
    }
}
