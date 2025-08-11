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

package altoclef.tasks.entity;

import altoclef.AltoClefController;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.Entity;

public class KillEntityTask extends AbstractKillEntityTask {
   private final Entity target;

   public KillEntityTask(Entity entity) {
      this.target = entity;
   }

   public KillEntityTask(Entity entity, double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
      super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
      this.target = entity;
   }

   @Override
   protected Optional<Entity> getEntityTarget(AltoClefController mod) {
      return Optional.of(this.target);
   }

   @Override
   protected boolean isSubEqual(AbstractDoToEntityTask other) {
      return other instanceof KillEntityTask task ? Objects.equals(task.target, this.target) : false;
   }

   @Override
   protected String toDebugString() {
      return "Killing " + this.target.getType().getDescriptionId();
   }
}
