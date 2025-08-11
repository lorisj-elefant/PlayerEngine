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
import altoclef.commandsystem.ItemList;
import altoclef.tasks.container.StoreInStashTask;
import altoclef.util.BlockRange;
import altoclef.util.ItemTarget;
import altoclef.util.helpers.WorldHelper;
import net.minecraft.core.BlockPos;

public class StashCommand extends Command {
   public StashCommand() throws CommandException {
      super(
         "stash",
         "Store an item in a chest/container stash. Will deposit ALL non-equipped items if item list is empty.",
         new Arg<>(Integer.class, "x_start"),
         new Arg<>(Integer.class, "y_start"),
         new Arg<>(Integer.class, "z_start"),
         new Arg<>(Integer.class, "x_end"),
         new Arg<>(Integer.class, "y_end"),
         new Arg<>(Integer.class, "z_end"),
         new Arg<>(ItemList.class, "items (empty for ALL)", null, 6, false)
      );
   }

   @Override
   protected void call(AltoClefController mod, ArgParser parser) throws CommandException {
      BlockPos start = new BlockPos(parser.get(Integer.class), parser.get(Integer.class), parser.get(Integer.class));
      BlockPos end = new BlockPos(parser.get(Integer.class), parser.get(Integer.class), parser.get(Integer.class));
      ItemList itemList = parser.get(ItemList.class);
      ItemTarget[] items;
      if (itemList == null) {
         items = DepositCommand.getAllNonEquippedOrToolItemsAsTarget(mod);
      } else {
         items = itemList.items;
      }

      mod.runUserTask(new StoreInStashTask(true, new BlockRange(start, end, WorldHelper.getCurrentDimension(mod)), items), () -> this.finish());
   }
}
