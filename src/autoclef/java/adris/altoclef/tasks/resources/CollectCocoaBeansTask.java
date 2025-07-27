package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.movement.SearchWithinBiomeTask;
import adris.altoclef.tasksystem.Task;
import java.util.HashSet;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.item.Items;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biomes;

public class CollectCocoaBeansTask extends ResourceTask {
  private final int _count;
  
  private final HashSet<BlockPos> _wasFullyGrown = new HashSet<>();
  
  public CollectCocoaBeansTask(int targetCount) {
    super(Items.COCOA_BEANS, targetCount);
    this._count = targetCount;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    Predicate<BlockPos> validCocoa = blockPos -> {
        if (!mod.getChunkTracker().isChunkLoaded(blockPos))
          return this._wasFullyGrown.contains(blockPos); 
        BlockState s = mod.getWorld().getBlockState(blockPos);
        boolean mature = (((Integer)s.get((Property)CocoaBlock.AGE)).intValue() == 2);
        if (this._wasFullyGrown.contains(blockPos)) {
          if (!mature)
            this._wasFullyGrown.remove(blockPos); 
        } else if (mature) {
          this._wasFullyGrown.add(blockPos);
        } 
        return mature;
      };
    if (mod.getBlockScanner().anyFound(validCocoa, new Block[] { Blocks.COCOA })) {
      setDebugState("Breaking cocoa blocks");
      return (Task)new DoToClosestBlockTask(adris.altoclef.tasks.construction.DestroyBlockTask::new, validCocoa, new Block[] { Blocks.COCOA });
    } 
    if (isInWrongDimension(mod))
      return getToCorrectDimensionTask(mod); 
    setDebugState("Exploring around jungles");
    return (Task)new SearchWithinBiomeTask(Biomes.JUNGLE);
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectCocoaBeansTask;
  }
  
  protected String toDebugStringName() {
    return "Collecting " + this._count + " cocoa beans.";
  }
}
