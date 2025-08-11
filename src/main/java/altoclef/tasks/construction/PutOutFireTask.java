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

package altoclef.tasks.construction;

import altoclef.tasks.InteractWithBlockTask;
import altoclef.tasksystem.Task;
import altoclef.util.ItemTarget;
import baritone.api.utils.input.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PutOutFireTask extends Task {
   private final BlockPos firePosition;

   public PutOutFireTask(BlockPos firePosition) {
      this.firePosition = firePosition;
   }

   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      return new InteractWithBlockTask(ItemTarget.EMPTY, null, this.firePosition, Input.CLICK_LEFT, false, false);
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   public boolean isFinished() {
      BlockState s = this.controller.getWorld().getBlockState(this.firePosition);
      return s.getBlock() != Blocks.FIRE && s.getBlock() != Blocks.SOUL_FIRE;
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof PutOutFireTask task ? task.firePosition.equals(this.firePosition) : false;
   }

   @Override
   protected String toDebugString() {
      return "Putting out fire at " + this.firePosition;
   }
}
