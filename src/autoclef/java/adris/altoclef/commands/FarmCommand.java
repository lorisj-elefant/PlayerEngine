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

package adris.altoclef.commands;

import adris.altoclef.AltoClefController;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.misc.FarmTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.util.math.BlockPos;

public class FarmCommand extends Command {

    public FarmCommand() throws CommandException {
        super("farm", "Starts farming nearby crops automatically within range.  Example: `farm 10` to farm crops withing a range of 10 blocks",
                new Arg(Integer.class, "range")
        );
    }

    @Override
    protected void call(AltoClefController controller, ArgParser parser) throws CommandException {
        Integer range = parser.get(Integer.class);
        BlockPos origin = controller.getEntity().getBlockPos();
        Task farmTask = new FarmTask(range, origin);
        controller.runUserTask(farmTask, this::finish);
    }
}