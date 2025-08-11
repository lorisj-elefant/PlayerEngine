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

package altoclef.tasks.container;

import altoclef.AltoClefController;
import altoclef.TaskCatalogue;
import altoclef.tasks.movement.GetToXZTask;
import altoclef.tasksystem.Task;
import altoclef.trackers.storage.ContainerCache;
import altoclef.util.BlockRange;
import altoclef.util.ItemTarget;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;

public class StoreInStashTask extends Task {
   private final ItemTarget[] toStore;
   private final boolean getIfNotPresent;
   private final BlockRange stashRange;

   public StoreInStashTask(boolean getIfNotPresent, BlockRange stashRange, ItemTarget... toStore) {
      this.getIfNotPresent = getIfNotPresent;
      this.stashRange = stashRange;
      this.toStore = toStore;
   }

   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      ItemTarget[] itemsToStore = this.getItemsToStore(this.controller);
      if (itemsToStore.length == 0) {
         return null;
      } else {
         if (this.getIfNotPresent) {
            for (ItemTarget target : this.toStore) {
               if (this.controller.getItemStorage().getItemCount(target) < target.getTargetCount()) {
                  this.setDebugState("Collecting " + target + " before stashing.");
                  return TaskCatalogue.getItemTask(target);
               }
            }
         }

         Optional<BlockPos> closestContainer = this.controller.getBlockScanner().getNearestBlock((Predicate<BlockPos>)(pos -> {
            if (!this.stashRange.contains(this.controller, pos)) {
               return false;
            } else {
               Optional<ContainerCache> cache = this.controller.getItemStorage().getContainerAtPosition(pos);
               return cache.<Boolean>map(containerCache -> !containerCache.isFull()).orElse(true);
            }
         }), StoreInContainerTask.CONTAINER_BLOCKS);
         if (closestContainer.isPresent()) {
            this.setDebugState("Storing in closest stash container.");
            return new StoreInContainerTask(closestContainer.get(), false, itemsToStore);
         } else if (!this.stashRange.contains(this.controller, this.controller.getEntity().blockPosition())) {
            this.setDebugState("Traveling to stash area.");
            BlockPos centerStash = this.stashRange.getCenter();
            return new GetToXZTask(centerStash.getX(), centerStash.getZ());
         } else {
            this.setDebugState("Inside stash, but no non-full containers found. Cannot store items.");
            return null;
         }
      }
   }

   @Override
   public boolean isFinished() {
      return this.getItemsToStore(this.controller).length == 0;
   }

   private ItemTarget[] getItemsToStore(AltoClefController controller) {
      return Arrays.stream(this.toStore).filter(target -> controller.getItemStorage().hasItem(target.getMatches())).toArray(ItemTarget[]::new);
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   protected boolean isEqual(Task other) {
      return !(other instanceof StoreInStashTask task)
         ? false
         : task.stashRange.equals(this.stashRange)
            && task.getIfNotPresent == this.getIfNotPresent
            && Arrays.equals((Object[])task.toStore, (Object[])this.toStore);
   }

   @Override
   protected String toDebugString() {
      return "Storing in stash " + this.stashRange + ": " + Arrays.toString((Object[])this.toStore);
   }
}
