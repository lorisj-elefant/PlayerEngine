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

public class Character {
   public final String name;
   public final String shortName;
   public final String greetingInfo;
   public final String description;
   public final String skinURL;
   public final String[] voiceIds;

   public Character(String characterName, String shortName, String greetingInfo, String description, String skinURL, String[] voiceIds) {
      this.name = characterName;
      this.shortName = shortName;
      this.greetingInfo = greetingInfo;
      this.voiceIds = voiceIds;
      this.skinURL = skinURL;
      this.description = description;
   }

   @Override
   public String toString() {
      return String.format(
         "Character{name='%s', shortName='%s', greeting='%s', voiceIds=%s}",
         this.name,
         this.shortName,
         this.greetingInfo,
         Arrays.toString((Object[])this.voiceIds)
      );
   }
}
