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

package altoclef.trackers;

import altoclef.util.helpers.BaritoneHelper;
import altoclef.util.helpers.WorldHelper;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public class EntityStuckTracker extends Tracker {
   final float MOB_RANGE = 25.0F;
   private final Set<BlockPos> blockedSpots = new HashSet<>();

   public EntityStuckTracker(TrackerManager manager) {
      super(manager);
   }

   public boolean isBlockedByEntity(BlockPos pos) {
      this.ensureUpdated();
      synchronized (BaritoneHelper.MINECRAFT_LOCK) {
         return this.blockedSpots.contains(pos);
      }
   }

   @Override
   protected synchronized void updateState() {
      synchronized (BaritoneHelper.MINECRAFT_LOCK) {
         this.blockedSpots.clear();
         LivingEntity clientPlayerEntity = this.mod.getEntity();

         for (Entity entity : this.mod.getWorld().getAllEntities()) {
            if (entity != null && entity.isAlive() && !entity.equals(clientPlayerEntity) && clientPlayerEntity.closerThan(entity, 25.0)) {
               AABB b = entity.getBoundingBox();

               for (BlockPos p : WorldHelper.getBlocksTouchingBox(b)) {
                  this.blockedSpots.add(p);
               }
            }
         }
      }
   }

   @Override
   protected void reset() {
      this.blockedSpots.clear();
   }
}
