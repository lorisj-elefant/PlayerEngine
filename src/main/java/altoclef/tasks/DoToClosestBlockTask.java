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

package altoclef.tasks;

import altoclef.AltoClefController;
import altoclef.tasksystem.Task;
import altoclef.util.helpers.WorldHelper;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class DoToClosestBlockTask extends AbstractDoToClosestObjectTask<BlockPos> {
   private final Block[] targetBlocks;
   private final Supplier<Vec3> getOriginPos;
   private final Function<Vec3, Optional<BlockPos>> getClosest;
   private final Function<BlockPos, Task> getTargetTask;
   private final Predicate<BlockPos> isValid;

   public DoToClosestBlockTask(
      Supplier<Vec3> getOriginSupplier,
      Function<BlockPos, Task> getTargetTask,
      Function<Vec3, Optional<BlockPos>> getClosestBlock,
      Predicate<BlockPos> isValid,
      Block... blocks
   ) {
      this.getOriginPos = getOriginSupplier;
      this.getTargetTask = getTargetTask;
      this.getClosest = getClosestBlock;
      this.isValid = isValid;
      this.targetBlocks = blocks;
   }

   public DoToClosestBlockTask(
      Function<BlockPos, Task> getTargetTask, Function<Vec3, Optional<BlockPos>> getClosestBlock, Predicate<BlockPos> isValid, Block... blocks
   ) {
      this((Supplier<Vec3>)null, getTargetTask, getClosestBlock, isValid, blocks);
   }

   public DoToClosestBlockTask(Function<BlockPos, Task> getTargetTask, Predicate<BlockPos> isValid, Block... blocks) {
      this((Supplier<Vec3>)null, getTargetTask, (Function<Vec3, Optional<BlockPos>>)null, isValid, blocks);
   }

   public DoToClosestBlockTask(Function<BlockPos, Task> getTargetTask, Block... blocks) {
      this(getTargetTask, (Function<Vec3, Optional<BlockPos>>)null, blockPos -> true, blocks);
   }

   protected Vec3 getPos(AltoClefController mod, BlockPos obj) {
      return WorldHelper.toVec3d(obj);
   }

   @Override
   protected Optional<BlockPos> getClosestTo(AltoClefController mod, Vec3 pos) {
      return this.getClosest != null ? this.getClosest.apply(pos) : mod.getBlockScanner().getNearestBlock(pos, this.isValid, this.targetBlocks);
   }

   @Override
   protected Vec3 getOriginPos(AltoClefController mod) {
      return this.getOriginPos != null ? this.getOriginPos.get() : mod.getPlayer().position();
   }

   protected Task getGoalTask(BlockPos obj) {
      return this.getTargetTask.apply(obj);
   }

   protected boolean isValid(AltoClefController mod, BlockPos obj) {
      if (!mod.getChunkTracker().isChunkLoaded(obj)) {
         return true;
      } else {
         return this.isValid != null && !this.isValid.test(obj) ? false : mod.getBlockScanner().isBlockAtPosition(obj, this.targetBlocks);
      }
   }

   @Override
   protected void onStart() {
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof DoToClosestBlockTask task ? Arrays.equals((Object[])task.targetBlocks, (Object[])this.targetBlocks) : false;
   }

   @Override
   protected String toDebugString() {
      return "Doing something to closest block...";
   }
}
