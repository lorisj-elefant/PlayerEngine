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
import altoclef.chains.MobDefenseChain;
import altoclef.tasksystem.Task;
import altoclef.util.baritone.GoalRunAwayFromEntities;
import baritone.api.pathing.goals.Goal;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;

public class RunAwayFromCreepersTask extends CustomBaritoneGoalTask {
   private final double distanceToRun;

   public RunAwayFromCreepersTask(double distance) {
      this.distanceToRun = distance;
   }

   @Override
   protected boolean isEqual(Task other) {
      return other instanceof RunAwayFromCreepersTask task ? !(Math.abs(task.distanceToRun - this.distanceToRun) > 1.0) : false;
   }

   @Override
   protected String toDebugString() {
      return "Run " + this.distanceToRun + " blocks away from creepers";
   }

   @Override
   protected Goal newGoal(AltoClefController mod) {
      mod.getBaritone().getPathingBehavior().forceCancel();
      return new RunAwayFromCreepersTask.GoalRunAwayFromCreepers(mod, this.distanceToRun);
   }

   private static class GoalRunAwayFromCreepers extends GoalRunAwayFromEntities {
      public GoalRunAwayFromCreepers(AltoClefController mod, double distance) {
         super(mod, distance, false, 10.0);
      }

      @Override
      protected List<Entity> getEntities(AltoClefController mod) {
         return new ArrayList<>(mod.getEntityTracker().getTrackedEntities(Creeper.class));
      }

      @Override
      protected double getCostOfEntity(Entity entity, int x, int y, int z) {
         return entity instanceof Creeper
            ? MobDefenseChain.getCreeperSafety(new Vec3(x + 0.5, y + 0.5, z + 0.5), (Creeper)entity)
            : super.getCostOfEntity(entity, x, y, z);
      }
   }
}
