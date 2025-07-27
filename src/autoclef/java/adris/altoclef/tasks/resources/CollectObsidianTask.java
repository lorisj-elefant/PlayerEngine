package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PlaceObsidianBucketTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.world.RaycastContext;

public class CollectObsidianTask extends ResourceTask {
  private final TimerGame _placeWaterTimeout = new TimerGame(6.0D);
  
  private final MovementProgressChecker _lavaTimeout = new MovementProgressChecker();
  
  private final Set<BlockPos> _lavaBlacklist = new HashSet<>();
  
  private final int _count;
  
  private Task _forceCompleteTask = null;
  
  private BlockPos _lavaWaitCurrentPos;
  
  private PlaceObsidianBucketTask _placeObsidianTask;
  
  public CollectObsidianTask(int count) {
    super(Items.OBSIDIAN, count);
    this._count = count;
  }
  
  private static BlockPos getLavaStructurePos(BlockPos lavaPos) {
    return lavaPos.add(1, 1, 0);
  }
  
  private static BlockPos getLavaWaterPos(BlockPos lavaPos) {
    return lavaPos.up();
  }
  
  private static BlockPos getGoodObsidianPosition(AltoClefController mod) {
    BlockPos start = mod.getPlayer().getBlockPos().add(-3, -3, -3);
    BlockPos end = mod.getPlayer().getBlockPos().add(3, 3, 3);
    for (BlockPos pos : WorldHelper.scanRegion(start, end)) {
      if (!WorldHelper.canBreak(mod, pos) || !WorldHelper.canPlace(mod, pos))
        return null; 
    } 
    return mod.getPlayer().getBlockPos();
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {
    mod.getBehaviour().push();
    mod.getBehaviour().setRayTracingFluidHandling(RaycastContext.FluidHandling.SOURCE_ONLY);
    mod.getBehaviour().avoidBlockPlacing(pos -> (this._lavaWaitCurrentPos != null) ? (
        
        (pos.equals(this._lavaWaitCurrentPos) || pos.equals(getLavaWaterPos(this._lavaWaitCurrentPos)))) : false);
    mod.getBehaviour().avoidBlockBreaking(pos -> (this._lavaWaitCurrentPos != null) ? pos.equals(getLavaStructurePos(this._lavaWaitCurrentPos)) : false);
  }
  
  protected Task onResourceTick(AltoClefController mod) {
    if (this._lavaWaitCurrentPos != null && mod.getChunkTracker().isChunkLoaded(this._lavaWaitCurrentPos) && mod.getWorld().getBlockState(this._lavaWaitCurrentPos).getBlock() != Blocks.LAVA)
      this._lavaWaitCurrentPos = null; 
    if (!StorageHelper.miningRequirementMet(controller, MiningRequirement.DIAMOND)) {
      setDebugState("Getting diamond pickaxe first");
      return (Task)new SatisfyMiningRequirementTask(MiningRequirement.DIAMOND);
    } 
    if (this._forceCompleteTask != null && this._forceCompleteTask.isActive() && !this._forceCompleteTask.isFinished())
      return this._forceCompleteTask; 
    Predicate<BlockPos> goodObsidian = blockPos -> 
      (blockPos.isCenterWithinDistance((Position)mod.getPlayer().getPos(), 800.0D) && WorldHelper.canBreak(mod, blockPos));
    if (mod.getBlockScanner().anyFound(goodObsidian, new Block[] { Blocks.OBSIDIAN }) || mod.getEntityTracker().itemDropped(new Item[] { Items.OBSIDIAN })) {
      setDebugState("Mining/Collecting obsidian");
      this._placeObsidianTask = null;
      return (Task)new MineAndCollectTask(new ItemTarget(Items.OBSIDIAN, this._count), new Block[] { Blocks.OBSIDIAN }, MiningRequirement.DIAMOND);
    } 
    if (WorldHelper.getCurrentDimension(mod) == Dimension.NETHER) {
      double AVERAGE_GOLD_PER_OBSIDIAN = 11.475D;
      int gold_buffer = (int)(11.475D * this._count);
      setDebugState("We can't place water, so we're trading for obsidian");
      return (Task)new TradeWithPiglinsTask(gold_buffer, Items.OBSIDIAN, this._count);
    } 
    if (this._placeObsidianTask == null) {
      BlockPos goodPos = getGoodObsidianPosition(mod);
      if (goodPos != null) {
        this._placeObsidianTask = new PlaceObsidianBucketTask(goodPos);
      } else {
        setDebugState("Walking until we find a spot to place obsidian");
        return (Task)new TimeoutWanderTask();
      } 
    } 
    if (this._placeObsidianTask != null && !mod.getItemStorage().hasItem(new Item[] { Items.LAVA_BUCKET }))
      if (!this._placeObsidianTask.getPos().isCenterWithinDistance((Position)mod.getPlayer().getPos(), 4.0D)) {
        BlockPos goodPos = getGoodObsidianPosition(mod);
        if (goodPos != null) {
          Debug.logMessage("(nudged obsidian target closer)");
          this._placeObsidianTask = new PlaceObsidianBucketTask(goodPos);
        } 
      }  
    setDebugState("Placing Obsidian");
    return (Task)this._placeObsidianTask;
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    mod.getBehaviour().pop();
  }
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.resources.CollectObsidianTask) {
      adris.altoclef.tasks.resources.CollectObsidianTask task = (adris.altoclef.tasks.resources.CollectObsidianTask)other;
      return (task._count == this._count);
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Collect " + this._count + " blocks of obsidian";
  }
}
