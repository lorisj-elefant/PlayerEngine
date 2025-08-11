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
import altoclef.multiversion.blockpos.BlockPosVer;
import altoclef.tasks.ResourceTask;
import altoclef.tasks.construction.PutOutFireTask;
import altoclef.tasks.entity.KillEntitiesTask;
import altoclef.tasks.movement.DefaultGoToDimensionTask;
import altoclef.tasks.movement.GetToBlockTask;
import altoclef.tasks.movement.RunAwayFromHostilesTask;
import altoclef.tasks.movement.SearchChunkForBlockTask;
import altoclef.tasksystem.Task;
import altoclef.util.Dimension;
import altoclef.util.helpers.WorldHelper;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CollectBlazeRodsTask extends ResourceTask {
   private static final double SPAWNER_BLAZE_RADIUS = 32.0;
   private static final double TOO_LITTLE_HEALTH_BLAZE = 10.0;
   private static final int TOO_MANY_BLAZES = 5;
   private final int count;
   private final Task searcher = new SearchChunkForBlockTask(Blocks.NETHER_BRICKS);
   private BlockPos foundBlazeSpawner = null;

   public CollectBlazeRodsTask(int count) {
      super(Items.BLAZE_ROD, count);
      this.count = count;
   }

   private static boolean isHoveringAboveLavaOrTooHigh(AltoClefController mod, Entity entity) {
      int MAX_HEIGHT = 11;

      for (BlockPos check = entity.blockPosition(); entity.blockPosition().getY() - check.getY() < MAX_HEIGHT; check = check.below()) {
         if (mod.getWorld().getBlockState(check).getBlock() == Blocks.LAVA) {
            return true;
         }

         if (WorldHelper.isSolidBlock(mod, check)) {
            return false;
         }
      }

      return true;
   }

   @Override
   protected void onResourceStart(AltoClefController mod) {
   }

   @Override
   protected Task onResourceTick(AltoClefController mod) {
      if (WorldHelper.getCurrentDimension(this.controller) != Dimension.NETHER) {
         this.setDebugState("Going to nether");
         return new DefaultGoToDimensionTask(Dimension.NETHER);
      } else {
         Optional<Entity> toKill = Optional.empty();
         if (mod.getEntityTracker().entityFound(Blaze.class)) {
            toKill = mod.getEntityTracker().getClosestEntity(Blaze.class);
            if (toKill.isPresent() && mod.getPlayer().getHealth() <= 10.0 && mod.getEntityTracker().getTrackedEntities(Blaze.class).size() >= 5) {
               this.setDebugState("Running away as there are too many blazes nearby.");
               return new RunAwayFromHostilesTask(30.0, true);
            }

            if (this.foundBlazeSpawner != null && toKill.isPresent()) {
               Entity kill = toKill.get();
               Vec3 nearest = kill.position();
               double sqDistanceToPlayer = nearest.distanceToSqr(mod.getPlayer().position());
               if (sqDistanceToPlayer > 1024.0) {
                  BlockHitResult hit = mod.getWorld()
                     .clip(new ClipContext(mod.getPlayer().getEyePosition(1.0F), kill.getEyePosition(1.0F), Block.OUTLINE, Fluid.NONE, mod.getPlayer()));
                  if (hit != null && BlockPosVer.getSquaredDistance(hit.getBlockPos(), mod.getPlayer().position()) < sqDistanceToPlayer) {
                     toKill = Optional.empty();
                  }
               }
            }
         }

         if (toKill.isPresent() && toKill.get().isAlive() && !isHoveringAboveLavaOrTooHigh(mod, toKill.get())) {
            this.setDebugState("Killing blaze");
            Predicate<Entity> safeToPursue = entity -> !isHoveringAboveLavaOrTooHigh(mod, entity);
            return new KillEntitiesTask(safeToPursue, toKill.get().getClass());
         } else {
            if (this.foundBlazeSpawner != null
               && mod.getChunkTracker().isChunkLoaded(this.foundBlazeSpawner)
               && !this.isValidBlazeSpawner(mod, this.foundBlazeSpawner)) {
               Debug.logMessage("Blaze spawner at " + this.foundBlazeSpawner + " too far away or invalid. Re-searching.");
               this.foundBlazeSpawner = null;
            }

            if (this.foundBlazeSpawner != null) {
               if (!this.foundBlazeSpawner.closerToCenterThan(mod.getPlayer().position(), 4.0)) {
                  this.setDebugState("Going to blaze spawner");
                  return new GetToBlockTask(this.foundBlazeSpawner.above(), false);
               } else {
                  Optional<BlockPos> nearestFire = mod.getBlockScanner().getNearestWithinRange(this.foundBlazeSpawner, 5.0, Blocks.FIRE);
                  if (nearestFire.isPresent()) {
                     this.setDebugState("Clearing fire around spawner to prevent loss of blaze rods.");
                     return new PutOutFireTask(nearestFire.get());
                  } else {
                     this.setDebugState("Waiting near blaze spawner for blazes to spawn");
                     return null;
                  }
               }
            } else {
               Optional<BlockPos> pos = mod.getBlockScanner()
                  .getNearestBlock((Predicate<BlockPos>)(blockPos -> this.isValidBlazeSpawner(mod, blockPos)), Blocks.SPAWNER);
               pos.ifPresent(blockPos -> this.foundBlazeSpawner = blockPos);
               this.setDebugState("Searching for fortress/Traveling around fortress");
               return this.searcher;
            }
         }
      }
   }

   private boolean isValidBlazeSpawner(AltoClefController mod, BlockPos pos) {
      return !mod.getChunkTracker().isChunkLoaded(pos) ? false : WorldHelper.getSpawnerEntity(mod, pos) instanceof Blaze;
   }

   @Override
   protected void onResourceStop(AltoClefController mod, Task interruptTask) {
   }

   @Override
   protected boolean isEqualResource(ResourceTask other) {
      return other instanceof CollectBlazeRodsTask;
   }

   @Override
   protected String toDebugStringName() {
      return "Collect blaze rods - " + this.controller.getItemStorage().getItemCount(Items.BLAZE_ROD) + "/" + this.count;
   }

   @Override
   protected boolean shouldAvoidPickingUp(AltoClefController mod) {
      return false;
   }
}
