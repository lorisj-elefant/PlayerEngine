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
import altoclef.commandsystem.Arg;
import altoclef.commandsystem.ArgParser;
import altoclef.commandsystem.Command;
import altoclef.commandsystem.CommandException;
import altoclef.tasks.movement.GoToStrongholdPortalTask;
import altoclef.tasks.movement.LocateDesertTempleTask;

public class LocateStructureCommand extends Command {
   public LocateStructureCommand() throws CommandException {
      super(
         "locate_structure",
         "Locate a world generated structure. Only works for stronghold and desert_temple",
         new Arg<>(LocateStructureCommand.Structure.class, "structure")
      );
   }

   @Override
   protected void call(AltoClefController mod, ArgParser parser) throws CommandException {
      LocateStructureCommand.Structure structure = parser.get(LocateStructureCommand.Structure.class);
      switch (structure) {
         case STRONGHOLD:
            mod.runUserTask(new GoToStrongholdPortalTask(1), () -> this.finish());
            break;
         case DESERT_TEMPLE:
            mod.runUserTask(new LocateDesertTempleTask(), () -> this.finish());
      }
   }

   public static enum Structure {
      DESERT_TEMPLE,
      STRONGHOLD;
   }
}
