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

package altoclef.mixins.baritone;

import baritone.api.IBaritone;
import baritone.api.utils.IEntityAccessor;
import baritone.behavior.PathingBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class})
public abstract class MixinEntity implements IEntityAccessor {
   @Shadow
   public abstract Level level();

   @Invoker("getEyeHeight")
   @Override
   public abstract float automatone$invokeGetEyeHeight(Pose var1, EntityDimensions var2);

   @Inject(
      method = {"setRemoved"},
      at = {@At("RETURN")}
   )
   private void shutdownPathingOnUnloading(RemovalReason reason, CallbackInfo ci) {
      if (!this.level().isClientSide() && ((Object)this) instanceof LivingEntity) {
         IBaritone.KEY.maybeGet((LivingEntity)(Object)this).ifPresent(b -> ((PathingBehavior)b.getPathingBehavior()).shutdown());
      }
   }
}
