package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;

public class KillAndLootTask extends ResourceTask {
  private final Class<?> _toKill;
  
  private final Task _killTask;
  
  public KillAndLootTask(Class<?> toKill, Predicate<Entity> shouldKill, ItemTarget... itemTargets) {
    super((ItemTarget[])itemTargets.clone());
    this._toKill = toKill;
    this._killTask = (Task)new KillEntitiesTask(shouldKill, new Class[] { this._toKill });
  }
  
  public KillAndLootTask(Class<?> toKill, ItemTarget... itemTargets) {
    super((ItemTarget[])itemTargets.clone());
    this._toKill = toKill;
    this._killTask = (Task)new KillEntitiesTask(new Class[] { this._toKill });
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    if (!mod.getEntityTracker().entityFound(new Class[] { this._toKill })) {
      if (isInWrongDimension(mod)) {
        setDebugState("Going to correct dimension.");
        return getToCorrectDimensionTask(mod);
      } 
      setDebugState("Searching for mob...");
      return (Task)new TimeoutWanderTask();
    } 
    return this._killTask;
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.resources.KillAndLootTask) {
      adris.altoclef.tasks.resources.KillAndLootTask task = (adris.altoclef.tasks.resources.KillAndLootTask)other;
      return task._toKill.equals(this._toKill);
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Collect items from " + this._toKill.toGenericString();
  }
}
