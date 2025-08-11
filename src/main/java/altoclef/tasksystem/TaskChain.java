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

package altoclef.tasksystem;

import altoclef.AltoClefController;
import java.util.ArrayList;
import java.util.List;

public abstract class TaskChain {
   protected AltoClefController controller;
   private final List<Task> cachedTaskChain = new ArrayList<>();

   public TaskChain(TaskRunner runner) {
      runner.addTaskChain(this);
      this.controller = runner.getMod();
   }

   public void tick() {
      this.cachedTaskChain.clear();
      this.onTick();
   }

   public void stop() {
      this.cachedTaskChain.clear();
      this.onStop();
   }

   protected abstract void onStop();

   public abstract void onInterrupt(TaskChain var1);

   protected abstract void onTick();

   public abstract float getPriority();

   public abstract boolean isActive();

   public abstract String getName();

   public List<Task> getTasks() {
      return this.cachedTaskChain;
   }

   void addTaskToChain(Task task) {
      this.cachedTaskChain.add(task);
   }

   @Override
   public String toString() {
      return this.getName();
   }
}
