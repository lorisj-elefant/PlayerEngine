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
import altoclef.tasks.container.UpgradeInSmithingTableTask;
import altoclef.util.ItemTarget;
import java.util.ArrayList;
import java.util.List;

public class SmithingSquasher extends TypeSquasher<UpgradeInSmithingTableTask> {
   @Override
   protected List<ResourceTask> getSquashed(List<UpgradeInSmithingTableTask> tasks) {
      if (tasks.isEmpty()) {
         return new ArrayList<>();
      } else {
         List<ResourceTask> result = new ArrayList<>();
         List<ItemTarget> materialsToCollect = new ArrayList<>();

         for (UpgradeInSmithingTableTask task : tasks) {
            materialsToCollect.add(task.getMaterials());
            materialsToCollect.add(task.getTools());
            materialsToCollect.add(task.getTemplate());
         }

         if (!materialsToCollect.isEmpty()) {
            result.add(new CataloguedResourceTask(materialsToCollect.toArray(new ItemTarget[0])));
         }

         result.addAll(tasks);
         return result;
      }
   }
}
