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
import altoclef.Debug;
import altoclef.commandsystem.Arg;
import altoclef.commandsystem.ArgParser;
import altoclef.commandsystem.Command;
import altoclef.commandsystem.CommandException;

public class SetAIBridgeEnabledCommand extends Command {
   public SetAIBridgeEnabledCommand() throws CommandException {
      super(
         "chatclef",
         "Turns chatclef on or off, can ONLY be run by the user (NOT the agent).",
         new Arg<>(SetAIBridgeEnabledCommand.ToggleState.class, "onOrOff")
      );
   }

   @Override
   protected void call(AltoClefController mod, ArgParser parser) throws CommandException {
      SetAIBridgeEnabledCommand.ToggleState toggle = parser.get(SetAIBridgeEnabledCommand.ToggleState.class);
      switch (toggle) {
         case ON:
            Debug.logMessage(
               "Enabling the AI Bridge! You can now hear the player again and will intercept their messages, give them a quick welcome back message."
            );
            mod.setChatClefEnabled(true);
            break;
         case OFF:
            Debug.logMessage("AI Bridge disabled! Say goodbye to the player as you won't hear or intercept any of their messages until they turn you back on.");
            mod.setChatClefEnabled(false);
      }

      this.finish();
   }

   public static enum ToggleState {
      ON,
      OFF;
   }
}
