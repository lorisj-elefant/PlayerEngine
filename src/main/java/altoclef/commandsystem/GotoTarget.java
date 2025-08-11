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

package altoclef.commandsystem;

import altoclef.util.Dimension;
import java.util.ArrayList;
import java.util.List;

public class GotoTarget {
   private final int x;
   private final int y;
   private final int z;
   private final Dimension dimension;
   private final GotoTarget.GotoTargetCoordType type;

   public GotoTarget(int x, int y, int z, Dimension dimension, GotoTarget.GotoTargetCoordType type) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.dimension = dimension;
      this.type = type;
   }

   public static GotoTarget parseRemainder(String line) throws CommandException {
      line = line.trim();
      if (line.startsWith("(") && line.endsWith(")")) {
         line = line.substring(1, line.length() - 1);
      }

      String[] parts = line.split(" ");
      List<Integer> numbers = new ArrayList<>();
      Dimension dimension = null;

      for (String part : parts) {
         try {
            int num = Integer.parseInt(part);
            numbers.add(num);
         } catch (NumberFormatException var9) {
            dimension = (Dimension)Arg.parseEnum(part, Dimension.class);
            break;
         }
      }

      int x = 0;
      int y = 0;
      int z = 0;

      return new GotoTarget(x, y, z, dimension, switch (numbers.size()) {
         case 0 -> GotoTarget.GotoTargetCoordType.NONE;
         case 1 -> {
            y = numbers.get(0);
            yield GotoTarget.GotoTargetCoordType.Y;
         }
         case 2 -> {
            x = numbers.get(0);
            z = numbers.get(1);
            yield GotoTarget.GotoTargetCoordType.XZ;
         }
         case 3 -> {
            x = numbers.get(0);
            y = numbers.get(1);
            z = numbers.get(2);
            yield GotoTarget.GotoTargetCoordType.XYZ;
         }
         default -> throw new CommandException("Unexpected number of integers passed to coordinate: " + numbers.size());
      });
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   public Dimension getDimension() {
      return this.dimension;
   }

   public boolean hasDimension() {
      return this.dimension != null;
   }

   public GotoTarget.GotoTargetCoordType getType() {
      return this.type;
   }

   public static enum GotoTargetCoordType {
      XYZ,
      XZ,
      Y,
      NONE;
   }
}
