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
import altoclef.tasksystem.Task;
import java.util.function.Function;

public abstract class PriorityTask {
   private final Function<AltoClefController, Boolean> canCall;
   private final boolean shouldForce;
   private final boolean canCache;
   public final boolean bypassForceCooldown;

   public PriorityTask(Function<AltoClefController, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
      this.canCall = canCall;
      this.shouldForce = shouldForce;
      this.canCache = canCache;
      this.bypassForceCooldown = bypassForceCooldown;
   }

   public final double calculatePriority(AltoClefController mod) {
      return !this.canCall.apply(mod) ? Double.NEGATIVE_INFINITY : this.getPriority(mod);
   }

   @Override
   public String toString() {
      return this.getDebugString();
   }

   public abstract Task getTask(AltoClefController var1);

   public abstract String getDebugString();

   protected abstract double getPriority(AltoClefController var1);

   public boolean needCraftingOnStart(AltoClefController mod) {
      return false;
   }

   public boolean shouldForce() {
      return this.shouldForce;
   }

   public boolean canCache() {
      return this.canCache;
   }
}
