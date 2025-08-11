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

package altoclef.util.progresscheck;

import altoclef.AltoClefController;
import altoclef.util.helpers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class MovementProgressChecker {
   private final IProgressChecker<Vec3> distanceChecker;
   private final IProgressChecker<Double> mineChecker;
   public BlockPos lastBreakingBlock = null;

   public MovementProgressChecker(double distanceTimeout, double minDistance, double mineTimeout, double minMineProgress, int attempts) {
      this.distanceChecker = new ProgressCheckerRetry<>(new DistanceProgressChecker(distanceTimeout, minDistance), attempts);
      this.mineChecker = new LinearProgressChecker(mineTimeout, minMineProgress);
   }

   public MovementProgressChecker(double distanceTimeout, double minDistance, double mineTimeout, double minMineProgress) {
      this(distanceTimeout, minDistance, mineTimeout, minMineProgress, 1);
   }

   public MovementProgressChecker(int attempts) {
      this(6.0, 0.1, 10.0, 0.001, attempts);
   }

   public MovementProgressChecker() {
      this(1);
   }

   public boolean check(AltoClefController mod) {
      if (mod.getFoodChain().needsToEat()) {
         this.distanceChecker.reset();
         this.mineChecker.reset();
      }

      if (mod.getControllerExtras().isBreakingBlock()) {
         BlockPos breakBlock = mod.getControllerExtras().getBreakingBlockPos();
         if (this.lastBreakingBlock != null && WorldHelper.isAir(mod.getWorld().getBlockState(this.lastBreakingBlock).getBlock())) {
            this.distanceChecker.reset();
            this.mineChecker.reset();
         }

         this.lastBreakingBlock = breakBlock;
         this.mineChecker.setProgress(0.0);
         return !this.mineChecker.failed();
      } else {
         this.mineChecker.reset();
         this.distanceChecker.setProgress(mod.getPlayer().position());
         return !this.distanceChecker.failed();
      }
   }

   public void reset() {
      this.distanceChecker.reset();
      this.mineChecker.reset();
   }
}
