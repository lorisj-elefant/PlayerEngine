package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class CollectFuelTask extends Task {
  private final double targetFuel;
  
  public CollectFuelTask(double targetFuel) {
    this.targetFuel = targetFuel;
  }
  
  protected void onStart() {}

  @Override
  protected Task onTick() {

    switch (WorldHelper.getCurrentDimension(controller)) {
      case OVERWORLD -> {
        // Just collect coal for now.
        setDebugState("Collecting coal.");
        return TaskCatalogue.getItemTask(Items.COAL, (int) Math.ceil(targetFuel / 8));
      }
      case END -> {
        setDebugState("Going to overworld, since, well, no more fuel can be found here.");
        return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
      }
      case NETHER -> {
        setDebugState("Going to overworld, since we COULD use wood but wood confuses the bot. A bug at the moment.");
        return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
      }
    }
    setDebugState("INVALID DIMENSION: " + WorldHelper.getCurrentDimension(controller));
    return null;
  }
  
  protected void onStop(Task interruptTask) {}
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.resources.CollectFuelTask) {
      adris.altoclef.tasks.resources.CollectFuelTask task = (adris.altoclef.tasks.resources.CollectFuelTask)other;
      return (Math.abs(task.targetFuel - this.targetFuel) < 0.01D);
    } 
    return false;
  }
  
  public boolean isFinished() {
    return (controller.getItemStorage().getItemCountInventoryOnly(new Item[] { Items.COAL }) >= this.targetFuel);
  }
  
  protected String toDebugString() {
    return "Collect Fuel: x" + this.targetFuel;
  }
}
