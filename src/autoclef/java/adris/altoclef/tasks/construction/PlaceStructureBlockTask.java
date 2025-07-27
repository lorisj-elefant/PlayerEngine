package adris.altoclef.tasks.construction;

import adris.altoclef.tasks.construction.PlaceBlockTask;
import net.minecraft.util.math.BlockPos;

public class PlaceStructureBlockTask extends PlaceBlockTask {
  public PlaceStructureBlockTask(BlockPos target) {
    super(target, new net.minecraft.block.Block[0], true, true);
  }
}
