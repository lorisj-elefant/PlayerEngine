package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.resources.KillAndLootTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;

public class HeroTask extends Task {
  protected void onStart() {}
  
  protected Task onTick() {
    AltoClefController mod = controller;
    if (mod.getFoodChain().needsToEat()) {
      setDebugState("Eat first.");
      return null;
    } 
    Optional<Entity> experienceOrb = mod.getEntityTracker().getClosestEntity(new Class[] { ExperienceOrbEntity.class });
    if (experienceOrb.isPresent()) {
      setDebugState("Getting experience.");
      return (Task)new GetToEntityTask(experienceOrb.get());
    } 
    assert controller.getWorld() != null;
    Iterable<Entity> hostiles = controller.getWorld().iterateEntities();
    if (hostiles != null)
      for (Entity hostile : hostiles) {
        if (hostile instanceof net.minecraft.entity.mob.HostileEntity || hostile instanceof net.minecraft.entity.mob.SlimeEntity) {
          Optional<Entity> closestHostile = mod.getEntityTracker().getClosestEntity(new Class[] { hostile.getClass() });
          if (closestHostile.isPresent()) {
            setDebugState("Killing hostiles or picking hostile drops.");
            return (Task)new KillAndLootTask(hostile.getClass(), new ItemTarget[] { new ItemTarget(ItemHelper.HOSTILE_MOB_DROPS) });
          } 
        } 
      }  
    if (mod.getEntityTracker().itemDropped(ItemHelper.HOSTILE_MOB_DROPS)) {
      setDebugState("Picking hostile drops.");
      return (Task)new PickupDroppedItemTask(new ItemTarget(ItemHelper.HOSTILE_MOB_DROPS), true);
    } 
    setDebugState("Searching for hostile mobs.");
    return (Task)new TimeoutWanderTask();
  }
  
  protected void onStop(Task interruptTask) {}
  
  protected boolean isEqual(Task other) {
    return other instanceof adris.altoclef.tasks.entity.HeroTask;
  }
  
  protected String toDebugString() {
    return "Killing all hostile mobs.";
  }
}
