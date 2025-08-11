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

package altoclef.util.helpers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

public interface StlHelper {
   static <T> Comparator<T> compareValues(Function<T, Double> getValue) {
      return (left, right) -> (int)Math.signum(getValue.apply(left) - getValue.apply(right));
   }

   static <T> String toString(Collection<T> thing, Function<T, String> toStringFunc) {
      StringBuilder result = new StringBuilder();
      result.append("[");
      int i = 0;

      for (T item : thing) {
         result.append(toStringFunc.apply(item));
         if (i != thing.size() - 1) {
            result.append(",");
         }

         i++;
      }

      result.append("]");
      return result.toString();
   }

   static <T> String toString(T[] thing, Function<T, String> toStringFunc) {
      try {
         return toString(Arrays.asList(thing), toStringFunc);
      } catch (NullPointerException var3) {
         return "<null>";
      }
   }
}
