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
import altoclef.BotBehaviour;
import altoclef.TaskCatalogue;
import altoclef.tasksystem.Task;
import altoclef.util.ItemTarget;
import altoclef.util.MiningRequirement;
import altoclef.util.helpers.ItemHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class ShearAndCollectBlockTask extends MineAndCollectTask {
   public ShearAndCollectBlockTask(ItemTarget[] itemTargets, Block... blocksToMine) {
      super(itemTargets, blocksToMine, MiningRequirement.HAND);
   }

   public ShearAndCollectBlockTask(Item[] items, int count, Block... blocksToMine) {
      this(new ItemTarget[]{new ItemTarget(items, count)}, blocksToMine);
   }

   public ShearAndCollectBlockTask(Item item, int count, Block... blocksToMine) {
      this(new Item[]{item}, count, blocksToMine);
   }

   @Override
   protected void onStart() {
      BotBehaviour botBehaviour = this.controller.getBehaviour();
      botBehaviour.push();
      botBehaviour.forceUseTool((blockState, itemStack) -> itemStack.getItem() == Items.SHEARS && ItemHelper.areShearsEffective(blockState.getBlock()));
      super.onStart();
   }

   @Override
   protected void onStop(Task interruptTask) {
      this.controller.getBehaviour().pop();
      super.onStop(interruptTask);
   }

   @Override
   protected Task onResourceTick(AltoClefController mod) {
      return (Task)(!mod.getItemStorage().hasItem(Items.SHEARS) ? TaskCatalogue.getItemTask(Items.SHEARS, 1) : super.onResourceTick(mod));
   }
}
