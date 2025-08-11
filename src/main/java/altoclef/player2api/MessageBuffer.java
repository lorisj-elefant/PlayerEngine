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

import java.util.ArrayList;

public class MessageBuffer {
   ArrayList<String> msgs = new ArrayList<>();
   int maxSize;

   public MessageBuffer(int maxSize) {
      this.maxSize = maxSize;
   }

   public void addMsg(String msg) {
      this.msgs.add(msg);
      if (this.msgs.size() > this.maxSize) {
         this.msgs.remove(0);
      }
   }

   private void dump() {
      this.msgs = new ArrayList<>();
   }

   public String dumpAndGetString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");

      for (String msg : this.msgs) {
         sb.append(String.format("\"%s\",", msg));
      }

      this.dump();
      return sb.append("]").toString();
   }
}
