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

package altoclef.player2api.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ObjectStatus {
   protected final Map<String, String> fields = new HashMap<>();

   public ObjectStatus add(String key, String value) {
      this.fields.put(key, value);
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("{\n");

      for (Entry<String, String> entry : this.fields.entrySet()) {
         sb.append(entry.getKey()).append(" : \"").append(entry.getValue()).append("\",\n");
      }

      sb.append("}");
      return sb.toString();
   }
}
