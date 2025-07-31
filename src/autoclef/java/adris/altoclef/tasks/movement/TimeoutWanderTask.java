package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

public class TimeoutWanderTask extends Task implements ITaskRequiresGrounded {
  private final MovementProgressChecker stuckCheck = new MovementProgressChecker();
  
  private final float distanceToWander;
  
  private final MovementProgressChecker progressChecker = new MovementProgressChecker();
  
  private final boolean increaseRange;
  
  private final TimerGame timer = new TimerGame(60.0D);
  
  Block[] annoyingBlocks = new Block[] { 
      Blocks.VINE, Blocks.NETHER_SPROUTS, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WEEPING_VINES_PLANT, Blocks.LADDER, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM,
      Blocks.SMALL_DRIPLEAF, Blocks.TALL_GRASS, Blocks.GRASS, Blocks.SWEET_BERRY_BUSH };
  
  private Vec3d origin;
  
  private boolean _forceExplore;
  
  private Task _unstuckTask = null;
  
  private int failCounter;
  
  private double _wanderDistanceExtension;
  
  public TimeoutWanderTask(float distanceToWander, boolean increaseRange) {
    this.distanceToWander = distanceToWander;
    this.increaseRange = increaseRange;
    this._forceExplore = false;
  }
  
  public TimeoutWanderTask(float distanceToWander) {
    this(distanceToWander, false);
  }
  
  public TimeoutWanderTask() {
    this(Float.POSITIVE_INFINITY, false);
  }
  
  public TimeoutWanderTask(boolean forceExplore) {
    this();
    this._forceExplore = forceExplore;
  }
  
  private static BlockPos[] generateSides(BlockPos pos) {
    return new BlockPos[] { pos
        .add(1, 0, 0), pos
        .add(-1, 0, 0), pos
        .add(0, 0, 1), pos
        .add(0, 0, -1), pos
        .add(1, 0, -1), pos
        .add(1, 0, 1), pos
        .add(-1, 0, -1), pos
        .add(-1, 0, 1) };
  }
  
  private boolean isAnnoying(AltoClefController mod, BlockPos pos) {
    Block[] arrayOfBlock = this.annoyingBlocks;
    int i = arrayOfBlock.length;
    byte b = 0;
    if (b < i) {
      Block AnnoyingBlocks = arrayOfBlock[b];
      return (mod.getWorld().getBlockState(pos).getBlock() == AnnoyingBlocks || mod
        .getWorld().getBlockState(pos).getBlock() instanceof net.minecraft.block.DoorBlock || mod
        .getWorld().getBlockState(pos).getBlock() instanceof net.minecraft.block.FenceBlock || mod
        .getWorld().getBlockState(pos).getBlock() instanceof net.minecraft.block.FenceGateBlock || mod
        .getWorld().getBlockState(pos).getBlock() instanceof net.minecraft.block.FlowerBlock);
    } 
    return false;
  }
  
  public void resetWander() {
    this._wanderDistanceExtension = 0.0D;
  }
  
  private BlockPos stuckInBlock(AltoClefController mod) {
    BlockPos p = mod.getPlayer().getBlockPos();
    if (isAnnoying(mod, p))
      return p; 
    if (isAnnoying(mod, p.up()))
      return p.up(); 
    BlockPos[] toCheck = generateSides(p);
    for (BlockPos check : toCheck) {
      if (isAnnoying(mod, check))
        return check; 
    } 
    BlockPos[] toCheckHigh = generateSides(p.up());
    for (BlockPos check : toCheckHigh) {
      if (isAnnoying(mod, check))
        return check; 
    } 
    return null;
  }
  
  private Task getFenceUnstuckTask() {
    return (Task)new SafeRandomShimmyTask();
  }
  
