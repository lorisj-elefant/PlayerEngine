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

import java.lang.reflect.ParameterizedType;

public abstract class ArgBase {
   protected int minArgCountToUseDefault;
   protected boolean hasDefault;

   protected <V> V getConverted(Class<V> vType, Object ob) {
      try {
         return (V)ob;
      } catch (Exception var4) {
         throw new IllegalArgumentException(
            "Tried to convert the following object to type {typeof(V)} and failed: {ob}. This is probably an internal problem, contact the dev!"
         );
      }
   }

   public <V> V parseUnit(String unit, String[] unitPlusRemainder) throws CommandException {
      Class<V> vType = (Class<V>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
      return this.getConverted(vType, this.parseUnit(unit, unitPlusRemainder));
   }

   public abstract <V> V getDefault(Class<V> var1);

   public abstract String getHelpRepresentation();

   public int getMinArgCountToUseDefault() {
      return this.minArgCountToUseDefault;
   }

   public boolean hasDefault() {
      return this.hasDefault;
   }

   public boolean isArray() {
      return false;
   }

   public boolean isArbitrarilyLong() {
      return false;
   }
}
