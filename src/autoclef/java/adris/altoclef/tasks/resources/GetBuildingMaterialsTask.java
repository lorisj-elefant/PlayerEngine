package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.item.Item;

public class GetBuildingMaterialsTask extends Task {
  private final int _count;
  
  public GetBuildingMaterialsTask(int count) {
    this._count = count;
  }
  
  protected void onStart() {}
  
  protected Task onTick() {
    Item[] throwaways = controller.getModSettings().getThrowawayItems(controller, true);
    return (Task)new MineAndCollectTask(new ItemTarget[] { new ItemTarget(throwaways, this._count) }, MiningRequirement.WOOD);
  }
  
  protected void onStop(Task interruptTask) {}
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.resources.GetBuildingMaterialsTask) {
      adris.altoclef.tasks.resources.GetBuildingMaterialsTask task = (adris.altoclef.tasks.resources.GetBuildingMaterialsTask)other;
      return (task._count == this._count);
    } 
    return false;
  }
  
  public boolean isFinished() {
    return (StorageHelper.getBuildingMaterialCount(controller) >= this._count);
  }
  
  protected String toDebugString() {
    return "Collecting " + this._count + " building materials.";
  }
}
