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

package altoclef.util.baritone;

import baritone.api.schematic.AbstractSchematic;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceBlockSchematic extends AbstractSchematic {
   private static final int RANGE = 1;
   private final Block[] blockToPlace;
   private final boolean skipIfAlreadyThere;
   private final boolean done;
   private BlockState targetPlace;

   public PlaceBlockSchematic(Block[] blocksToPlace, boolean skipIfAlreadyThere) {
      super(1, 1, 1);
      this.blockToPlace = blocksToPlace;
      this.done = false;
      this.targetPlace = null;
      this.skipIfAlreadyThere = skipIfAlreadyThere;
   }

   public PlaceBlockSchematic(Block[] blocksToPlace) {
      this(blocksToPlace, true);
   }

   public PlaceBlockSchematic(Block blockToPlace) {
      this(new Block[]{blockToPlace});
   }

   public boolean foundSpot() {
      return this.targetPlace != null;
   }

   @Override
   public BlockState desiredState(int x, int y, int z, BlockState blockState, List<BlockState> list) {
      if (x == 0 && y == 0 && z == 0) {
         if (this.skipIfAlreadyThere && this.blockIsTarget(blockState.getBlock())) {
            this.targetPlace = blockState;
         }

         boolean isDone = this.targetPlace != null;
         if (isDone) {
            return this.targetPlace;
         } else {
            if (!list.isEmpty()) {
               for (BlockState possible : list) {
                  if (possible != null && this.blockIsTarget(possible.getBlock())) {
                     this.targetPlace = possible;
                     return possible;
                  }
               }
            }

            return blockState;
         }
      } else {
         return blockState;
      }
   }

   private boolean blockIsTarget(Block block) {
      if (this.blockToPlace != null) {
         for (Block check : this.blockToPlace) {
            if (check == block) {
               return true;
            }
         }
      }

      return false;
   }
}
