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

package altoclef.tasks.resources;

import altoclef.AltoClefController;
import altoclef.Debug;
import altoclef.TaskCatalogue;
import altoclef.tasks.CraftInInventoryTask;
import altoclef.tasks.ResourceTask;
import altoclef.tasks.container.CraftInTableTask;
import altoclef.tasksystem.Task;
import altoclef.util.CraftingRecipe;
import altoclef.util.ItemTarget;
import altoclef.util.RecipeTarget;
import net.minecraft.world.item.Item;

public abstract class CraftWithMatchingMaterialsTask extends ResourceTask {
   private final ItemTarget target;
   private final CraftingRecipe recipe;
   private final boolean[] sameMask;
   private final ItemTarget sameResourceTarget;
   private final int sameResourceRequiredCount;
   private final int sameResourcePerRecipe;

   public CraftWithMatchingMaterialsTask(ItemTarget target, CraftingRecipe recipe, boolean[] sameMask) {
      super(target);
      this.target = target;
      this.recipe = recipe;
      this.sameMask = sameMask;
      int sameResourceRequiredCount = 0;
      ItemTarget sameResourceTarget = null;
      if (recipe.getSlotCount() != sameMask.length) {
         Debug.logError("Invalid CraftWithMatchingMaterialsTask constructor parameters: Recipe size must equal \"sameMask\" size.");
      }

      for (int i = 0; i < recipe.getSlotCount(); i++) {
         if (sameMask[i]) {
            sameResourceRequiredCount++;
            sameResourceTarget = recipe.getSlot(i);
         }
      }

      this.sameResourceTarget = sameResourceTarget;
      int craftsNeeded = (int)(1.0 + Math.floor(target.getTargetCount() / recipe.outputCount() - 0.001));
      this.sameResourcePerRecipe = sameResourceRequiredCount;
      this.sameResourceRequiredCount = sameResourceRequiredCount * craftsNeeded;
   }

   private static CraftingRecipe generateSameRecipe(CraftingRecipe diverseRecipe, Item sameItem, boolean[] sameMask) {
      ItemTarget[] result = new ItemTarget[diverseRecipe.getSlotCount()];

      for (int i = 0; i < result.length; i++) {
         if (sameMask[i]) {
            result[i] = new ItemTarget(sameItem, 1);
         } else {
            result[i] = diverseRecipe.getSlot(i);
         }
      }

      return CraftingRecipe.newShapedRecipe(result, diverseRecipe.outputCount());
   }

   @Override
   protected void onResourceStart(AltoClefController mod) {
   }

   @Override
   protected Task onResourceTick(AltoClefController mod) {
      int canCraftTotal = 0;
      int majorityCraftCount = 0;
      Item majorityCraftItem = null;

      for (Item sameCheck : this.sameResourceTarget.getMatches()) {
         int count = this.getExpectedTotalCountOfSameItem(mod, sameCheck);
         int canCraft = count / this.sameResourcePerRecipe * this.recipe.outputCount();
         canCraftTotal += canCraft;
         if (canCraft > majorityCraftCount) {
            majorityCraftCount = canCraft;
            majorityCraftItem = sameCheck;
         }
      }

      int currentTargetCount = mod.getItemStorage().getItemCount(this.target);
      int currentTargetsRequired = this.target.getTargetCount() - currentTargetCount;
      if (canCraftTotal < currentTargetsRequired) {
         return this.getAllSameResourcesTask(mod);
      } else {
         int trueCanCraftTotal = 0;

         for (Item sameCheckx : this.sameResourceTarget.getMatches()) {
            int trueCount = mod.getItemStorage().getItemCount(sameCheckx);
            int trueCanCraft = trueCount / this.sameResourcePerRecipe * this.recipe.outputCount();
            trueCanCraftTotal += trueCanCraft;
         }

         if (trueCanCraftTotal < currentTargetsRequired) {
            return this.getSpecificSameResourceTask(mod, this.sameResourceTarget.getMatches());
         } else {
            CraftingRecipe sameRecipe = generateSameRecipe(this.recipe, majorityCraftItem, this.sameMask);
            int var20 = Math.min(majorityCraftCount, this.target.getTargetCount());
            Item output = this.getSpecificItemCorrespondingToMajorityResource(majorityCraftItem);
            var20 = Math.min(this.target.getTargetCount(), var20 + mod.getItemStorage().getItemCount(output));
            RecipeTarget recipeTarget = new RecipeTarget(output, var20, sameRecipe);
            return (Task)(this.recipe.isBig() ? new CraftInTableTask(recipeTarget) : new CraftInInventoryTask(recipeTarget));
         }
      }
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   protected Task getAllSameResourcesTask(AltoClefController mod) {
      ItemTarget infinityVersion = new ItemTarget(this.sameResourceTarget, 999999);
      return TaskCatalogue.getItemTask(infinityVersion);
   }

   protected int getExpectedTotalCountOfSameItem(AltoClefController mod, Item sameItem) {
      return mod.getItemStorage().getItemCount(sameItem);
   }

   protected Task getSpecificSameResourceTask(AltoClefController mod, Item[] toGet) {
      Debug.logError("Uh oh!!! getSpecificSameResourceTask should be implemented!!!! Now we're stuck.");
      return null;
   }

   protected abstract Item getSpecificItemCorrespondingToMajorityResource(Item var1);
}
