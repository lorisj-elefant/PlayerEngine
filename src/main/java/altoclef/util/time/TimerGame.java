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

package altoclef.util.time;

import altoclef.AltoClefController;
import altoclef.Debug;
import altoclef.mixins.ClientConnectionAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;

public class TimerGame extends BaseTimer {
   private Connection lastConnection;

   public TimerGame(double intervalSeconds) {
      super(intervalSeconds);
   }

   private static double getTime(Connection connection) {
      return connection == null ? 0.0 : ((ClientConnectionAccessor)connection).getTicks() / 20.0;
   }

   @Override
   protected double currentTime() {
      if (!AltoClefController.inGame()) {
         Debug.logError("Running game timer while not in game.");
         return 0.0;
      } else {
         Connection currentConnection = null;
         if (Minecraft.getInstance().getConnection() != null) {
            currentConnection = Minecraft.getInstance().getConnection().getConnection();
         }

         if (currentConnection != this.lastConnection) {
            if (this.lastConnection != null) {
               double prevTimeTotal = getTime(this.lastConnection);
               Debug.logInternal("(TimerGame: New connection detected, offsetting by " + prevTimeTotal + " seconds)");
               this.setPrevTimeForce(this.getPrevTime() - prevTimeTotal);
            }

            this.lastConnection = currentConnection;
         }

         return getTime(currentConnection);
      }
   }
}
