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

package altoclef.eventbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.util.Tuple;

public class EventBus {
   private static final HashMap<Class, List<Subscription>> topics = new HashMap<>();
   private static final List<Tuple<Class, Subscription>> toAdd = new ArrayList<>();
   private static boolean lock;

   public static <T> void publish(T event) {
      Class<?> type = event.getClass();

      for (Tuple<Class, Subscription> toAdd : EventBus.toAdd) {
         subscribeInternal((Class<T>)toAdd.getA(), (Subscription<T>)toAdd.getB());
      }

      EventBus.toAdd.clear();
      if (topics.containsKey(type)) {
         List<Subscription> subscribers = topics.get(type);
         List<Subscription> toDelete = new ArrayList<>();
         lock = true;

         for (Subscription<T> subRaw : subscribers) {
            try {
               if (subRaw.shouldDelete()) {
                  toDelete.add(subRaw);
               } else {
                  subRaw.accept(event);
               }
            } catch (ClassCastException var7) {
               System.err.println("TRIED PUBLISHING MISMAPPED EVENT: " + event);
               var7.printStackTrace();
            }
         }

         lock = false;
      }
   }

   private static <T> void subscribeInternal(Class<T> type, Subscription<T> sub) {
      if (!topics.containsKey(type)) {
         topics.put(type, new ArrayList<>());
      }

      topics.get(type).add(sub);
   }

   public static <T> Subscription<T> subscribe(Class<T> type, Consumer<T> consumeEvent) {
      Subscription<T> sub = new Subscription<>(consumeEvent);
      if (lock) {
         toAdd.add(new Tuple(type, sub));
      } else {
         subscribeInternal(type, sub);
      }

      return sub;
   }

   public static <T> void unsubscribe(Subscription<T> subscription) {
      if (subscription != null) {
         subscription.delete();
      }
   }
}
