package adris.altoclef.tasks.resources;

import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.item.Items;

public class SatisfyMiningRequirementTask extends Task {
  private final MiningRequirement requirement;
  
  public SatisfyMiningRequirementTask(MiningRequirement requirement) {
    this.requirement = requirement;
  }
  
  protected void onStart() {}
  
  protected Task onTick() {
    switch (this.requirement) {
      case HAND:
        // Will never happen if you program this right
        break;
      case WOOD:
        return TaskCatalogue.getItemTask(Items.WOODEN_PICKAXE, 1);
      case STONE:
        return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 1);
      case IRON:
        return TaskCatalogue.getItemTask(Items.IRON_PICKAXE, 1);
      case DIAMOND:
        return TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
    } 
    return null;
  }
  
  protected void onStop(Task interruptTask) {}
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.resources.SatisfyMiningRequirementTask) {
      adris.altoclef.tasks.resources.SatisfyMiningRequirementTask task = (adris.altoclef.tasks.resources.SatisfyMiningRequirementTask)other;
      return (task.requirement == this.requirement);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Satisfy Mining Req: " + String.valueOf(this.requirement);
  }
  
  public boolean isFinished() {
    return StorageHelper.miningRequirementMetInventory(controller, this.requirement);
  }
}
