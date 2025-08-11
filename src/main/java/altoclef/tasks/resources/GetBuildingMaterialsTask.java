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

import altoclef.tasksystem.Task;
import altoclef.util.ItemTarget;
import altoclef.util.MiningRequirement;
import altoclef.util.helpers.StorageHelper;
import net.minecraft.world.item.Item;

public class GetBuildingMaterialsTask extends Task {
   private final int count;

   public GetBuildingMaterialsTask(int count) {
      this.count = count;
   }

   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      Item[] throwaways = this.controller.getModSettings().getThrowawayItems(this.controller, true);
      return new MineAndCollectTask(new ItemTarget[]{new ItemTarget(throwaways, this.count)}, MiningRequirement.WOOD);
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof GetBuildingMaterialsTask task ? task.count == this.count : false;
   }

   @Override
   public boolean isFinished() {
      return StorageHelper.getBuildingMaterialCount(this.controller) >= this.count;
   }

   @Override
   protected String toDebugString() {
      return "Collecting " + this.count + " building materials.";
   }
}
