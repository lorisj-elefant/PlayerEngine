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
import altoclef.TaskCatalogue;
import altoclef.commandsystem.Arg;
import altoclef.commandsystem.ArgParser;
import altoclef.commandsystem.Command;
import altoclef.commandsystem.CommandException;
import altoclef.tasks.entity.GiveItemToPlayerTask;
import altoclef.util.ItemTarget;
import altoclef.util.helpers.FuzzySearchHelper;
import altoclef.util.helpers.ItemHelper;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

public class GiveCommand extends Command {
   public GiveCommand() throws CommandException {
      super(
         "give",
         "Give or drop an item to a player. Examples: `give Ellie diamond 3` to give player with username Ellie 3 diamonds.",
         new Arg<>(String.class, "username", null, 2),
         new Arg<>(String.class, "item"),
         new Arg<>(Integer.class, "count", 1, 1)
      );
   }

   @Override
   protected void call(AltoClefController mod, ArgParser parser) throws CommandException {
      String username = parser.get(String.class);
      if (username == null) {
         if (mod.getOwner() == null) {
            mod.logWarning("No butler user currently present. Running this command with no user argument can ONLY be done via butler.");
            this.finish();
            return;
         }

         username = mod.getOwner().getName().getString();
      }

      String item = parser.get(String.class);
      int count = parser.get(Integer.class);
      ItemTarget target = null;
      if (TaskCatalogue.taskExists(item)) {
         target = TaskCatalogue.getItemTarget(item, count);
      } else {
         for (int i = 0; i < mod.getInventory().getContainerSize(); i++) {
            ItemStack stack = mod.getInventory().getItem(i);
            if (!stack.isEmpty()) {
               String name = ItemHelper.stripItemName(stack.getItem());
               if (name.equals(item)) {
                  target = new ItemTarget(stack.getItem(), count);
                  break;
               }
            }
         }
      }

      if (!mod.getEntityTracker().isPlayerLoaded(username)) {
         String nearbyUsernames = String.join(",", mod.getEntityTracker().getAllLoadedPlayerUsernames());
         Debug.logMessage(
            "No user in render distance found with username \""
               + username
               + "\". Maybe this was a typo or there is a user with a similar name around? Nearby users: ["
               + nearbyUsernames
               + "]."
         );
         this.finish();
      } else {
         if (target != null) {
            Debug.logMessage("USER: " + username + " : ITEM: " + item + " x " + count);
            mod.runUserTask(new GiveItemToPlayerTask(username, target), () -> this.finish());
         } else {
            Set<String> validNames = new HashSet<>(TaskCatalogue.resourceNames());

            for (int ix = 0; ix < mod.getInventory().getContainerSize(); ix++) {
               ItemStack stack = mod.getInventory().getItem(ix);
               if (!stack.isEmpty()) {
                  String name = ItemHelper.stripItemName(stack.getItem());
                  validNames.add(name);
               }
            }

            String closestMatch = FuzzySearchHelper.getClosestMatchMinecraftItems(item, validNames);
            mod.log("Item not found or task does not exist for item: \"" + item + "\". Does the user mean \"" + closestMatch + "\"?");
            this.finish();
         }
      }
   }
}
