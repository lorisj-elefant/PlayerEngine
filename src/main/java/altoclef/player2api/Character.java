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

package altoclef.player2api;

import java.util.Arrays;

public record Character(String name, String shortName, String greetingInfo, String description, String skinURL,
      String[] voiceIds) {
   /**
    * Returns a formatted string representation of the Character object.
    *
    * @return A string containing character details.
    */
   @Override
   public String toString() {
      return String.format(
            "Character{name='%s', shortName='%s', greeting='%s', voiceIds=%s}",
            name,
            shortName,
            greetingInfo,
            Arrays.toString(voiceIds));
   }
}