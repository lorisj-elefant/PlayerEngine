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

package altoclef.multiversion;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

public class DamageSourceWrapper {
   private final DamageSource source;

   public static DamageSourceWrapper of(DamageSource source) {
      return source == null ? null : new DamageSourceWrapper(source);
   }

   private DamageSourceWrapper(DamageSource source) {
      this.source = source;
   }

   public DamageSource getSource() {
      return this.source;
   }

   public boolean bypassesArmor() {
      return this.source.is(DamageTypeTags.BYPASSES_ARMOR);
   }

   public boolean bypassesShield() {
      return this.source.is(DamageTypeTags.BYPASSES_SHIELD);
   }

   public boolean isOutOfWorld() {
      return this.source.is(DamageTypes.FELL_OUT_OF_WORLD);
   }
}
