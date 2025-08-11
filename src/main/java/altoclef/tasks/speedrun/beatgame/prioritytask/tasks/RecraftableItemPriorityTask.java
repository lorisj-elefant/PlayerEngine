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

package altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import altoclef.AltoClefController;
import altoclef.util.RecipeTarget;
import java.util.function.Function;

public class RecraftableItemPriorityTask extends CraftItemPriorityTask {
   private final double recraftPriority;

   public RecraftableItemPriorityTask(double priority, double recraftPriority, RecipeTarget toCraft, Function<AltoClefController, Boolean> canCall) {
      super(priority, toCraft, canCall);
      this.recraftPriority = recraftPriority;
   }

   @Override
   protected double getPriority(AltoClefController mod) {
      return this.isSatisfied() ? this.recraftPriority : super.getPriority(mod);
   }
}
