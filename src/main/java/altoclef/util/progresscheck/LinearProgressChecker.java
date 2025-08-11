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

import altoclef.util.time.TimerGame;

public class LinearProgressChecker implements IProgressChecker<Double> {
   private final double minProgress;
   private final TimerGame timer;
   private double lastProgress;
   private double currentProgress;
   private boolean first;
   private boolean failed;

   public LinearProgressChecker(double timeout, double minProgress) {
      this.minProgress = minProgress;
      this.timer = new TimerGame(timeout);
      this.reset();
   }

   public void setProgress(Double progress) {
      this.currentProgress = progress;
      if (this.first) {
         this.lastProgress = progress;
         this.first = false;
      }

      if (this.timer.elapsed()) {
         double improvement = progress - this.lastProgress;
         if (improvement < this.minProgress) {
            this.failed = true;
         }

         this.first = false;
         this.timer.reset();
         this.lastProgress = progress;
      }
   }

   @Override
   public boolean failed() {
      return this.failed;
   }

   @Override
   public void reset() {
      this.failed = false;
      this.timer.reset();
      this.first = true;
   }
}
