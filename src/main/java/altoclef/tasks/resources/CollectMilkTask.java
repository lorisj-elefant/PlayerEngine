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
import altoclef.Debug;
import altoclef.TaskCatalogue;
import altoclef.tasks.ResourceTask;
import altoclef.tasks.entity.AbstractDoToEntityTask;
import altoclef.tasksystem.Task;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CollectMilkTask extends ResourceTask {
   private final int count;

   public CollectMilkTask(int targetCount) {
      super(Items.MILK_BUCKET, targetCount);
      this.count = targetCount;
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
      if (!mod.getItemStorage().hasItem(Items.BUCKET)) {
         return TaskCatalogue.getItemTask(Items.BUCKET, 1);
      } else {
         return (Task)(!mod.getEntityTracker().entityFound(Cow.class) && this.isInWrongDimension(mod)
            ? this.getToCorrectDimensionTask(mod)
            : new CollectMilkTask.MilkCowTask());
      }
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectMilkTask;
   }

   @Override
   protected String toDebugStringName() {
      return "Collecting " + this.count + " milk buckets.";
   }

   static class MilkCowTask extends AbstractDoToEntityTask {
      public MilkCowTask() {
         super(0.0, -1.0, -1.0);
      }

      @Override
      protected boolean isSubEqual(AbstractDoToEntityTask other) {
         return other instanceof CollectMilkTask.MilkCowTask;
      }

      @Override
      protected Task onEntityInteract(AltoClefController mod, Entity entity) {
         if (!mod.getItemStorage().hasItem(Items.BUCKET)) {
            Debug.logWarning("Failed to milk cow because you have no bucket.");
            return null;
         } else {
            if (mod.getSlotHandler().forceEquipItem(Items.BUCKET)) {
               mod.getInventory().setItem(mod.getInventory().selectedSlot, new ItemStack(Items.MILK_BUCKET));
            }

            return null;
         }
      }

      @Override
      protected Optional<Entity> getEntityTarget(AltoClefController mod) {
         return mod.getEntityTracker().getClosestEntity(mod.getPlayer().position(), Cow.class);
      }

      @Override
      protected String toDebugString() {
         return "Milking Cow";
      }
   }
}
