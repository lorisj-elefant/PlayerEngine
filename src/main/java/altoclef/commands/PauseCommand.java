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

package altoclef.commands;

import altoclef.AltoClefController;
import altoclef.commandsystem.ArgParser;
import altoclef.commandsystem.Command;

public class PauseCommand extends Command {
   public PauseCommand() {
      super("pause", "Pauses the bot after the task thats running (Still in development!)");
   }

   @Override
   protected void call(AltoClefController mod, ArgParser parser) {
      mod.setStoredTask(mod.getUserTaskChain().getCurrentTask());
      mod.setPaused(true);
      mod.getUserTaskChain().stop();
      mod.log("Pausing Bot and time");
      this.finish();
   }
}
