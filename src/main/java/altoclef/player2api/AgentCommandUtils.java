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

package altoclef.player2api;

import altoclef.AltoClefController;
import altoclef.util.ItemTarget;
import java.util.ArrayList;
import java.util.List;

public class AgentCommandUtils {
   public static ItemTarget[] addPresentItemsToTargets(AltoClefController mod, ItemTarget[] items) {
      List<ItemTarget> resultTargets = new ArrayList<>();

      for (ItemTarget target : items) {
         int count = target.getTargetCount();
         count += mod.getItemStorage().getItemCountInventoryOnly(target.getMatches());
         resultTargets.add(new ItemTarget(target, count));
      }

      return resultTargets.toArray(new ItemTarget[0]);
   }
}
