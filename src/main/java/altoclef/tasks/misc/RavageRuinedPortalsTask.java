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

import altoclef.AltoClefController;
import altoclef.tasks.container.LootContainerTask;
import altoclef.tasks.movement.TimeoutWanderTask;
import altoclef.tasksystem.Task;
import altoclef.util.Dimension;
import altoclef.util.helpers.WorldHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class RavageRuinedPortalsTask extends Task {
   public final Item[] LOOT = new Item[]{
      Items.IRON_NUGGET,
      Items.FLINT,
      Items.OBSIDIAN,
      Items.FIRE_CHARGE,
      Items.FLINT_AND_STEEL,
      Items.GOLD_NUGGET,
      Items.GOLDEN_APPLE,
      Items.GOLDEN_AXE,
      Items.GOLDEN_HOE,
      Items.GOLDEN_PICKAXE,
      Items.GOLDEN_SHOVEL,
      Items.GOLDEN_SWORD,
      Items.GOLDEN_HELMET,
      Items.GOLDEN_CHESTPLATE,
      Items.GOLDEN_LEGGINGS,
      Items.GOLDEN_BOOTS,
      Items.GLISTERING_MELON_SLICE,
      Items.GOLDEN_CARROT,
      Items.GOLD_INGOT,
      Items.CLOCK,
      Items.LIGHT_WEIGHTED_PRESSURE_PLATE,
      Items.GOLDEN_HORSE_ARMOR,
      Items.GOLD_BLOCK,
      Items.BELL,
      Items.ENCHANTED_GOLDEN_APPLE
   };
   private List<BlockPos> notRuinedPortalChests = new ArrayList<>();
   private Task lootTask;

   @Override
   protected void onStart() {
      this.controller.getBehaviour().push();
   }

   @Override
   protected Task onTick() {
      if (this.lootTask != null && this.lootTask.isActive() && !this.lootTask.isFinished()) {
         return this.lootTask;
      } else {
         Optional<BlockPos> closest = this.locateClosestUnopenedRuinedPortalChest(this.controller);
         if (closest.isPresent()) {
            this.lootTask = new LootContainerTask(closest.get(), List.of(this.LOOT));
            return this.lootTask;
         } else {
            return new TimeoutWanderTask();
         }
      }
   }

   @Override
   protected void onStop(Task task) {
      this.controller.getBehaviour().pop();
   }

   @Override
   protected boolean isEqual(Task task) {
      return task instanceof RavageRuinedPortalsTask;
   }

   @Override
   public boolean isFinished() {
      return false;
   }

   @Override
   protected String toDebugString() {
      return "Ravaging Ruined Portals";
   }

   private boolean canBeLootablePortalChest(AltoClefController mod, BlockPos blockPos) {
      if (mod.getWorld().getBlockState(blockPos.above(1)).getBlock() != Blocks.WATER && blockPos.getY() >= 50) {
         for (BlockPos check : WorldHelper.scanRegion(blockPos.offset(-4, -2, -4), blockPos.offset(4, 2, 4))) {
            if (mod.getWorld().getBlockState(check).getBlock() == Blocks.NETHERRACK) {
               return true;
            }
         }

         this.notRuinedPortalChests.add(blockPos);
         return false;
      } else {
         return false;
      }
   }

   private Optional<BlockPos> locateClosestUnopenedRuinedPortalChest(AltoClefController mod) {
      return WorldHelper.getCurrentDimension(this.controller) != Dimension.OVERWORLD
         ? Optional.empty()
         : mod.getBlockScanner()
            .getNearestBlock(
               (Predicate<BlockPos>)(blockPos -> !this.notRuinedPortalChests.contains(blockPos)
                  && WorldHelper.isUnopenedChest(this.controller, blockPos)
                  && this.canBeLootablePortalChest(mod, blockPos)),
               Blocks.CHEST
            );
   }
}
