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
import altoclef.tasksystem.Task;
import altoclef.util.MiningRequirement;
import java.util.Arrays;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CollectBlockByOneTask extends ResourceTask {
   private final Item item;
   private final Block[] blocks;
   private final MiningRequirement requirement;
   private final int count;

   public CollectBlockByOneTask(Item item, Block[] blocks, MiningRequirement requirement, int targetCount) {
      super(item, targetCount);
      this.item = item;
      this.blocks = blocks;
      this.requirement = requirement;
      this.count = targetCount;
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
      return new MineAndCollectTask(this.item, this.count, this.blocks, this.requirement);
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return !(other instanceof CollectBlockByOneTask task)
         ? false
         : task.count == this.count
            && task.item.equals(this.item)
            && Arrays.stream(task.blocks).allMatch(block -> Arrays.stream(this.blocks).toList().contains(block));
   }

   @Override
   protected String toDebugStringName() {
      return "Collect " + this.item;
   }

   public static class CollectCobbledDeepslateTask extends CollectBlockByOneTask {
      public CollectCobbledDeepslateTask(int targetCount) {
         super(Items.COBBLED_DEEPSLATE, new Block[]{Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE}, MiningRequirement.WOOD, targetCount);
      }
   }

   public static class CollectCobblestoneTask extends CollectBlockByOneTask {
      public CollectCobblestoneTask(int targetCount) {
         super(Items.COBBLESTONE, new Block[]{Blocks.STONE, Blocks.COBBLESTONE}, MiningRequirement.WOOD, targetCount);
      }
   }

   public static class CollectEndStoneTask extends CollectBlockByOneTask {
      public CollectEndStoneTask(int targetCount) {
         super(Items.END_STONE, new Block[]{Blocks.END_STONE}, MiningRequirement.WOOD, targetCount);
      }
   }
}
