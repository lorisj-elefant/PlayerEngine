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

package altoclef.tasks.movement;

import altoclef.tasksystem.Task;
import altoclef.util.helpers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biomes;

public class LocateDesertTempleTask extends Task {
   private BlockPos finalPos;

   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      BlockPos desertTemplePos = WorldHelper.getADesertTemple(this.controller);
      if (desertTemplePos != null) {
         this.finalPos = desertTemplePos.above(14);
      }

      if (this.finalPos != null) {
         this.setDebugState("Going to found desert temple");
         return new GetToBlockTask(this.finalPos, false);
      } else {
         return new SearchWithinBiomeTask(Biomes.DESERT);
      }
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof LocateDesertTempleTask;
   }

   @Override
   protected String toDebugString() {
      return "Searchin' for temples";
   }

   @Override
   public boolean isFinished() {
      return this.controller.getPlayer().blockPosition().equals(this.finalPos);
   }
}
