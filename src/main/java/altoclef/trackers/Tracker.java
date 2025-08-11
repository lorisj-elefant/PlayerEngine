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

package altoclef.trackers;

import altoclef.AltoClefController;

public abstract class Tracker {
   protected AltoClefController mod;
   private boolean dirty = true;

   public Tracker(TrackerManager manager) {
      manager.addTracker(this);
   }

   public void setDirty() {
      this.dirty = true;
   }

   protected boolean isDirty() {
      return this.dirty;
   }

   public void ensureUpdated() {
      if (this.isDirty()) {
         this.updateState();
         this.dirty = false;
      }
   }

   protected abstract void updateState();

   protected abstract void reset();
}
