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

package altoclef.tasks.movement;

import altoclef.AltoClefController;
import altoclef.tasks.construction.compound.ConstructNetherPortalBucketTask;
import altoclef.tasks.construction.compound.ConstructNetherPortalObsidianTask;
import altoclef.tasksystem.Task;
import altoclef.util.Dimension;
import altoclef.util.helpers.WorldHelper;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;

public class DefaultGoToDimensionTask extends Task {
   private final Dimension target;
   private final Task cachedNetherBucketConstructionTask = new ConstructNetherPortalBucketTask();

   public DefaultGoToDimensionTask(Dimension target) {
      this.target = target;
   }

   @Override
   protected void onStart() {
   }

   @Override
   protected Task onTick() {
      if (WorldHelper.getCurrentDimension(this.controller) == this.target) {
         return null;
      } else {
         label25:
         switch (this.target) {
            case NETHER:
               switch (WorldHelper.getCurrentDimension(this.controller)) {
                  case END:
                     return this.goToOverworldFromEndTask();
                  case OVERWORLD:
                     return this.goToNetherFromOverworldTask();
                  default:
                     break label25;
               }
            case END:
               switch (WorldHelper.getCurrentDimension(this.controller)) {
                  case NETHER:
                     return this.goToOverworldFromNetherTask();
                  case OVERWORLD:
                     return this.goToEndTask();
                  default:
                     break label25;
               }
            case OVERWORLD:
               switch (WorldHelper.getCurrentDimension(this.controller)) {
                  case NETHER:
                     return this.goToOverworldFromNetherTask();
                  case END:
                     return this.goToOverworldFromEndTask();
               }
         }

         this.setDebugState(WorldHelper.getCurrentDimension(this.controller) + " -> " + this.target + " is NOT IMPLEMENTED YET!");
         return null;
      }
   }

   @Override
   protected void onStop(Task interruptTask) {
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof DefaultGoToDimensionTask task ? task.target == this.target : false;
   }

   @Override
   protected String toDebugString() {
      return "Going to dimension: " + this.target + " (default version)";
   }

   @Override
   public boolean isFinished() {
      return WorldHelper.getCurrentDimension(this.controller) == this.target;
   }

   private Task goToOverworldFromNetherTask() {
      if (this.netherPortalIsClose(this.controller)) {
         this.setDebugState("Going to nether portal");
         return new EnterNetherPortalTask(Dimension.NETHER);
      } else {
         Optional<BlockPos> closest = this.controller.getMiscBlockTracker().getLastUsedNetherPortal(Dimension.NETHER);
         if (closest.isPresent()) {
            this.setDebugState("Going to last nether portal pos");
            return new GetToBlockTask(closest.get());
         } else {
            this.setDebugState("Constructing nether portal with obsidian");
            return new ConstructNetherPortalObsidianTask();
         }
      }
   }

   private Task goToOverworldFromEndTask() {
      this.setDebugState("TODO: Go to center portal (at 0,0). If it doesn't exist, kill ender dragon lol");
      return null;
   }

   private Task goToNetherFromOverworldTask() {
      if (this.netherPortalIsClose(this.controller)) {
         this.setDebugState("Going to nether portal");
         return new EnterNetherPortalTask(Dimension.NETHER);
      } else {
         return (Task)(switch (this.controller.getModSettings().getOverworldToNetherBehaviour()) {
            case BUILD_PORTAL_VANILLA -> this.cachedNetherBucketConstructionTask;
            case GO_TO_HOME_BASE -> new GetToBlockTask(this.controller.getModSettings().getHomeBasePosition());
         });
      }
   }

   private Task goToEndTask() {
      this.setDebugState("TODO: Get to End, Same as BeatMinecraft");
      return null;
   }

   private boolean netherPortalIsClose(AltoClefController mod) {
      if (!mod.getBlockScanner().anyFound(Blocks.NETHER_PORTAL)) {
         return false;
      } else {
         Optional<BlockPos> closest = mod.getBlockScanner().getNearestBlock(Blocks.NETHER_PORTAL);
         return closest.isPresent()
            && closest.get()
               .closerThan(new Vec3i((int)mod.getPlayer().position().x, (int)mod.getPlayer().position().y, (int)mod.getPlayer().position().z), 2000.0);
      }
   }

   public static enum OVERWORLD_TO_NETHER_BEHAVIOUR {
      BUILD_PORTAL_VANILLA,
      GO_TO_HOME_BASE;
   }
}
