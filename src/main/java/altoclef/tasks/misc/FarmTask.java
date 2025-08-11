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

package altoclef.tasks.misc;

import altoclef.tasksystem.Task;
import baritone.api.process.IFarmProcess;
import java.util.Objects;
import net.minecraft.core.BlockPos;

public class FarmTask extends Task {
   private final Integer range;
   private final BlockPos center;

   public FarmTask(Integer range, BlockPos center) {
      this.range = range;
      this.center = center;
   }

   public FarmTask() {
      this(null, null);
   }

   @Override
   protected void onStart() {
      IFarmProcess farmProcess = this.controller.getBaritone().getFarmProcess();
      if (this.range != null && this.center != null) {
         farmProcess.farm(this.range, this.center);
      } else if (this.range != null) {
         farmProcess.farm(this.range);
      } else {
         farmProcess.farm();
      }
   }

   @Override
   protected Task onTick() {
      IFarmProcess farmProcess = this.controller.getBaritone().getFarmProcess();
      if (!farmProcess.isActive()) {
         this.onStart();
      }

      this.setDebugState("Farming with Automatone...");
      return null;
   }

   @Override
   protected void onStop(Task interruptTask) {
      IFarmProcess farmProcess = this.controller.getBaritone().getFarmProcess();
      if (farmProcess.isActive()) {
         farmProcess.onLostControl();
      }
   }

   @Override
   public boolean isFinished() {
      return false;
   }

   @Override
   protected boolean isEqual(Task other) {
      return !(other instanceof FarmTask task) ? false : Objects.equals(task.range, this.range) && Objects.equals(task.center, this.center);
   }

   @Override
   protected String toDebugString() {
      return this.range != null && this.center != null ? "Farming in range " + this.range + " around " + this.center.toShortString() : "Farming nearby";
   }
}
