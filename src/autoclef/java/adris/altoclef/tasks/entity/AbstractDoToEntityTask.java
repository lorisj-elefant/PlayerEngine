package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.Slot;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalRunAway;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractDoToEntityTask extends Task implements ITaskRequiresGrounded {
  protected final MovementProgressChecker progress = new MovementProgressChecker();
  
  private final double maintainDistance;
  
  private final double combatGuardLowerRange;
  
  private final double combatGuardLowerFieldRadius;
  
  private TimeoutWanderTask wanderTask;
  
  protected AbstractDoToEntityTask(double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
    this.maintainDistance = maintainDistance;
    this.combatGuardLowerRange = combatGuardLowerRange;
    this.combatGuardLowerFieldRadius = combatGuardLowerFieldRadius;
  }
  
  protected AbstractDoToEntityTask(double maintainDistance) {
    this(maintainDistance, 0.0D, Double.POSITIVE_INFINITY);
  }
  
  protected AbstractDoToEntityTask(double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
    this(-1.0D, combatGuardLowerRange, combatGuardLowerFieldRadius);
  }
  
  protected void onStart() {
    AltoClefController mod = controller;
    this.progress.reset();
    ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot(controller);
    if (!cursorStack.isEmpty()) {
      Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
      moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
      if (ItemHelper.canThrowAwayStack(mod, cursorStack))
        mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP); 
      Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
      garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
      mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
    }
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
    if (mod.getBaritone().getPathingBehavior().isPathing())
      this.progress.reset(); 
    Optional<Entity> checkEntity = getEntityTarget(mod);
    if (checkEntity.isEmpty()) {
      mod.getMobDefenseChain().resetTargetEntity();
      mod.getMobDefenseChain().resetForceField();
    } else {
      mod.getMobDefenseChain().setTargetEntity(checkEntity.get());
    } 
    if (checkEntity.isPresent()) {
      Entity entity = checkEntity.get();
      double playerReach = mod.getModSettings().getEntityReachRange();
      EntityHitResult result = LookHelper.raycast((Entity)mod.getPlayer(), entity, playerReach);
      double sqDist = entity.squaredDistanceTo((Entity)mod.getPlayer());
      if (sqDist < this.combatGuardLowerRange * this.combatGuardLowerRange) {
        mod.getMobDefenseChain().setForceFieldRange(this.combatGuardLowerFieldRadius);
      } else {
        mod.getMobDefenseChain().resetForceField();
      } 
      double maintainDistance = (this.maintainDistance >= 0.0D) ? this.maintainDistance : (playerReach - 1.0D);
      boolean tooClose = (sqDist < maintainDistance * maintainDistance);
      if (tooClose && !mod.getBaritone().getCustomGoalProcess().isActive())
        mod.getBaritone().getCustomGoalProcess().setGoalAndPath((Goal)new GoalRunAway(maintainDistance, new BlockPos[] { entity.getBlockPos() })); 
      if (mod.getControllerExtras().inRange(entity) && result != null && result
        .getType() == HitResult.Type.ENTITY && !mod.getFoodChain().needsToEat() && 
        !mod.getMLGBucketChain().isFalling(mod) && mod.getMLGBucketChain().doneMLG() && 
        !mod.getMLGBucketChain().isChorusFruiting() && mod
        .getBaritone().getPathingBehavior().isSafeToCancel() && mod
        .getPlayer().isOnGround()) {
        this.progress.reset();
        return onEntityInteract(mod, entity);
      } 
      if (!tooClose) {
        setDebugState("Approaching target");
        if (!this.progress.check(mod)) {
          this.progress.reset();
          Debug.logMessage("Failed to get to target, blacklisting.");
          mod.getEntityTracker().requestEntityUnreachable(entity);
        } 
        return (Task)new GetToEntityTask(entity, maintainDistance);
      } 
    } 
    if (BeatMinecraftTask.isTaskRunning(mod, (Task)this.wanderTask))
      return (Task)this.wanderTask; 
    if (!mod.getBaritone().getPathingBehavior().isSafeToCancel())
      return null; 
    this.wanderTask = new TimeoutWanderTask();
    return (Task)this.wanderTask;
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.entity.AbstractDoToEntityTask) {
      adris.altoclef.tasks.entity.AbstractDoToEntityTask task = (adris.altoclef.tasks.entity.AbstractDoToEntityTask)other;
      if (!doubleCheck(task.maintainDistance, this.maintainDistance))
        return false; 
      if (!doubleCheck(task.combatGuardLowerFieldRadius, this.combatGuardLowerFieldRadius))
        return false; 
      if (!doubleCheck(task.combatGuardLowerRange, this.combatGuardLowerRange))
        return false; 
      return isSubEqual(task);
    } 
    return false;
  }
  
  private boolean doubleCheck(double a, double b) {
    if (Double.isInfinite(a) == Double.isInfinite(b))
      return true; 
    return (Math.abs(a - b) < 0.1D);
  }
  
  protected void onStop(Task interruptTask) {
    AltoClefController mod = controller;
    mod.getMobDefenseChain().setTargetEntity(null);
    mod.getMobDefenseChain().resetForceField();
  }
  
  protected abstract boolean isSubEqual(adris.altoclef.tasks.entity.AbstractDoToEntityTask paramAbstractDoToEntityTask);
  
  protected abstract Task onEntityInteract(AltoClefController paramAltoClefController, Entity paramEntity);
  
  protected abstract Optional<Entity> getEntityTarget(AltoClefController paramAltoClefController);
}
