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

package adris.altoclef.mixins.baritone;

import baritone.BaritoneProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinMobEntity {
    @Shadow
    public abstract void setMovementSpeed(float movementSpeed);

    @Inject(method = "tickNewAi", at = @At("HEAD"), cancellable = true)
    private void cancelAiTick(CallbackInfo ci) {
        if (BaritoneProvider.INSTANCE.isPathing((LivingEntity) (Object) this)) {
            // mobs tend to set their movement speed to 0, preventing any movement
            float forwardSpeed = ((LivingEntity) (Object) this).forwardSpeed;
            this.setMovementSpeed((float) ((LivingEntity) (Object) this).getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
            ((LivingEntity) (Object) this).forwardSpeed = forwardSpeed;
            ci.cancel();
        }
    }
}
