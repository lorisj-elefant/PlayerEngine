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
import net.minecraft.world.phys.Vec3;

public class Vec3dDeserializer extends AbstractVectorDeserializer<Vec3, Double> {
   @Override
   protected String getTypeName() {
      return "Vec3d";
   }

   @Override
   protected String[] getComponents() {
      return new String[]{"x", "y"};
   }

   protected Double parseUnit(String unit) throws Exception {
      return Double.parseDouble(unit);
   }

   protected Vec3 deserializeFromUnits(List<Double> units) {
      return new Vec3(units.get(0), units.get(1), units.get(2));
   }

   @Override
   protected boolean isUnitTokenValid(JsonToken token) {
      return token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT;
   }
}
