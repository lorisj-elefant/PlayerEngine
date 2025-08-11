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
import altoclef.tasks.entity.DoToClosestEntityTask;
import altoclef.tasks.movement.DefaultGoToDimensionTask;
import altoclef.tasks.movement.GetToEntityTask;
import altoclef.tasksystem.Task;
import altoclef.util.Dimension;
import altoclef.util.helpers.WorldHelper;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.Items;

public class CollectEggsTask extends ResourceTask {
   private final int count;
   private final DoToClosestEntityTask waitNearChickens;
   private AltoClefController mod;

   public CollectEggsTask(int targetCount) {
      super(Items.EGG, targetCount);
      this.count = targetCount;
      this.waitNearChickens = new DoToClosestEntityTask(chicken -> new GetToEntityTask(chicken, 5.0), Chicken.class);
   }

   @Override
   protected boolean shouldAvoidPickingUp(AltoClefController mod) {
      return false;
   }

   @Override
   protected void onResourceStart(AltoClefController mod) {
      this.mod = mod;
   }

   @Override
   protected Task onResourceTick(AltoClefController mod) {
      if (this.waitNearChickens.wasWandering() && WorldHelper.getCurrentDimension(this.controller) != Dimension.OVERWORLD) {
         this.setDebugState("Going to right dimension.");
         return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
      } else {
         this.setDebugState("Waiting around chickens. Yes.");
         return this.waitNearChickens;
      }
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectEggsTask;
   }

   @Override
   protected String toDebugStringName() {
      return "Collecting " + this.count + " eggs.";
   }
}
