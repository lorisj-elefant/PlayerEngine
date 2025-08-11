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
import altoclef.tasksystem.Task;
import java.util.List;

public class StatusCommand extends Command {
   public StatusCommand() {
      super("status", "Get status of currently executing command");
   }

   @Override
   protected void call(AltoClefController mod, ArgParser parser) {
      List<Task> tasks = mod.getUserTaskChain().getTasks();
      if (tasks.isEmpty()) {
         mod.log("No tasks currently running.");
      } else {
         mod.log("CURRENT TASK: " + tasks.get(0).toString());
      }

      this.finish();
   }
}
