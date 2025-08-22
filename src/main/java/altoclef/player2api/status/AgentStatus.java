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

package altoclef.player2api.status;

import altoclef.AltoClefController;
import net.minecraft.world.entity.LivingEntity;

public class AgentStatus extends ObjectStatus {
   public static AgentStatus fromMod(AltoClefController mod) {
      LivingEntity player = mod.getPlayer();
      return (AgentStatus)new AgentStatus()
         .add("health", String.format("%.2f/20", player.getHealth()))
         .add("food", String.format("%.2f/20", (float)mod.getBaritone().getEntityContext().hungerManager().getFoodLevel()))
         .add("saturation", String.format("%.2f/20", mod.getBaritone().getEntityContext().hungerManager().getSaturationLevel()))
         .add("position", String.format("(%f,%f,%f)", mod.getPlayer().getX(), mod.getPlayer().getY(), mod.getPlayer().getZ()))
         .add("inventory", StatusUtils.getInventoryString(mod))
         .add("taskStatus", StatusUtils.getTaskStatusString(mod))
         .add("oxygenLevel", StatusUtils.getOxygenString(mod))
         .add("armor", StatusUtils.getEquippedArmorStatusString(mod))
         .add("gamemode", StatusUtils.getGamemodeString(mod))
         .add("taskTree", StatusUtils.getTaskTree(mod));
   }
}
