package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClefController;
import adris.altoclef.BotBehaviour;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.FollowPlayerTask;
import adris.altoclef.tasks.movement.RunAwayFromPositionTask;
import adris.altoclef.tasks.slot.ThrowCursorTask;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

public class GiveItemToPlayerTask extends Task {
  private final String playerName;
  
  private final ItemTarget[] targets;
  
  private final CataloguedResourceTask resourceTask;
  
  private final List<ItemTarget> throwTarget = new ArrayList<>();
  
  private boolean droppingItems;
  
  private Task throwTask;
  
  private TimerGame _throwTimeout = new TimerGame(0.4D);
  
  public GiveItemToPlayerTask(String player, ItemTarget... targets) {
    this.playerName = player;
    this.targets = targets;
    this.resourceTask = TaskCatalogue.getSquashedItemTask(targets);
  }
  
  protected void onStart() {
    this.droppingItems = false;
    this.throwTarget.clear();
    BotBehaviour botBehaviour = controller.getBehaviour();
    botBehaviour.push();
    botBehaviour.addProtectedItems(ItemTarget.getMatches(this.targets));
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
    if (this.throwTask != null && this.throwTask.isActive() && !this.throwTask.isFinished()) {
      setDebugState("Throwing items");
      return this.throwTask;
    } 
    Optional<Vec3d> lastPos = mod.getEntityTracker().getPlayerMostRecentPosition(this.playerName);
    if (lastPos.isEmpty()) {
      String nearbyUsernames = String.join(",", mod.getEntityTracker().getAllLoadedPlayerUsernames());
      fail("No user in render distance found with username \"" + this.playerName + "\". Maybe this was a typo or there is a user with a similar name around? Nearby users: [" + nearbyUsernames + "].");
      return null;
    } 
    Vec3d targetPos = ((Vec3d)lastPos.get()).add(0.0D, 0.20000000298023224D, 0.0D);
    if (this.droppingItems) {
      setDebugState("Throwing items");
      if (!this._throwTimeout.elapsed())
        return null; 
      this._throwTimeout.reset();
      LookHelper.lookAt(mod, targetPos);
      for (int i = 0; i < this.throwTarget.size(); i++) {
        ItemTarget target = this.throwTarget.get(i);
        if (target.getTargetCount() > 0) {
          Optional<Slot> has = mod.getItemStorage().getSlotsWithItemPlayerInventory(false, target.getMatches()).stream().findFirst();
          if (has.isPresent()) {
            Slot currentlyPresent = has.get();
            System.out.println("Currently present: " + String.valueOf(currentlyPresent));
            if (Slot.isCursor(currentlyPresent)) {
              ItemStack stack = StorageHelper.getItemStackInSlot(currentlyPresent);
              target = new ItemTarget(target, target.getTargetCount() - stack.getCount());
              this.throwTarget.set(i, target);
              Debug.logMessage("THROWING: " + String.valueOf(has.get()));
              return (Task)new ThrowCursorTask();
            } 
            mod.getSlotHandler().clickSlot(currentlyPresent, 0, SlotActionType.PICKUP);
            return null;
          } 
        } 
      } 
      this._throwTimeout.forceElapse();
      if (!targetPos.isInRange((Position)mod.getPlayer().getPos(), 4.0D)) {
        mod.log("Finished giving items.");
        stop();
        return null;
      } 
      return (Task)new RunAwayFromPositionTask(6.0D, new BlockPos[] { WorldHelper.toBlockPos(targetPos) });
    } 
    if (!StorageHelper.itemTargetsMet(mod, this.targets)) {
      setDebugState("Collecting resources...");
      return (Task)this.resourceTask;
    } 
    if (targetPos.isInRange((Position)mod.getPlayer().getPos(), 4.0D)) {
      if (!mod.getEntityTracker().isPlayerLoaded(this.playerName)) {
        String nearbyUsernames = String.join(",", mod.getEntityTracker().getAllLoadedPlayerUsernames());
        fail("Failed to get to player \"" + this.playerName + "\". We moved to where we last saw them but now have no idea where they are. Nearby players: [" + nearbyUsernames + "]");
        return null;
      } 
      PlayerEntity p = mod.getEntityTracker().getPlayerEntity(this.playerName).get();
      if (p.getBlockPos().getY() <= mod.getPlayer().getBlockPos().getY() || p.getPos().distanceTo(mod.getPlayer().getPos()) <= 0.5D)
        if (LookHelper.seesPlayer((Entity)p, (Entity)mod.getPlayer(), 6.0D)) {
          this.droppingItems = true;
          this.throwTarget.addAll(Arrays.asList(this.targets));
          this._throwTimeout.reset();
        }  
    } 
    setDebugState("Going to player...");
    return (Task)new FollowPlayerTask(this.playerName, 0.5D);
  }
  
  protected void onStop(Task interruptTask) {
    controller.getBehaviour().pop();
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.entity.GiveItemToPlayerTask) {
      adris.altoclef.tasks.entity.GiveItemToPlayerTask task = (adris.altoclef.tasks.entity.GiveItemToPlayerTask)other;
      if (!task.playerName.equals(this.playerName))
        return false; 
      return Arrays.equals(task.targets, this.targets);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Giving items to " + this.playerName;
  }
}
