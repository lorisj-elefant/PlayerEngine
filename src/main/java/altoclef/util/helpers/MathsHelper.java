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

import net.minecraft.world.phys.Vec3;

public interface MathsHelper {
   static Vec3 project(Vec3 vec, Vec3 onto, boolean assumeOntoNormalized) {
      if (!assumeOntoNormalized) {
         onto = onto.normalize();
      }

      return onto.scale(vec.dot(onto));
   }

   static Vec3 project(Vec3 vec, Vec3 onto) {
      return project(vec, onto, false);
   }

   static Vec3 projectOntoPlane(Vec3 vec, Vec3 normal, boolean assumeNormalNormalized) {
      Vec3 p = project(vec, normal, assumeNormalNormalized);
      return vec.subtract(p);
   }

   static Vec3 projectOntoPlane(Vec3 vec, Vec3 normal) {
      return projectOntoPlane(vec, normal, false);
   }
}
