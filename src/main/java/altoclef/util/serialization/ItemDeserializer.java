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

package altoclef.util.serialization;

import altoclef.Debug;
import altoclef.util.helpers.ItemHelper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemDeserializer extends StdDeserializer<Object> {
   public ItemDeserializer() {
      this(null);
   }

   public ItemDeserializer(Class<Object> vc) {
      super(vc);
   }

   @Override
   public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      List<Item> result = new ArrayList<>();
      if (p.getCurrentToken() != JsonToken.START_ARRAY) {
         throw new JsonParseException(p, "Start array expected");
      } else {
         while (p.nextToken() != JsonToken.END_ARRAY) {
            Item item = null;
            if (p.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
               int rawId = p.getIntValue();
               item = Item.byId(rawId);
            } else {
               String itemKey = p.getText();
               itemKey = ItemHelper.trimItemName(itemKey);
               ResourceLocation identifier = new ResourceLocation(itemKey);
               if (BuiltInRegistries.ITEM.containsKey(identifier)) {
                  item = (Item)BuiltInRegistries.ITEM.get(identifier);
               } else {
                  Debug.logWarning("Invalid item name:" + itemKey + " at " + p.getCurrentLocation().toString());
               }
            }

            if (item != null) {
               result.add(item);
            }
         }

         return result;
      }
   }
}
