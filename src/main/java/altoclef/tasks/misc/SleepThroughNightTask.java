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

public class SleepThroughNightTask extends Task {
   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      return new PlaceBedAndSetSpawnTask().stayInBed();
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof SleepThroughNightTask;
   }

   @Override
   protected String toDebugString() {
      return "Sleeping through the night";
   }

   @Override
   public boolean isFinished() {
      int time = (int)(this.controller.getWorld().getDayTime() % 24000L);
      return 0 <= time && time < 13000;
   }
}
