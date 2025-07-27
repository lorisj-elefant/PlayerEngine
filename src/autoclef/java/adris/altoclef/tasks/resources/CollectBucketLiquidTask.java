package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetCloseToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import java.util.HashSet;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;

public class CollectBucketLiquidTask extends ResourceTask {
  private final HashSet<BlockPos> blacklist = new HashSet<>();
  
  private final TimerGame tryImmediatePickupTimer = new TimerGame(3.0D);
  
  private final TimerGame pickedUpTimer = new TimerGame(0.5D);
  
  private final int count;
  
  private final Item target;
  
  private final Block toCollect;
  
  private final String liquidName;
  
  private final MovementProgressChecker progressChecker = new MovementProgressChecker();
  
  private boolean wasWandering = false;
  
  int tries;
  
  TimerGame timeoutTimer;
  
  public CollectBucketLiquidTask(String liquidName, Item filledBucket, int targetCount, Block toCollect) {
    super(filledBucket, targetCount);
    this.tries = 0;
    this.timeoutTimer = new TimerGame(2.0D);
    this.liquidName = liquidName;
    this.target = filledBucket;
    this.count = targetCount;
    this.toCollect = toCollect;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {
    mod.getBehaviour().push();
    mod.getBehaviour().setRayTracingFluidHandling(RaycastContext.FluidHandling.SOURCE_ONLY);
    mod.getBehaviour().avoidBlockBreaking(pos -> controller.getWorld().getBlockState(pos).getBlock() == this.toCollect);
    mod.getBehaviour().avoidBlockPlacing(pos -> controller.getWorld().getBlockState(pos).getBlock() == this.toCollect);
    (mod.getBaritoneSettings()).avoidUpdatingFallingBlocks.set(Boolean.TRUE);
    this.progressChecker.reset();
  }
  
  protected Task onTick() {
    Task result = super.onTick();
    if (!thisOrChildAreTimedOut())
      this.wasWandering = false; 
    return result;
  }
  
  protected Task onResourceTick(AltoClefController mod) {
    if (mod.getBaritone().getPathingBehavior().isPathing())
      this.progressChecker.reset(); 
    if (this.tryImmediatePickupTimer.elapsed() && !mod.getItemStorage().hasItem(new Item[] { Items.WATER_BUCKET })) {
      Block standingInside = mod.getWorld().getBlockState(mod.getPlayer().getBlockPos()).getBlock();
      if (standingInside == this.toCollect && WorldHelper.isSourceBlock(controller, mod.getPlayer().getBlockPos(), false)) {
        setDebugState("Trying to collect (we are in it)");
        mod.getInputControls().forceLook(0.0F, 90.0F);
        this.tryImmediatePickupTimer.reset();
        if (mod.getSlotHandler().forceEquipItem(Items.BUCKET)) {
          mod.getInputControls().tryPress(Input.CLICK_RIGHT);
          mod.getExtraBaritoneSettings().setInteractionPaused(true);
          this.pickedUpTimer.reset();
          this.progressChecker.reset();
        } 
        return null;
      } 
    } 
    if (!this.pickedUpTimer.elapsed()) {
      mod.getExtraBaritoneSettings().setInteractionPaused(false);
      this.progressChecker.reset();
      return null;
    } 
    int bucketsNeeded = this.count - mod.getItemStorage().getItemCount(new Item[] { Items.BUCKET }) - mod.getItemStorage().getItemCount(new Item[] { this.target });
    if (bucketsNeeded > 0) {
      setDebugState("Getting bucket...");
      return (Task)TaskCatalogue.getItemTask(Items.BUCKET, bucketsNeeded);
    } 
    Predicate<BlockPos> isSafeSourceLiquid = blockPos -> {
        if (this.blacklist.contains(blockPos))
          return false; 
        if (!WorldHelper.canReach(controller, blockPos))
          return false; 
        if (!WorldHelper.canReach(controller, blockPos.up()))
          return false; 
        assert controller.getWorld() != null;
        Block above = mod.getWorld().getBlockState(blockPos.up()).getBlock();
        if (above == Blocks.BEDROCK || above == Blocks.WATER)
          return false; 
        for (Direction direction : Direction.values()) {
          if (!direction.getAxis().isVertical())
            if (mod.getWorld().getBlockState(blockPos.up().offset(direction)).getBlock() == Blocks.WATER)
              return false;  
        } 
        return WorldHelper.isSourceBlock(controller, blockPos, false);
      };
    if (mod.getBlockScanner().anyFound(isSafeSourceLiquid, new Block[] { this.toCollect })) {
      setDebugState("Trying to collect...");
      return new DoToClosestBlockTask(blockPos -> {
        // Clear above if lava because we can't enter.
        // but NOT if we're standing right above.
        if (mod.getWorld().getBlockState(blockPos.up()).isSolid()) {
          if (!progressChecker.check(mod)) {
            mod.getBaritone().getPathingBehavior().cancelEverything();
            mod.getBaritone().getPathingBehavior().forceCancel();
            mod.getBaritone().getExploreProcess().onLostControl();
            mod.getBaritone().getCustomGoalProcess().onLostControl();
            Debug.logMessage("Failed to break, blacklisting.");
            mod.getBlockScanner().requestBlockUnreachable(blockPos);
            blacklist.add(blockPos);
          }
          return new DestroyBlockTask(blockPos.up());
        }

        if (tries > 75) {
          if (timeoutTimer.elapsed()) {
            tries = 0;
          }
          mod.log("trying to wander "+timeoutTimer.getDuration());
          return new TimeoutWanderTask();
        }
        timeoutTimer.reset();

        // We can reach the block.
        if (LookHelper.getReach(controller, blockPos).isPresent() &&
                mod.getBaritone().getPathingBehavior().isSafeToCancel()) {
          tries++;
          return new InteractWithBlockTask(new ItemTarget(Items.BUCKET, 1), blockPos, toCollect != Blocks.LAVA, new Vec3i(0, 1, 0));
        }
        // Get close enough.
        // up because if we go below we'll try to move next to the liquid (for lava, not a good move)
        if (this.thisOrChildAreTimedOut() && !wasWandering) {
          mod.getBlockScanner().requestBlockUnreachable(blockPos.up());
          wasWandering = true;
        }
        return new GetCloseToBlockTask(blockPos.up());
      }, isSafeSourceLiquid, toCollect);
    } 
    if (this.toCollect == Blocks.WATER && WorldHelper.getCurrentDimension(controller) == Dimension.NETHER)
      return (Task)new DefaultGoToDimensionTask(Dimension.OVERWORLD); 
    setDebugState("Searching for liquid by wandering around aimlessly");
    return (Task)new TimeoutWanderTask();
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    mod.getBehaviour().pop();
    mod.getExtraBaritoneSettings().setInteractionPaused(false);
    (mod.getBaritoneSettings()).avoidUpdatingFallingBlocks.set(Boolean.valueOf(false));
  }
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.resources.CollectBucketLiquidTask) {
      adris.altoclef.tasks.resources.CollectBucketLiquidTask task = (adris.altoclef.tasks.resources.CollectBucketLiquidTask)other;
      if (task.count != this.count)
        return false; 
      return (task.toCollect == this.toCollect);
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Collect " + this.count + " " + this.liquidName + " buckets";
  }

  public static class CollectWaterBucketTask extends CollectBucketLiquidTask {
    public CollectWaterBucketTask(int targetCount) {
      super("water", Items.WATER_BUCKET, targetCount, Blocks.WATER);
    }
  }

  public static class CollectLavaBucketTask extends CollectBucketLiquidTask {
    public CollectLavaBucketTask(int targetCount) {
      super("lava", Items.LAVA_BUCKET, targetCount, Blocks.LAVA);
    }
  }
}
