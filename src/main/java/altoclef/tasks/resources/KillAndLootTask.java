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

package altoclef.tasks.resources;

import altoclef.AltoClefController;
import altoclef.tasks.ResourceTask;
import altoclef.tasks.entity.KillEntitiesTask;
import altoclef.tasks.movement.TimeoutWanderTask;
import altoclef.tasksystem.Task;
import altoclef.util.ItemTarget;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;

public class KillAndLootTask extends ResourceTask {
   private final Class<?> toKill;
   private final Task killTask;

   public KillAndLootTask(Class<?> toKill, Predicate<Entity> shouldKill, ItemTarget... itemTargets) {
      super((ItemTarget[])itemTargets.clone());
      this.toKill = toKill;
      this.killTask = new KillEntitiesTask(shouldKill, this.toKill);
   }

   public KillAndLootTask(Class<?> toKill, ItemTarget... itemTargets) {
      super((ItemTarget[])itemTargets.clone());
      this.toKill = toKill;
      this.killTask = new KillEntitiesTask(this.toKill);
   }

   @Override
   protected boolean shouldAvoidPickingUp(AltoClefController mod) {
      return false;
   }

   @Override
   protected void onResourceStart(AltoClefController mod) {
   }

   @Override
   protected Task onResourceTick(AltoClefController mod) {
      if (!mod.getEntityTracker().entityFound(this.toKill)) {
         if (this.isInWrongDimension(mod)) {
            this.setDebugState("Going to correct dimension.");
            return this.getToCorrectDimensionTask(mod);
         } else {
            this.setDebugState("Searching for mob...");
            return new TimeoutWanderTask();
         }
      } else {
         return this.killTask;
      }
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof KillAndLootTask task ? task.toKill.equals(this.toKill) : false;
   }

   @Override
   protected String toDebugStringName() {
      return "Collect items from " + this.toKill.toGenericString();
   }
}
