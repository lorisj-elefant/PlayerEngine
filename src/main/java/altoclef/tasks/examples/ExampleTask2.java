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

package altoclef.tasks.examples;

import altoclef.AltoClefController;
import altoclef.tasks.movement.GetToBlockTask;
import altoclef.tasks.movement.TimeoutWanderTask;
import altoclef.tasksystem.Task;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ExampleTask2 extends Task {
   private BlockPos target = null;

   @Override
   protected void onStart() {
      AltoClefController mod = this.controller;
      mod.getBehaviour().push();
      mod.getBehaviour().avoidBlockBreaking((Predicate<BlockPos>)(blockPos -> {
         BlockState s = mod.getWorld().getBlockState(blockPos);
         return s.getBlock() == Blocks.OAK_LEAVES || s.getBlock() == Blocks.OAK_LOG;
      }));
   }

   @Override
   protected Task onTick() {
      AltoClefController mod = this.controller;
      if (this.target != null) {
         return new GetToBlockTask(this.target);
      } else if (!mod.getBlockScanner().anyFound(Blocks.OAK_LOG)) {
         return new TimeoutWanderTask();
      } else {
         Optional<BlockPos> nearest = mod.getBlockScanner().getNearestBlock(Blocks.OAK_LOG);
         if (nearest.isPresent()) {
            BlockPos check = new BlockPos((Vec3i)nearest.get());

            while (mod.getWorld().getBlockState(check).getBlock() == Blocks.OAK_LOG || mod.getWorld().getBlockState(check).getBlock() == Blocks.OAK_LEAVES) {
               check = check.above();
            }

            this.target = check;
         }

         return null;
      }
   }

   @Override
   protected void onStop(Task interruptTask) {
      this.controller.getBehaviour().pop();
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof ExampleTask2;
   }

   @Override
   public boolean isFinished() {
      return this.target != null ? this.controller.getPlayer().blockPosition().equals(this.target) : super.isFinished();
   }

   @Override
   protected String toDebugString() {
      return "Standing on a tree";
   }
}
