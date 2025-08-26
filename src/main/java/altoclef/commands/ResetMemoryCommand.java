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
import altoclef.player2api.EventQueueManager;

public class ResetMemoryCommand extends Command {
   public ResetMemoryCommand() {
      super("resetmemory", "Reset the memory, does not stop the agent, can ONLY be run by the user (NOT the agent).");
   }

   @Override
   protected void call(AltoClefController mod, ArgParser parser) {
      EventQueueManager.resetMemory(mod);
      this.finish();
   }
}
