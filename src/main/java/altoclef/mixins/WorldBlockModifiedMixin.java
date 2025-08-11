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

package altoclef.mixins;

import altoclef.eventbus.EventBus;
import altoclef.eventbus.events.BlockPlaceEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Level.class})
public class WorldBlockModifiedMixin {
   @Unique
   private boolean hasBlock(BlockState state, BlockPos pos) {
      return !state.isAir() && state.isRedstoneConductor((Level)(Object)this, pos);
   }

   @Inject(
      method = {"onBlockStateChange"},
      at = {@At("HEAD")}
   )
   public void onBlockWasChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
      if (!((Level)(Object)this).isClientSide && !this.hasBlock(oldBlock, pos) && this.hasBlock(newBlock, pos)) {
         BlockPlaceEvent evt = new BlockPlaceEvent(pos, newBlock);
         EventBus.publish(evt);
      }
   }
}
