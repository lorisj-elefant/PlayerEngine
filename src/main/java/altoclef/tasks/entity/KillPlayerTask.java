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
import net.minecraft.world.entity.player.Player;

public class KillPlayerTask extends AbstractKillEntityTask {
   private String playerName;

   public KillPlayerTask(String playerName) {
      this.playerName = playerName;
   }

   public KillPlayerTask(String playerName, double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
      super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
      this.playerName = playerName;
   }

   @Override
   protected Optional<Entity> getEntityTarget(AltoClefController mod) {
      for (Entity entity : this.controller.getWorld().getAllEntities()) {
         if (entity.isAlive() && entity instanceof Player) {
            String playerName = entity.getName().getString().toLowerCase();
            if (playerName != null && playerName.equals(this.playerName.toLowerCase())) {
               return Optional.of(entity);
            }
         }
      }

      return Optional.empty();
   }

   @Override
   protected boolean isSubEqual(AbstractDoToEntityTask other) {
      return other instanceof KillPlayerTask task ? Objects.equals(task.playerName, this.playerName) : false;
   }

   @Override
   protected String toDebugString() {
      return "Killing Player " + this.playerName;
   }
}
