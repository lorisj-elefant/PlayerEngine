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
import altoclef.util.baritone.GoalRunAwayFromEntities;
import baritone.api.pathing.goals.Goal;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;

public abstract class RunAwayFromEntitiesTask extends CustomBaritoneGoalTask {
   private final Supplier<List<Entity>> runAwaySupplier;
   private final double distanceToRun;
   private final boolean xz;
   private final double penalty;

   public RunAwayFromEntitiesTask(Supplier<List<Entity>> toRunAwayFrom, double distanceToRun, boolean xz, double penalty) {
      this.runAwaySupplier = toRunAwayFrom;
      this.distanceToRun = distanceToRun;
      this.xz = xz;
      this.penalty = penalty;
   }

   public RunAwayFromEntitiesTask(Supplier<List<Entity>> toRunAwayFrom, double distanceToRun, double penalty) {
      this(toRunAwayFrom, distanceToRun, false, penalty);
   }

   @Override
   protected Goal newGoal(AltoClefController mod) {
      return new RunAwayFromEntitiesTask.GoalRunAwayStuff(mod, this.distanceToRun, this.xz);
   }

   private class GoalRunAwayStuff extends GoalRunAwayFromEntities {
      public GoalRunAwayStuff(AltoClefController mod, double distance, boolean xz) {
         super(mod, distance, xz, RunAwayFromEntitiesTask.this.penalty);
      }

      @Override
      protected List<Entity> getEntities(AltoClefController mod) {
         return RunAwayFromEntitiesTask.this.runAwaySupplier.get();
      }
   }
}