  protected void onStart() {
    AltoClefController mod = controller;
    this.timer.reset();
    mod.getBaritone().getPathingBehavior().forceCancel();
    this.origin = mod.getPlayer().getPos();
    this.progressChecker.reset();
    this.stuckCheck.reset();
    this.failCounter = 0;
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
      this.progressChecker.reset(); 
    if (WorldHelper.isInNetherPortal(controller)) {
      if (!mod.getBaritone().getPathingBehavior().isPathing()) {
        setDebugState("Getting out from nether portal");
        mod.getInputControls().hold(Input.SNEAK);
        mod.getInputControls().hold(Input.MOVE_FORWARD);
        return null;
      } 
      mod.getInputControls().release(Input.SNEAK);
      mod.getInputControls().release(Input.MOVE_BACK);
      mod.getInputControls().release(Input.MOVE_FORWARD);
    } else if (mod.getBaritone().getPathingBehavior().isPathing()) {
      mod.getInputControls().release(Input.SNEAK);
      mod.getInputControls().release(Input.MOVE_BACK);
      mod.getInputControls().release(Input.MOVE_FORWARD);
    } 
    if (this._unstuckTask != null && this._unstuckTask.isActive() && !this._unstuckTask.isFinished() && stuckInBlock(mod) != null) {
      setDebugState("Getting unstuck from block.");
      this.stuckCheck.reset();
      mod.getBaritone().getCustomGoalProcess().onLostControl();
      mod.getBaritone().getExploreProcess().onLostControl();
      return this._unstuckTask;
    } 
    if (!this.progressChecker.check(mod) || !this.stuckCheck.check(mod)) {
      List<Entity> closeEntities = mod.getEntityTracker().getCloseEntities();
      for (Entity CloseEntities : closeEntities) {
        if (CloseEntities instanceof net.minecraft.entity.mob.MobEntity && CloseEntities
          .getPos().isInRange((Position)mod.getPlayer().getPos(), 1.0D) && (CloseEntities!=mod.getEntity())) {
          setDebugState("Killing annoying entity.");
          return (Task)new KillEntitiesTask(new Class[] { CloseEntities.getClass() });
        } 
      } 
      BlockPos blockStuck = stuckInBlock(mod);
      if (blockStuck != null) {
        this.failCounter++;
        this._unstuckTask = getFenceUnstuckTask();
        return this._unstuckTask;
      } 
      this.stuckCheck.reset();
    } 
    setDebugState("Exploring.");
    switch (WorldHelper.getCurrentDimension(controller)) {
      case END -> {
        if (timer.getDuration() >= 30) {
          timer.reset();
        }
      }
      case OVERWORLD, NETHER -> {
        if (timer.getDuration() >= 30) {
        }
        if (timer.elapsed()) {
          timer.reset();
        }
      }
    }
    if (!mod.getBaritone().getExploreProcess().isActive())
      mod.getBaritone().getExploreProcess().explore((int)this.origin.getX(), (int)this.origin.getZ()); 
    if (!this.progressChecker.check(mod)) {
      this.progressChecker.reset();
      if (!this._forceExplore) {
        this.failCounter++;
        Debug.logMessage("Failed exploring.");
        if(progressChecker.lastBreakingBlock!=null){

        }
      } 
    } 
    return null;
  }
  
  protected void onStop(Task interruptTask) {
    controller.getBaritone().getPathingBehavior().forceCancel();
    if (isFinished() && 
      this.increaseRange) {
      this._wanderDistanceExtension += this.distanceToWander;
      Debug.logMessage("Increased wander range");
    } 
  }
  
  public boolean isFinished() {
    if (Float.isInfinite(this.distanceToWander))
      return false; 
    if (this.failCounter > 10)
      return true; 
    LivingEntity player = controller.getPlayer();
    if (player != null && player.getPos() != null && (player.isOnGround() || player
      .isTouchingWater())) {
      double sqDist = player.getPos().squaredDistanceTo(this.origin);
      double toWander = this.distanceToWander + this._wanderDistanceExtension;
      return (sqDist > toWander * toWander);
    } 
    return false;
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.TimeoutWanderTask) {
      adris.altoclef.tasks.movement.TimeoutWanderTask task = (adris.altoclef.tasks.movement.TimeoutWanderTask)other;
      if (Float.isInfinite(task.distanceToWander) || Float.isInfinite(this.distanceToWander))
        return (Float.isInfinite(task.distanceToWander) == Float.isInfinite(this.distanceToWander)); 
      return (Math.abs(task.distanceToWander - this.distanceToWander) < 0.5F);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Wander for " + this.distanceToWander + this._wanderDistanceExtension + " blocks";
  }
}
