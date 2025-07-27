package adris.altoclef.trackers;

import adris.altoclef.AltoClefController;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.WorldHelper;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class UserBlockRangeTracker extends Tracker {
  final int AVOID_BREAKING_RANGE = 16;
  
  final Block[] USER_INDICATOR_BLOCKS;
  
  final Block[] USER_BLOCKS_TO_AVOID_BREAKING;
  
  private final Set<BlockPos> _dontBreakBlocks;
  
  public UserBlockRangeTracker(TrackerManager manager) {
    super(manager);
    this.USER_INDICATOR_BLOCKS = (Block[])Streams.concat(new Stream[] { Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.BED)) }).toArray(x$0 -> new Block[x$0]);
    this.USER_BLOCKS_TO_AVOID_BREAKING = (Block[])Streams.concat(new Stream[] { Arrays.<Block>asList(new Block[] { Blocks.COBBLESTONE }).stream(), Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.LOG)) }).toArray(x$0 -> new Block[x$0]);
    this._dontBreakBlocks = new HashSet<>();
  }
  
  public boolean isNearUserTrackedBlock(BlockPos pos) {
    ensureUpdated();
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      return this._dontBreakBlocks.contains(pos);
    } 
  }
  
  protected void updateState() {
    this._dontBreakBlocks.clear();
    List<BlockPos> userBlocks = mod.getBlockScanner().getKnownLocationsIncludeUnreachable(this.USER_INDICATOR_BLOCKS);
    Set<Block> userIndicatorBlocks = new HashSet<>(Arrays.asList(this.USER_INDICATOR_BLOCKS));
    Set<Block> userBlocksToAvoidMining = new HashSet<>(Arrays.asList(this.USER_BLOCKS_TO_AVOID_BREAKING));
    userBlocks.removeIf(bpos -> {
          Block b = mod.getWorld().getBlockState(bpos).getBlock();
          return !userIndicatorBlocks.contains(b);
        });
    for (BlockPos userBlockPos : userBlocks) {
      BlockPos min = userBlockPos.add(-16, -16, -16);
      BlockPos max = userBlockPos.add(16, 16, 16);
      for (BlockPos possible : WorldHelper.scanRegion(min, max)) {
        Block b = mod.getWorld().getBlockState(possible).getBlock();
        if (userBlocksToAvoidMining.contains(b))
          this._dontBreakBlocks.add(possible); 
      } 
    } 
  }
  
  protected void reset() {
    this._dontBreakBlocks.clear();
  }
}
