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

import com.fasterxml.jackson.core.JsonToken;
import java.util.List;
import net.minecraft.core.BlockPos;

public class BlockPosDeserializer extends AbstractVectorDeserializer<BlockPos, Integer> {
   @Override
   protected String getTypeName() {
      return "BlockPos";
   }

   @Override
   protected String[] getComponents() {
      return new String[]{"x", "y", "z"};
   }

   protected Integer parseUnit(String unit) throws Exception {
      return Integer.parseInt(unit);
   }

   protected BlockPos deserializeFromUnits(List<Integer> units) {
      return new BlockPos(units.get(0), units.get(1), units.get(2));
   }

   @Override
   protected boolean isUnitTokenValid(JsonToken token) {
      return token == JsonToken.VALUE_NUMBER_INT;
   }
}
