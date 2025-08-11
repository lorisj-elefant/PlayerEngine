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
import altoclef.tasks.ResourceTask;
import altoclef.tasks.construction.DestroyBlockTask;
import altoclef.tasks.movement.DefaultGoToDimensionTask;
import altoclef.tasks.movement.SearchChunkForBlockTask;
import altoclef.tasksystem.Task;
import altoclef.util.Dimension;
import altoclef.util.helpers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class GetSmithingTemplateTask extends ResourceTask {
   private final Task searcher = new SearchChunkForBlockTask(Blocks.BLACKSTONE);
   private final int count;
   private BlockPos chestloc = null;

   public GetSmithingTemplateTask(int count) {
      super(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, count);
      this.count = count;
   }

   @Override
   protected void onResourceStart(AltoClefController mod) {
   }

   @Override
   protected Task onResourceTick(AltoClefController mod) {
      if (WorldHelper.getCurrentDimension(mod) != Dimension.NETHER) {
         this.setDebugState("Going to nether");
         return new DefaultGoToDimensionTask(Dimension.NETHER);
      } else {
         if (this.chestloc == null) {
            for (BlockPos pos : mod.getBlockScanner().getKnownLocations(Blocks.CHEST)) {
               if (WorldHelper.isInteractableBlock(mod, pos)) {
                  this.chestloc = pos;
                  break;
               }
            }
         }

         if (this.chestloc != null) {
            this.setDebugState("Destroying Chest");
            if (WorldHelper.isInteractableBlock(mod, this.chestloc)) {
               return new DestroyBlockTask(this.chestloc);
            }

            this.chestloc = null;

            for (BlockPos posx : mod.getBlockScanner().getKnownLocations(Blocks.CHEST)) {
               if (WorldHelper.isInteractableBlock(mod, posx)) {
                  this.chestloc = posx;
                  break;
               }
            }
         }

         this.setDebugState("Searching for/Traveling around bastion");
         return this.searcher;
      }
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof GetSmithingTemplateTask;
   }

   @Override
   protected String toDebugStringName() {
      return "Collect " + this.count + " smithing templates";
   }

   @Override
   protected boolean shouldAvoidPickingUp(AltoClefController mod) {
      return false;
   }
}
