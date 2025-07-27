package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.construction.compound.ConstructNetherPortalBucketTask;
import adris.altoclef.tasks.construction.compound.ConstructNetherPortalObsidianTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class EnterNetherPortalTask extends Task {
  private final Task getPortalTask;
  
  private final Dimension targetDimension;
  
  private final TimerGame portalTimeout = new TimerGame(10.0D);
  
  private final TimeoutWanderTask wanderTask = new TimeoutWanderTask(5.0F);
  
  private final Predicate<BlockPos> goodPortal;
  
  private boolean leftPortal;
  
  public EnterNetherPortalTask(Task getPortalTask, Dimension targetDimension, Predicate<BlockPos> goodPortal) {
    if (targetDimension == Dimension.END)
      throw new IllegalArgumentException("Can't build a nether portal to the end."); 
    this.getPortalTask = getPortalTask;
    this.targetDimension = targetDimension;
    this.goodPortal = goodPortal;
  }
  
  public EnterNetherPortalTask(Dimension targetDimension, Predicate<BlockPos> goodPortal) {
    this(null, targetDimension, goodPortal);
  }
  
  public EnterNetherPortalTask(Task getPortalTask, Dimension targetDimension) {
    this(getPortalTask, targetDimension, blockPos -> true);
  }
  
  public EnterNetherPortalTask(Dimension targetDimension) {
    this(null, targetDimension);
  }
  
  protected void onStart() {
    this.leftPortal = false;
    this.portalTimeout.reset();
    this.wanderTask.resetWander();
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
    if (this.wanderTask.isActive() && !this.wanderTask.isFinished()) {
      setDebugState("Exiting portal for a bit.");
      this.portalTimeout.reset();
      this.leftPortal = true;
      return (Task)this.wanderTask;
    } 
    if (mod.getWorld().getBlockState(mod.getPlayer().getBlockPos()).getBlock() == Blocks.NETHER_PORTAL) {
      if (this.portalTimeout.elapsed() && !this.leftPortal)
        return (Task)this.wanderTask; 
      setDebugState("Waiting inside portal");
      mod.getBaritone().getExploreProcess().onLostControl();
      mod.getBaritone().getCustomGoalProcess().onLostControl();
      mod.getBaritone().getMineProcess().onLostControl();
      mod.getBaritone().getFarmProcess().onLostControl();
      mod.getBaritone().getGetToBlockProcess();
      mod.getBaritone().getBuilderProcess();
      mod.getBaritone().getFollowProcess();
      mod.getInputControls().release(Input.SNEAK);
      mod.getInputControls().release(Input.MOVE_BACK);
      mod.getInputControls().release(Input.MOVE_FORWARD);
      return null;
    } 
    this.portalTimeout.reset();
    Predicate<BlockPos> standablePortal = blockPos -> {
        if (mod.getWorld().getBlockState(blockPos).getBlock() == Blocks.NETHER_PORTAL)
          return this.goodPortal.test(blockPos); 
        if (!mod.getChunkTracker().isChunkLoaded(blockPos))
          return this.goodPortal.test(blockPos); 
        BlockPos below = blockPos.down();
        boolean canStand = (WorldHelper.isSolidBlock(controller, below) && !mod.getBlockScanner().isBlockAtPosition(below, new Block[] { Blocks.NETHER_PORTAL }));
        return (canStand && this.goodPortal.test(blockPos));
      };
    if (mod.getBlockScanner().anyFound(standablePortal, new Block[] { Blocks.NETHER_PORTAL })) {
      setDebugState("Going to found portal");
      return (Task)new DoToClosestBlockTask(blockPos -> new GetToBlockTask(blockPos, false), standablePortal, new Block[] { Blocks.NETHER_PORTAL });
    } 
    if (!mod.getBlockScanner().anyFound(standablePortal, new Block[] { Blocks.NETHER_PORTAL })) {
      setDebugState("Making new nether portal.");
      if (WorldHelper.getCurrentDimension(controller) == Dimension.OVERWORLD)
        return (Task)new ConstructNetherPortalBucketTask(); 
      return (Task)new ConstructNetherPortalObsidianTask();
    } 
    setDebugState("Getting our portal");
    return this.getPortalTask;
  }
  
  protected void onStop(Task interruptTask) {}
  
  public boolean isFinished() {
    return (WorldHelper.getCurrentDimension(controller) == this.targetDimension);
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.EnterNetherPortalTask) {
      adris.altoclef.tasks.movement.EnterNetherPortalTask task = (adris.altoclef.tasks.movement.EnterNetherPortalTask)other;
      return (Objects.equals(task.getPortalTask, this.getPortalTask) && Objects.equals(task.targetDimension, this.targetDimension));
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Entering nether portal";
  }
}
