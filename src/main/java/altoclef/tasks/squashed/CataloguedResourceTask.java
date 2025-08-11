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

package altoclef.tasks.squashed;

import altoclef.AltoClefController;
import altoclef.TaskCatalogue;
import altoclef.tasks.ResourceTask;
import altoclef.tasks.container.CraftInTableTask;
import altoclef.tasks.container.UpgradeInSmithingTableTask;
import altoclef.tasksystem.Task;
import altoclef.util.ItemTarget;
import altoclef.util.helpers.StorageHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;

public class CataloguedResourceTask extends ResourceTask {
   private final CataloguedResourceTask.TaskSquasher squasher = new CataloguedResourceTask.TaskSquasher();
   private final ItemTarget[] targets;
   private final List<ResourceTask> tasksToComplete;

   public CataloguedResourceTask(boolean squash, ItemTarget... targets) {
      super(targets);
      this.targets = targets;
      this.tasksToComplete = new ArrayList<>(targets.length);

      for (ItemTarget target : targets) {
         if (target != null) {
            this.tasksToComplete.add(TaskCatalogue.getItemTask(target));
         }
      }

      if (squash) {
         this.squashTasks(this.tasksToComplete);
      }
   }

   public CataloguedResourceTask(ItemTarget... targets) {
      this(true, targets);
   }

   @Override
   protected void onResourceStart(AltoClefController mod) {
   }

   @Override
   protected Task onResourceTick(AltoClefController mod) {
      for (ResourceTask task : this.tasksToComplete) {
         for (ItemTarget target : task.getItemTargets()) {
            if (!StorageHelper.itemTargetsMetInventory(mod, target)) {
               return task;
            }
         }
      }

      return null;
   }

   @Override
   public boolean isFinished() {
      for (ResourceTask task : this.tasksToComplete) {
         for (ItemTarget target : task.getItemTargets()) {
            if (!StorageHelper.itemTargetsMetInventory(this.controller, target)) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   protected boolean shouldAvoidPickingUp(AltoClefController mod) {
      return false;
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CataloguedResourceTask task ? Arrays.equals((Object[])task.targets, (Object[])this.targets) : false;
   }

   @Override
   protected String toDebugStringName() {
      return "Get catalogued: " + ArrayUtils.toString(this.targets);
   }

   private void squashTasks(List<ResourceTask> tasks) {
      this.squasher.addTasks(tasks);
      tasks.clear();
      tasks.addAll(this.squasher.getSquashed());
   }

   static class TaskSquasher {
      private final Map<Class, TypeSquasher> squashMap = new HashMap<>();
      private final List<ResourceTask> unSquashableTasks = new ArrayList<>();

      public TaskSquasher() {
         this.squashMap.put(CraftInTableTask.class, new CraftSquasher());
         this.squashMap.put(UpgradeInSmithingTableTask.class, new SmithingSquasher());
      }

      public void addTask(ResourceTask t) {
         Class type = t.getClass();
         if (this.squashMap.containsKey(type)) {
            this.squashMap.get(type).add(t);
         } else {
            this.unSquashableTasks.add(t);
         }
      }

      public void addTasks(List<ResourceTask> tasks) {
         for (ResourceTask task : tasks) {
            this.addTask(task);
         }
      }

      public List<ResourceTask> getSquashed() {
         List<ResourceTask> result = new ArrayList<>();

         for (Class type : this.squashMap.keySet()) {
            result.addAll(this.squashMap.get(type).getSquashed());
         }

         result.addAll(this.unSquashableTasks);
         return result;
      }
   }
}
