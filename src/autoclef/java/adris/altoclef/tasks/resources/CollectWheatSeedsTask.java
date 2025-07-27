package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.MiningRequirement;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class CollectWheatSeedsTask extends ResourceTask {
  private final int _count;
  
  public CollectWheatSeedsTask(int count) {
    super(Items.WHEAT_SEEDS, count);
    this._count = count;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    if (mod.getBlockScanner().anyFound(new Block[] { Blocks.WHEAT }))
      return (Task)new CollectCropTask(Items.AIR, 999, Blocks.WHEAT, new Item[] { Items.WHEAT_SEEDS }); 
    return (Task)new MineAndCollectTask(Items.WHEAT_SEEDS, this._count, new Block[] { Blocks.GRASS, Blocks.TALL_GRASS }, MiningRequirement.HAND);
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectWheatSeedsTask;
  }
  
  protected String toDebugStringName() {
    return "Collecting " + this._count + " wheat seeds.";
  }
}
