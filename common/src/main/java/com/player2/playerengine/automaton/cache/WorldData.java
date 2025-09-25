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

package com.player2.playerengine.automaton.cache;

import com.player2.playerengine.automaton.api.cache.ICachedWorld;
import com.player2.playerengine.automaton.api.cache.IContainerMemory;
import com.player2.playerengine.automaton.api.cache.IWaypointCollection;
import com.player2.playerengine.automaton.api.cache.IWorldData;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class WorldData implements IWorldData {
   private final WaypointCollection waypoints = new WaypointCollection();
   private final ContainerMemory containerMemory = new ContainerMemory();
   public final ResourceKey<Level> dimension;

   WorldData(ResourceKey<Level> dimension) {
      this.dimension = dimension;
   }

   public void readFromNbt(HolderLookup.Provider levelRegistryAccess, CompoundTag tag) {
      this.containerMemory.read(levelRegistryAccess, tag.getCompound("containers"));
      this.waypoints.readFromNbt(tag.getCompound("waypoints"));
   }

   public void writeToNbt(HolderLookup.Provider levelRegistryAccess, CompoundTag tag) {
      tag.put("containers", this.containerMemory.toNbt(levelRegistryAccess));
      tag.put("waypoints", this.waypoints.toNbt());
   }

   @Override
   public ICachedWorld getCachedWorld() {
      return new ICachedWorld() {
         @Override
         public boolean isCached(int blockX, int blockZ) {
            return false;
         }

         @Override
         public ArrayList<BlockPos> getLocationsOf(String block, int maximum, int centerX, int centerZ, int maxRegionDistanceSq) {
            return new ArrayList<>();
         }
      };
   }

   @Override
   public IWaypointCollection getWaypoints() {
      return this.waypoints;
   }

   @Override
   public IContainerMemory getContainerMemory() {
      return this.containerMemory;
   }
}
