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

public class WorldStatus extends ObjectStatus {
   public static WorldStatus fromMod(AltoClefController mod) {
      return (WorldStatus)new WorldStatus()
         .add("weather", StatusUtils.getWeatherString(mod))
         .add("dimension", StatusUtils.getDimensionString(mod))
         .add("spawn position", StatusUtils.getSpawnPosString(mod))
         .add("nearby blocks", StatusUtils.getNearbyBlocksString(mod))
         .add("nearby hostiles", StatusUtils.getNearbyHostileMobs(mod))
         .add("nearby players", StatusUtils.getNearbyPlayers(mod))
         .add("nearby other npcs", StatusUtils.getNearbyNPCs(mod))
         .add("difficulty", StatusUtils.getDifficulty(mod))
         .add("timeInfo", StatusUtils.getTimeString(mod));
   }
}
