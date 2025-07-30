package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.control.InputControls;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.Goal;
import baritone.api.utils.input.Input;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public abstract class CustomBaritoneGoalTask extends Task implements ITaskRequiresGrounded {
  private final Task wanderTask = (Task)new TimeoutWanderTask(5.0F, true);
  
  private final MovementProgressChecker stuckCheck = new MovementProgressChecker();
  
  private final boolean wander;
  
  protected MovementProgressChecker checker = new MovementProgressChecker();
  
  protected Goal cachedGoal = null;
  
  Block[] annoyingBlocks = new Block[] { 
      Blocks.VINE, Blocks.NETHER_SPROUTS, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WEEPING_VINES_PLANT, Blocks.LADDER, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM,
      Blocks.SMALL_DRIPLEAF, Blocks.TALL_GRASS, Blocks.GRASS, Blocks.SWEET_BERRY_BUSH };
  
  private Task unstuckTask = null;
  
  public CustomBaritoneGoalTask(boolean wander) {
    this.wander = wander;
  }
  
  public CustomBaritoneGoalTask() {
    this(true);
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
    this.checker.reset();
    this.stuckCheck.reset();
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
    InputControls controls = mod.getInputControls();
    if (mod.getBaritone().getPathingBehavior().isPathing())
      this.checker.reset(); 
    if (WorldHelper.isInNetherPortal(controller)) {
      if (!mod.getBaritone().getPathingBehavior().isPathing()) {
        setDebugState("Getting out from nether portal");
        controls.hold(Input.SNEAK);
        controls.hold(Input.MOVE_FORWARD);
        return null;
      } 
      controls.release(Input.SNEAK);
      controls.release(Input.MOVE_BACK);
      controls.release(Input.MOVE_FORWARD);
    } else if (mod.getBaritone().getPathingBehavior().isPathing()) {
      controls.release(Input.SNEAK);
      controls.release(Input.MOVE_BACK);
      controls.release(Input.MOVE_FORWARD);
    } 
    if (this.unstuckTask != null && this.unstuckTask.isActive() && !this.unstuckTask.isFinished() && stuckInBlock(mod) != null) {
      setDebugState("Getting unstuck from block.");
      this.stuckCheck.reset();
      mod.getBaritone().getCustomGoalProcess().onLostControl();
      mod.getBaritone().getExploreProcess().onLostControl();
      return this.unstuckTask;
    } 
    if (!this.checker.check(mod) || !this.stuckCheck.check(mod)) {
      BlockPos blockStuck = stuckInBlock(mod);
      if (blockStuck != null) {
        this.unstuckTask = getFenceUnstuckTask();
        return this.unstuckTask;
      }
      if(stuckCheck.lastBreakingBlock!=null){
        controller.getBaritone().getPathingBehavior().forceCancel();
      }
      this.stuckCheck.reset();
    } 
    if (this.cachedGoal == null)
      this.cachedGoal = newGoal(mod); 
    if (this.wander)
      if (isFinished()) {
        this.checker.reset();
      } else {
        if (this.wanderTask.isActive() && !this.wanderTask.isFinished()) {
          setDebugState("Wandering...");
          this.checker.reset();
          return this.wanderTask;
        } 
        if (!this.checker.check(mod)) {
          Debug.logMessage("Failed to make progress on goal, wandering.");
          onWander(mod);
          return this.wanderTask;
        } 
      }  
    if (!mod.getBaritone().getCustomGoalProcess().isActive() && mod
      .getBaritone().getPathingBehavior().isSafeToCancel())
      mod.getBaritone().getCustomGoalProcess().setGoalAndPath(this.cachedGoal); 
    setDebugState("Completing goal.");
    return null;
  }
  
  public boolean isFinished() {
    if (this.cachedGoal == null)
      this.cachedGoal = newGoal(controller);
    return (this.cachedGoal != null && this.cachedGoal.isInGoal(controller.getPlayer().getBlockPos()));
  }
  
  protected void onStop(Task interruptTask) {
    controller.getBaritone().getPathingBehavior().forceCancel();
  }
  
  protected abstract Goal newGoal(AltoClefController paramAltoClefController);
  
  protected void onWander(AltoClefController mod) {}
}
