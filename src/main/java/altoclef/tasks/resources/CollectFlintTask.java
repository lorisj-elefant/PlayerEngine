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
import altoclef.TaskCatalogue;
import altoclef.tasks.DoToClosestBlockTask;
import altoclef.tasks.ResourceTask;
import altoclef.tasks.construction.DestroyBlockTask;
import altoclef.tasks.construction.PlaceBlockNearbyTask;
import altoclef.tasksystem.Task;
import altoclef.util.helpers.WorldHelper;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class CollectFlintTask extends ResourceTask {
   private static final float CLOSE_ENOUGH_FLINT = 10.0F;
   private final int count;

   public CollectFlintTask(int targetCount) {
      super(Items.FLINT, targetCount);
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
      Optional<BlockPos> closest = mod.getBlockScanner()
         .getNearestBlock(
            mod.getPlayer().position(),
            validGravel -> WorldHelper.fallingBlockSafeToBreak(this.controller, validGravel) && WorldHelper.canBreak(this.controller, validGravel),
            Blocks.GRAVEL
         );
      if (closest.isPresent() && closest.get().closerToCenterThan(mod.getPlayer().position(), 10.0)) {
         return new DoToClosestBlockTask(DestroyBlockTask::new, Blocks.GRAVEL);
      } else {
         return (Task)(mod.getItemStorage().hasItem(Items.GRAVEL) ? new PlaceBlockNearbyTask(Blocks.GRAVEL) : TaskCatalogue.getItemTask(Items.GRAVEL, 1));
      }
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectFlintTask task ? task.count == this.count : false;
   }

   @Override
   protected String toDebugStringName() {
      return "Collect " + this.count + " flint";
   }
}
