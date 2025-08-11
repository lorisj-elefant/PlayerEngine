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

package altoclef;

import altoclef.commands.AttackPlayerOrMobCommand;
import altoclef.commands.DepositCommand;
import altoclef.commands.EquipCommand;
import altoclef.commands.FarmCommand;
import altoclef.commands.FishCommand;
import altoclef.commands.FollowCommand;
import altoclef.commands.FoodCommand;
import altoclef.commands.GamerCommand;
import altoclef.commands.GetCommand;
import altoclef.commands.GiveCommand;
import altoclef.commands.GotoCommand;
import altoclef.commands.HeroCommand;
import altoclef.commands.IdleCommand;
import altoclef.commands.LocateStructureCommand;
import altoclef.commands.MeatCommand;
import altoclef.commands.ReloadSettingsCommand;
import altoclef.commands.ResetMemoryCommand;
import altoclef.commands.SetAIBridgeEnabledCommand;
import altoclef.commands.StopCommand;
import altoclef.commands.random.ScanCommand;
import altoclef.commandsystem.CommandException;

public class AltoClefCommands {
   public static void init(AltoClefController controller) throws CommandException {
      controller.getCommandExecutor()
         .registerNewCommand(
            new GetCommand(),
            new EquipCommand(),
            new DepositCommand(),
            new GotoCommand(),
            new IdleCommand(),
            new HeroCommand(),
            new LocateStructureCommand(),
            new StopCommand(),
            new FoodCommand(),
            new MeatCommand(),
            new ReloadSettingsCommand(),
            new ResetMemoryCommand(),
            new GamerCommand(),
            new FollowCommand(),
            new GiveCommand(),
            new ScanCommand(),
            new AttackPlayerOrMobCommand(),
            new SetAIBridgeEnabledCommand(),
            new FarmCommand(),
            new FishCommand()
         );
   }
}
