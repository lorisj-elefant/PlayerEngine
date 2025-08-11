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

package altoclef.tasks.misc;

import altoclef.tasks.construction.DestroyBlockTask;
import altoclef.tasks.container.LootContainerTask;
import altoclef.tasksystem.Task;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;

public class LootDesertTempleTask extends Task {
   public final Vec3i[] CHEST_POSITIONS_RELATIVE = new Vec3i[]{new Vec3i(2, 0, 0), new Vec3i(-2, 0, 0), new Vec3i(0, 0, 2), new Vec3i(0, 0, -2)};
   private final BlockPos temple;
   private final List<Item> wanted;
   private Task lootTask;
   private short looted = 0;

   public LootDesertTempleTask(BlockPos temple, List<Item> wanted) {
      this.temple = temple;
      this.wanted = wanted;
   }

   @Override
   protected void onStart() {
      this.controller.getBaritoneSettings().blocksToAvoid.get().add(Blocks.STONE_PRESSURE_PLATE);
   }

   @Override
   protected Task onTick() {
      if (this.lootTask != null) {
         if (!this.lootTask.isFinished()) {
            this.setDebugState("Looting a desert temple chest");
            return this.lootTask;
         }

         this.looted++;
      }

      if (this.controller.getWorld().getBlockState(this.temple).getBlock() == Blocks.STONE_PRESSURE_PLATE) {
         this.setDebugState("Breaking pressure plate");
         return new DestroyBlockTask(this.temple);
      } else if (this.looted < 4) {
         this.setDebugState("Looting a desert temple chest");
         this.lootTask = new LootContainerTask(this.temple.offset(this.CHEST_POSITIONS_RELATIVE[this.looted]), this.wanted);
         return this.lootTask;
      } else {
         this.setDebugState("Why is this still running? Report this");
         return null;
      }
   }

   @Override
   protected void onStop(Task task) {
      this.controller.getBaritoneSettings().blocksToAvoid.get().remove(Blocks.STONE_PRESSURE_PLATE);
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof LootDesertTempleTask && ((LootDesertTempleTask)other).getTemplePos() == this.temple;
   }

   @Override
   public boolean isFinished() {
      return this.looted == 4;
   }

   @Override
   protected String toDebugString() {
      return "Looting Desert Temple";
   }

   public BlockPos getTemplePos() {
      return this.temple;
   }
}
