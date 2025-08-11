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

package altoclef.util;

import java.util.Objects;
import net.minecraft.world.item.Item;

public class SmeltTarget {
   private final ItemTarget item;
   private final Item[] optionalMaterials;
   private ItemTarget material;

   public SmeltTarget(ItemTarget item, ItemTarget material, Item... optionalMaterials) {
      this.item = item;
      this.material = material;
      this.material = new ItemTarget(material, this.item.getTargetCount());
      this.optionalMaterials = optionalMaterials;
   }

   public ItemTarget getItem() {
      return this.item;
   }

   public ItemTarget getMaterial() {
      return this.material;
   }

   public Item[] getOptionalMaterials() {
      return this.optionalMaterials;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SmeltTarget that = (SmeltTarget)o;
         return Objects.equals(this.material, that.material) && Objects.equals(this.item, that.item);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.material, this.item);
   }
}
