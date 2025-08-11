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
import altoclef.commands.BlockScanner;
import altoclef.commandsystem.Arg;
import altoclef.commandsystem.ArgParser;
import altoclef.commandsystem.Command;
import altoclef.commandsystem.CommandException;
import altoclef.util.helpers.FuzzySearchHelper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ScanCommand extends Command {
   public ScanCommand() throws CommandException {
      super("scan", "Locates nearest block", new Arg<>(String.class, "block", "DIRT", 0));
   }

   @Override
   protected void call(AltoClefController mod, ArgParser parser) throws CommandException {
      String blockStr = parser.get(String.class);
      Field[] declaredFields = Blocks.class.getDeclaredFields();
      Block block = null;
      List<String> allBlockNames = new ArrayList<>();

      for (Field field : declaredFields) {
         field.setAccessible(true);

         try {
            String fieldName = field.getName();
            allBlockNames.add(fieldName.toLowerCase());
            if (fieldName.equalsIgnoreCase(blockStr)) {
               block = (Block)field.get(Blocks.class);
            }
         } catch (IllegalAccessException var12) {
            throw new RuntimeException(var12);
         }

         field.setAccessible(false);
      }

      if (block == null) {
         String closest = FuzzySearchHelper.getClosestMatchMinecraftItems(blockStr, allBlockNames);
         mod.log("Block named: \"" + blockStr + "\" not a valid block. Perhaps the user meant \"" + closest + "\"?");
         this.finish();
      } else {
         BlockScanner blockScanner = mod.getBlockScanner();
         Optional<BlockPos> p = blockScanner.getNearestBlock(block, mod.getPlayer().position());
         if (p.isPresent()) {
            mod.log("Closest " + blockStr + ": " + p.get().toString());
         } else {
            mod.log("No blocks of type " + blockStr + " found nearby.");
         }

         this.finish();
      }
   }
}
