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
import altoclef.Debug;
import altoclef.tasks.CraftInInventoryTask;
import altoclef.tasks.container.CraftInTableTask;
import altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;
import altoclef.tasksystem.Task;
import altoclef.util.RecipeTarget;
import altoclef.util.helpers.CraftingHelper;
import java.util.function.Function;

public class CraftItemPriorityTask extends PriorityTask {
   public final double priority;
   public final RecipeTarget recipeTarget;
   private boolean satisfied = false;

   public CraftItemPriorityTask(double priority, RecipeTarget toCraft) {
      this(priority, toCraft, mod -> true);
   }

   public CraftItemPriorityTask(double priority, RecipeTarget toCraft, Function<AltoClefController, Boolean> canCall) {
      this(priority, toCraft, canCall, false, true, true);
   }

   public CraftItemPriorityTask(double priority, RecipeTarget toCraft, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
      this(priority, toCraft, mod -> true, shouldForce, canCache, bypassForceCooldown);
   }

   public CraftItemPriorityTask(
      double priority, RecipeTarget toCraft, Function<AltoClefController, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown
   ) {
      super(canCall, shouldForce, canCache, bypassForceCooldown);
      this.priority = priority;
      this.recipeTarget = toCraft;
   }

   @Override
   public Task getTask(AltoClefController mod) {
      return (Task)(this.recipeTarget.getRecipe().isBig() ? new CraftInTableTask(this.recipeTarget) : new CraftInInventoryTask(this.recipeTarget));
   }

   @Override
   public String getDebugString() {
      return "Crafting " + this.recipeTarget;
   }

   @Override
   protected double getPriority(AltoClefController mod) {
      if (BeatMinecraftTask.hasItem(mod, this.recipeTarget.getOutputItem())) {
         Debug.logInternal("THIS IS SATISFIED " + this.recipeTarget.getOutputItem());
         this.satisfied = true;
      }

      Debug.logInternal("NOT SATISFIED");
      return this.satisfied ? Double.NEGATIVE_INFINITY : this.priority;
   }

   @Override
   public boolean needCraftingOnStart(AltoClefController mod) {
      return CraftingHelper.canCraftItemNow(mod, this.recipeTarget.getOutputItem());
   }

   public boolean isSatisfied() {
      return this.satisfied;
   }
}
