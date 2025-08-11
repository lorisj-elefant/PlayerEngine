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

package altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import altoclef.AltoClefController;
import altoclef.TaskCatalogue;
import altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.ItemPriorityCalculator;
import altoclef.tasksystem.Task;
import altoclef.util.ItemTarget;
import java.util.Arrays;
import java.util.function.Function;

public class ResourcePriorityTask extends PriorityTask {
   private final ItemPriorityCalculator priorityCalculator;
   private final ItemTarget[] collect;
   private boolean collected = false;
   private Task task = null;

   public ResourcePriorityTask(ItemPriorityCalculator priorityCalculator, Function<AltoClefController, Boolean> canCall, Task task, ItemTarget... collect) {
      this(priorityCalculator, canCall, false, true, false, collect);
      this.task = task;
   }

   public ResourcePriorityTask(ItemPriorityCalculator priorityCalculator, Function<AltoClefController, Boolean> canCall, ItemTarget... collect) {
      this(priorityCalculator, canCall, false, true, false, collect);
   }

   public ResourcePriorityTask(
      ItemPriorityCalculator priorityCalculator,
      Function<AltoClefController, Boolean> canCall,
      boolean shouldForce,
      boolean canCache,
      boolean bypassForceCooldown,
      ItemTarget... collect
   ) {
      super(canCall, shouldForce, canCache, bypassForceCooldown);
      this.collect = collect;
      this.priorityCalculator = priorityCalculator;
   }

   @Override
   public Task getTask(AltoClefController mod) {
      return (Task)(this.task != null ? this.task : TaskCatalogue.getSquashedItemTask(this.collect));
   }

   @Override
   public String getDebugString() {
      return "Collecting resource: " + Arrays.toString((Object[])this.collect);
   }

   @Override
   public double getPriority(AltoClefController mod) {
      if (this.collected) {
         return Double.NEGATIVE_INFINITY;
      } else {
         int count = 0;

         for (ItemTarget target : this.collect) {
            count += mod.getItemStorage().getItemCount(target.getMatches());
         }

         if (count >= this.priorityCalculator.maxCount) {
            this.collected = true;
         }

         return this.priorityCalculator.getPriority(count);
      }
   }

   public boolean isCollected() {
      return this.collected;
   }
}
