package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.SmeltInFurnaceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.SmeltTarget;
import net.minecraft.item.Items;

public class CollectIronIngotTask extends ResourceTask {
  private final int count;
  
  public CollectIronIngotTask(int count) {
    super(Items.IRON_INGOT, count);
    this.count = count;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {
    mod.getBehaviour().push();
  }
  
  protected Task onResourceTick(AltoClefController mod) {
    return (Task)new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.IRON_INGOT, this.count), new ItemTarget(Items.RAW_IRON, this.count), new net.minecraft.item.Item[0]));
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    mod.getBehaviour().pop();
  }

  @Override
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof CollectIronIngotTask same && same.count == count;
  }
  
  protected String toDebugStringName() {
    return "Collecting " + this.count + " iron.";
  }
}
