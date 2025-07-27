package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalFollowEntity;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.Goal;
import baritone.api.utils.input.Input;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class GetToEntityTask extends Task implements ITaskRequiresGrounded {
  private final MovementProgressChecker stuckCheck = new MovementProgressChecker();
  
  private final MovementProgressChecker _progress = new MovementProgressChecker();
  
  private final TimeoutWanderTask _wanderTask = new TimeoutWanderTask(5.0F);
  
  private final Entity _entity;
  
  private final double _closeEnoughDistance;
  
  Block[] annoyingBlocks = new Block[] { 
      Blocks.VINE, Blocks.NETHER_SPROUTS, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WEEPING_VINES_PLANT, Blocks.LADDER, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM,
      Blocks.SMALL_DRIPLEAF, Blocks.TALL_GRASS, Blocks.GRASS, Blocks.SWEET_BERRY_BUSH };
  
  private Task _unstuckTask = null;
  
  public GetToEntityTask(Entity entity, double closeEnoughDistance) {
    this._entity = entity;
    this._closeEnoughDistance = closeEnoughDistance;
  }
  
  public GetToEntityTask(Entity entity) {
    this(entity, 1.0D);
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
    if (this.annoyingBlocks != null) {
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
    } 
    return false;
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
    controller.getBaritone().getPathingBehavior().forceCancel();
    this._progress.reset();
    this.stuckCheck.reset();
    this._wanderTask.resetWander();
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
    if (mod.getBaritone().getPathingBehavior().isPathing())
      this._progress.reset(); 
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
    if (!this._progress.check(mod) || !this.stuckCheck.check(mod)) {
      BlockPos blockStuck = stuckInBlock(mod);
      if (blockStuck != null) {
        this._unstuckTask = getFenceUnstuckTask();
        return this._unstuckTask;
      } 
      this.stuckCheck.reset();
    } 
    if (this._wanderTask.isActive() && !this._wanderTask.isFinished()) {
      this._progress.reset();
      setDebugState("Failed to get to target, wandering for a bit.");
      return (Task)this._wanderTask;
    } 
    if (!mod.getBaritone().getCustomGoalProcess().isActive())
      mod.getBaritone().getCustomGoalProcess().setGoalAndPath((Goal)new GoalFollowEntity(this._entity, this._closeEnoughDistance)); 
    if (mod.getPlayer().isInRange(this._entity, this._closeEnoughDistance))
      this._progress.reset(); 
    if (!this._progress.check(mod))
      return (Task)this._wanderTask; 
    setDebugState("Going to entity");
    return null;
  }
  
  protected void onStop(Task interruptTask) {
    controller.getBaritone().getPathingBehavior().forceCancel();
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.GetToEntityTask) {
      adris.altoclef.tasks.movement.GetToEntityTask task = (adris.altoclef.tasks.movement.GetToEntityTask)other;
      return (task._entity.equals(this._entity) && Math.abs(task._closeEnoughDistance - this._closeEnoughDistance) < 0.1D);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Approach entity " + this._entity.getType().getTranslationKey();
  }
}
