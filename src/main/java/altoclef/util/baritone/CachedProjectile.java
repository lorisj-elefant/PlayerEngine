/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package altoclef.util.baritone;

import altoclef.util.time.TimerGame;
import java.lang.reflect.Type;
import net.minecraft.world.phys.Vec3;

public class CachedProjectile {
   private final TimerGame lastCache = new TimerGame(2.0);
   public Vec3 velocity;
   public Vec3 position;
   public double gravity;
   public Type projectileType;
   private Vec3 cachedHit;
   private boolean cacheHeld = false;

   public Vec3 getCachedHit() {
      return this.cachedHit;
   }

   public void setCacheHit(Vec3 cache) {
      this.cachedHit = cache;
      this.cacheHeld = true;
      this.lastCache.reset();
   }

   public boolean needsToRecache() {
      return !this.cacheHeld || this.lastCache.elapsed();
   }
}
