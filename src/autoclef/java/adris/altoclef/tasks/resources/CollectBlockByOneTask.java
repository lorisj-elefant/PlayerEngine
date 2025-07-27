package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.MiningRequirement;
import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class CollectBlockByOneTask extends ResourceTask {
  private final Item item;
  
  private final Block[] blocks;
  
  private final MiningRequirement requirement;
  
  private final int count;
  
  public CollectBlockByOneTask(Item item, Block[] blocks, MiningRequirement requirement, int targetCount) {
    super(item, targetCount);
    this.item = item;
    this.blocks = blocks;
    this.requirement = requirement;
    this.count = targetCount;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    return (Task)new MineAndCollectTask(this.item, 1, this.blocks, this.requirement);
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.resources.CollectBlockByOneTask) {
      adris.altoclef.tasks.resources.CollectBlockByOneTask task = (adris.altoclef.tasks.resources.CollectBlockByOneTask)other;
      return (task.count == this.count && task.item.equals(this.item) && Arrays.<Block>stream(task.blocks).allMatch(block -> Arrays.<Block>stream(this.blocks).toList().contains(block)));
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Collect " + String.valueOf(this.item);
  }

  public static class CollectCobblestoneTask extends CollectBlockByOneTask {

    public CollectCobblestoneTask(int targetCount) {
      super(Items.COBBLESTONE, new Block[]{Blocks.STONE, Blocks.COBBLESTONE}, MiningRequirement.WOOD, targetCount);
    }
  }

  public static class CollectCobbledDeepslateTask extends CollectBlockByOneTask {

    public CollectCobbledDeepslateTask(int targetCount) {
      super(Items.COBBLED_DEEPSLATE, new Block[]{Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE}, MiningRequirement.WOOD, targetCount);
    }
  }

  public static class CollectEndStoneTask extends CollectBlockByOneTask {

    public CollectEndStoneTask(int targetCount) {
      super(Items.END_STONE, new Block[]{Blocks.END_STONE}, MiningRequirement.WOOD, targetCount);
    }
  }
}
