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

package altoclef.tasks.squashed;

import altoclef.tasks.ResourceTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TypeSquasher<T extends ResourceTask> {
   private final List<T> tasks = new ArrayList<>();

   void add(T task) {
      this.tasks.add(task);
   }

   public List<ResourceTask> getSquashed() {
      return this.tasks.isEmpty() ? Collections.emptyList() : this.getSquashed(this.tasks);
   }

   protected abstract List<ResourceTask> getSquashed(List<T> var1);
}
