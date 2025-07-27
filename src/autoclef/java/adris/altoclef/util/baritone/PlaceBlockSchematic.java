package adris.altoclef.util.baritone;

import baritone.api.schematic.AbstractSchematic;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

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
    this(new Block[] { blockToPlace });
  }
  
  public boolean foundSpot() {
    return (this.targetPlace != null);
  }
  
  public BlockState desiredState(int x, int y, int z, BlockState blockState, List<BlockState> list) {
    if (x != 0 || y != 0 || z != 0)
      return blockState; 
    if (this.skipIfAlreadyThere && blockIsTarget(blockState.getBlock()))
      this.targetPlace = blockState; 
    boolean isDone = (this.targetPlace != null);
    if (isDone)
      return this.targetPlace; 
    if (!list.isEmpty())
      for (BlockState possible : list) {
        if (possible == null)
          continue; 
        if (blockIsTarget(possible.getBlock())) {
          this.targetPlace = possible;
          return possible;
        } 
      }  
    return blockState;
  }
  
  private boolean blockIsTarget(Block block) {
    if (this.blockToPlace != null)
      for (Block check : this.blockToPlace) {
        if (check == block)
          return true; 
      }  
    return false;
  }
}
