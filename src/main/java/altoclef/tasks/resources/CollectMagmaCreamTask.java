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
import altoclef.TaskCatalogue;
import altoclef.tasks.ResourceTask;
import altoclef.tasks.movement.DefaultGoToDimensionTask;
import altoclef.tasksystem.Task;
import altoclef.util.Dimension;
import altoclef.util.ItemTarget;
import altoclef.util.helpers.WorldHelper;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class CollectMagmaCreamTask extends ResourceTask {
   private final int count;

   public CollectMagmaCreamTask(int count) {
      super(Items.MAGMA_CREAM, count);
      this.count = count;
   }

   @Override
   protected boolean shouldAvoidPickingUp(AltoClefController mod) {
      return false;
   }

   @Override
   protected void onResourceStart(AltoClefController mod) {
   }

    protected Task onResourceTick(AltoClefController mod) {
        int currentBlazePowderPotential, currentSlime, currentCream = mod.getItemStorage().getItemCount(new Item[]{Items.MAGMA_CREAM});
        int neededCream = this.count - currentCream;
        switch (WorldHelper.getCurrentDimension(controller).ordinal()) {
            case 1:
                if (mod.getEntityTracker().entityFound(MagmaCube.class)) {
                    setDebugState("Killing Magma cube");
                    return (Task) new KillAndLootTask(MagmaCube.class, new ItemTarget[]{new ItemTarget(Items.MAGMA_CREAM)});
                }
                currentBlazePowderPotential = mod.getItemStorage().getItemCount(new Item[]{Items.BLAZE_POWDER}) + mod.getItemStorage().getItemCount(new Item[]{Items.BLAZE_ROD});
                if (neededCream > currentBlazePowderPotential) {
                    setDebugState("Getting blaze powder");
                    return (Task) TaskCatalogue.getItemTask(Items.BLAZE_POWDER, neededCream - currentCream);
                }
                setDebugState("Going back to overworld to kill slimes, we have enough blaze powder and no nearby magma cubes.");
                return (Task) new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            case 2:
                currentSlime = mod.getItemStorage().getItemCount(new Item[]{Items.SLIME_BALL});
                if (neededCream > currentSlime) {
                    setDebugState("Getting slime balls");
                    return (Task) TaskCatalogue.getItemTask(Items.SLIME_BALL, neededCream - currentCream);
                }
                setDebugState("Going to nether to get blaze powder and/or kill magma cubes");
                return (Task) new DefaultGoToDimensionTask(Dimension.NETHER);
            case 3:
                setDebugState("Going to overworld, no magma cream materials exist here.");
                return (Task) new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }
        setDebugState("INVALID DIMENSION??: " + String.valueOf(WorldHelper.getCurrentDimension(controller)));
        return null;
    }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectMagmaCreamTask;
   }

   @Override
   protected String toDebugStringName() {
      return "Collecting " + this.count + " Magma cream.";
   }
}
