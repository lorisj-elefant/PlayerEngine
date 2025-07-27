package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.Items;

public class CollectEggsTask extends ResourceTask {
  private final int _count;
  
  private final DoToClosestEntityTask _waitNearChickens;
  
  private AltoClefController _mod;
  
  public CollectEggsTask(int targetCount) {
    super(Items.EGG, targetCount);
    this._count = targetCount;
    this._waitNearChickens = new DoToClosestEntityTask(chicken -> new GetToEntityTask(chicken, 5.0D), new Class[] { ChickenEntity.class });
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {
    this._mod = mod;
  }
  
  protected Task onResourceTick(AltoClefController mod) {
    if (this._waitNearChickens.wasWandering() && WorldHelper.getCurrentDimension(controller) != Dimension.OVERWORLD) {
      setDebugState("Going to right dimension.");
      return (Task)new DefaultGoToDimensionTask(Dimension.OVERWORLD);
    } 
    setDebugState("Waiting around chickens. Yes.");
    return (Task)this._waitNearChickens;
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectEggsTask;
  }
  
  protected String toDebugStringName() {
    return "Collecting " + this._count + " eggs.";
  }
}
