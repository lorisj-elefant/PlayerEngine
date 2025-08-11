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

package altoclef.commands.random;

import altoclef.AltoClefController;
import altoclef.commandsystem.ArgParser;
import altoclef.commandsystem.Command;
import altoclef.commandsystem.CommandException;
import altoclef.tasksystem.Task;

public class DummyTaskCommand extends Command {
   public DummyTaskCommand() {
      super("dummy", "Doesnt do anything");
   }

   @Override
   protected void call(AltoClefController mod, ArgParser parser) throws CommandException {
      mod.runUserTask(new DummyTaskCommand.DummyTask(), () -> this.finish());
   }

   private class DummyTask extends Task {
      @Override
      protected void onStart() {
      }

      @Override
      protected Task onTick() {
         return null;
      }

      @Override
      protected void onStop(Task interruptTask) {
      }

      @Override
      protected boolean isEqual(Task other) {
         return false;
      }

      @Override
      protected String toDebugString() {
         return null;
      }
   }
}
