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

package altoclef.chains;

import altoclef.AltoClefController;
import altoclef.tasks.entity.AbstractKillEntityTask;
import altoclef.tasksystem.TaskChain;
import altoclef.tasksystem.TaskRunner;
import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.movement.IMovement;
import baritone.pathing.movement.Movement;
import baritone.utils.BlockStateInterface;
import java.util.Optional;

public class PreEquipItemChain extends SingleTaskChain {
   public PreEquipItemChain(TaskRunner runner) {
      super(runner);
   }

   @Override
   protected void onTaskFinish(AltoClefController mod) {
   }

   @Override
   public float getPriority() {
      this.update(this.controller);
      return -1.0F;
   }

   private void update(AltoClefController mod) {
      if (!mod.getFoodChain().isTryingToEat()) {
         TaskChain currentChain = mod.getTaskRunner().getCurrentTaskChain();
         if (currentChain != null) {
            Optional<IPath> pathOptional = mod.getBaritone().getPathingBehavior().getPath();
            if (!pathOptional.isEmpty()) {
               IPath path = pathOptional.get();
               BlockStateInterface bsi = new BlockStateInterface(this.controller.getBaritone().getEntityContext());

               for (IMovement iMovement : path.movements()) {
                  Movement movement = (Movement)iMovement;
                  if (movement.toBreak(bsi).stream().anyMatch(pos -> mod.getWorld().getBlockState(pos).getBlock().defaultDestroyTime() > 0.0F)
                     || !movement.toPlace(bsi).isEmpty()) {
                     return;
                  }
               }

               if (currentChain.getTasks().stream().anyMatch(task -> task instanceof AbstractKillEntityTask)) {
                  AbstractKillEntityTask.equipWeapon(mod);
               }
            }
         }
      }
   }

   @Override
   public String getName() {
      return "pre-equip item chain";
   }

   @Override
   public boolean isActive() {
      return true;
   }
}
