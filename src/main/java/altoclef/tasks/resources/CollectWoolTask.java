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
import altoclef.tasks.entity.ShearSheepTask;
import altoclef.tasksystem.Task;
import altoclef.util.ItemTarget;
import altoclef.util.MiningRequirement;
import altoclef.util.helpers.ItemHelper;
import java.util.Arrays;
import java.util.HashSet;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CollectWoolTask extends ResourceTask {
   private final int count;
   private final HashSet<DyeColor> colors;
   private final Item[] wools;

   public CollectWoolTask(DyeColor[] colors, int count) {
      super(new ItemTarget(ItemHelper.WOOL, count));
      this.colors = new HashSet<>(Arrays.asList(colors));
      this.count = count;
      this.wools = getWoolColorItems(colors);
   }

   public CollectWoolTask(DyeColor color, int count) {
      this(new DyeColor[]{color}, count);
   }

   public CollectWoolTask(int count) {
      this(DyeColor.values(), count);
   }

   private static Item[] getWoolColorItems(DyeColor[] colors) {
      Item[] result = new Item[colors.length];

      for (int i = 0; i < result.length; i++) {
         result[i] = ItemHelper.getColorfulItems(colors[i]).wool;
      }

      return result;
   }

   @Override
   protected boolean shouldAvoidPickingUp(AltoClefController mod) {
      return false;
   }

   @Override
   protected void onResourceStart(AltoClefController mod) {
   }

   @Override
   protected Task onResourceTick(AltoClefController mod) {
      Block[] woolBlocks = ItemHelper.itemsToBlocks(this.wools);
      if (mod.getBlockScanner().anyFound(woolBlocks)) {
         return new MineAndCollectTask(new ItemTarget(this.wools), woolBlocks, MiningRequirement.HAND);
      } else if (this.isInWrongDimension(mod) && !mod.getEntityTracker().entityFound(Sheep.class)) {
         return this.getToCorrectDimensionTask(mod);
      } else {
         return (Task)(mod.getItemStorage().hasItem(Items.SHEARS)
            ? new ShearSheepTask()
            : new KillAndLootTask(
               Sheep.class,
               entity -> !(entity instanceof Sheep sheep) ? false : this.colors.contains(sheep.getColor()) && !sheep.isSheared(),
               new ItemTarget(this.wools, this.count)
            ));
      }
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectWoolTask && ((CollectWoolTask)other).count == this.count;
   }

   @Override
   protected String toDebugStringName() {
      return "Collect " + this.count + " wool.";
   }
}
