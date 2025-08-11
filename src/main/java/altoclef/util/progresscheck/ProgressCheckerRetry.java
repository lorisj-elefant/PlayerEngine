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

public class ProgressCheckerRetry<T> implements IProgressChecker<T> {
   private final IProgressChecker<T> subChecker;
   private final int allowedAttempts;
   private int failCount;

   public ProgressCheckerRetry(IProgressChecker<T> subChecker, int allowedAttempts) {
      this.subChecker = subChecker;
      this.allowedAttempts = allowedAttempts;
   }

   @Override
   public void setProgress(T progress) {
      this.subChecker.setProgress(progress);
      if (this.subChecker.failed()) {
         this.failCount++;
         this.subChecker.reset();
      }
   }

   @Override
   public boolean failed() {
      return this.failCount >= this.allowedAttempts;
   }

   @Override
   public void reset() {
      this.subChecker.reset();
      this.failCount = 0;
   }
}
