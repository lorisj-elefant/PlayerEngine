package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClefController;
import adris.altoclef.BotBehaviour;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commands.BlockScanner;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public class PlaceObsidianBucketTask extends Task {
  public static final Vec3i[] CAST_FRAME = new Vec3i[] { new Vec3i(0, -1, 0), new Vec3i(0, -1, -1), new Vec3i(0, -1, 1), new Vec3i(-1, -1, 0), new Vec3i(1, -1, 0), new Vec3i(0, 0, -1), new Vec3i(0, 0, 1), new Vec3i(-1, 0, 0), new Vec3i(1, 0, 0), new Vec3i(1, 1, 0) };
  
  private final MovementProgressChecker _progressChecker = new MovementProgressChecker();
  
  private final BlockPos _pos;
  
  private BlockPos _currentCastTarget;
  
  private BlockPos _currentDestroyTarget;
  
  public PlaceObsidianBucketTask(BlockPos pos) {
    this._pos = pos;
  }
  
  protected void onStart() {
    BotBehaviour botBehaviour = controller.getBehaviour();
    botBehaviour.push();
    botBehaviour.avoidBlockBreaking(this::isBlockInCastFrame);
    botBehaviour.avoidBlockPlacing(this::isBlockInCastWaterOrLava);
    this._progressChecker.reset();
    Debug.logInternal("Started onStart method");
    Debug.logInternal("Behaviour pushed");
    Debug.logInternal("Avoiding block breaking");
    Debug.logInternal("Avoiding block placing");
    Debug.logInternal("Progress checker reset");
  }
  
  private boolean isBlockInCastFrame(BlockPos block) {
    Objects.requireNonNull(this._pos);
    Objects.requireNonNull(block);
    return Arrays.<Vec3i>stream(CAST_FRAME).map(this._pos::add).anyMatch(block::equals);
  }
  
  private boolean isBlockInCastWaterOrLava(BlockPos blockPos) {
    BlockPos waterTarget = this._pos.up();
    Debug.logInternal("blockPos: " + String.valueOf(blockPos));
    Debug.logInternal("waterTarget: " + String.valueOf(waterTarget));
    return (blockPos.equals(this._pos) || blockPos.equals(waterTarget));
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
    if (mod.getBaritone().getPathingBehavior().isPathing())
      this._progressChecker.reset(); 
    if (mod.getBlockScanner().isBlockAtPosition(this._pos, new Block[] { Blocks.OBSIDIAN }) && mod.getBlockScanner().isBlockAtPosition(this._pos.up(), new Block[] { Blocks.WATER }))
      return (Task)new ClearLiquidTask(this._pos.up()); 
    if (!mod.getItemStorage().hasItem(new Item[] { Items.WATER_BUCKET })) {
      this._progressChecker.reset();
      return (Task)TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
    } 
    if (!mod.getItemStorage().hasItem(new Item[] { Items.LAVA_BUCKET }))
      if (!mod.getBlockScanner().isBlockAtPosition(this._pos, new Block[] { Blocks.LAVA })) {
        this._progressChecker.reset();
        return (Task)TaskCatalogue.getItemTask(Items.LAVA_BUCKET, 1);
      }  
    if (!this._progressChecker.check(mod)) {
      mod.getBaritone().getPathingBehavior().forceCancel();
      mod.getBlockScanner().requestBlockUnreachable(this._pos);
      this._progressChecker.reset();
      return (Task)new TimeoutWanderTask(5.0F);
    } 
    if (this._currentCastTarget != null)
      if (WorldHelper.isSolidBlock(controller, this._currentCastTarget)) {
        this._currentCastTarget = null;
      } else {
        return (Task)new PlaceBlockTask(this._currentCastTarget, 
            (Block[])Arrays.<Block>stream(ItemHelper.itemsToBlocks(mod.getModSettings().getThrowawayItems(mod))).filter(b -> !Arrays.<Block>stream(ItemHelper.itemsToBlocks(ItemHelper.LEAVES)).toList().contains(b)).toArray(x$0 -> new Block[x$0]));
      }  
    if (this._currentDestroyTarget != null)
      if (!WorldHelper.isSolidBlock(controller, this._currentDestroyTarget)) {
        this._currentDestroyTarget = null;
      } else {
        return (Task)new DestroyBlockTask(this._currentDestroyTarget);
      }  
    if (this._currentCastTarget != null && WorldHelper.isSolidBlock(controller, this._currentCastTarget))
      this._currentCastTarget = null; 
    for (Vec3i castPosRelative : CAST_FRAME) {
      BlockPos castPos = this._pos.add(castPosRelative);
      if (!WorldHelper.isSolidBlock(controller, castPos)) {
        this._currentCastTarget = castPos;
        Debug.logInternal("Building cast frame...");
        return null;
      } 
    } 
    if (mod.getWorld().getBlockState(this._pos).getBlock() != Blocks.LAVA) {
      BlockPos targetPos = this._pos.add(-1, 1, 0);
      if (!mod.getPlayer().getBlockPos().equals(targetPos) && mod.getItemStorage().hasItem(new Item[] { Items.LAVA_BUCKET })) {
        Debug.logInternal("Positioning player before placing lava...");
        return (Task)new GetToBlockTask(targetPos, false);
      } 
      if (WorldHelper.isSolidBlock(controller, this._pos)) {
        Debug.logInternal("Clearing space around lava...");
        this._currentDestroyTarget = this._pos;
        return null;
      } 
      if (WorldHelper.isSolidBlock(controller, this._pos.up())) {
        Debug.logInternal("Clearing space around lava...");
        this._currentDestroyTarget = this._pos.up();
        return null;
      } 
      if (WorldHelper.isSolidBlock(controller, this._pos.up(2))) {
        Debug.logInternal("Clearing space around lava...");
        this._currentDestroyTarget = this._pos.up(2);
        return null;
      } 
      Debug.logInternal("Placing lava for cast...");
      return (Task)new InteractWithBlockTask(new ItemTarget(Items.LAVA_BUCKET, 1), Direction.WEST, this._pos.add(1, 0, 0), false);
    } 
    BlockPos waterCheck = this._pos.up();
    if (mod.getWorld().getBlockState(waterCheck).getBlock() != Blocks.WATER) {
      Debug.logInternal("Placing water for cast...");
      BlockPos targetPos = this._pos.add(-1, 1, 0);
      if (!mod.getPlayer().getBlockPos().equals(targetPos) && mod.getItemStorage().hasItem(new Item[] { Items.WATER_BUCKET })) {
        Debug.logInternal("Positioning player before placing water...");
        return (Task)new GetToBlockTask(targetPos, false);
      } 
      if (WorldHelper.isSolidBlock(controller, waterCheck)) {
        this._currentDestroyTarget = waterCheck;
        return null;
      } 
      if (WorldHelper.isSolidBlock(controller, waterCheck.up())) {
        this._currentDestroyTarget = waterCheck.up();
        return null;
      } 
      return (Task)new InteractWithBlockTask(new ItemTarget(Items.WATER_BUCKET, 1), Direction.WEST, this._pos.add(1, 1, 0), true);
    } 
    return null;
  }
  
  protected void onStop(Task interruptTask) {
    if (controller.getBehaviour() != null) {
      controller.getBehaviour().pop();
      Debug.logInternal("Behaviour popped.");
    } 
  }
  
  public boolean isFinished() {
    BlockScanner blockTracker = controller.getBlockScanner();
    BlockPos pos = this._pos;
    boolean isObsidian = blockTracker.isBlockAtPosition(pos, new Block[] { Blocks.OBSIDIAN });
    Debug.logInternal("isObsidian: " + isObsidian);
    boolean isNotWaterAbove = !blockTracker.isBlockAtPosition(pos.up(), new Block[] { Blocks.WATER });
    Debug.logInternal("isNotWaterAbove: " + isNotWaterAbove);
    boolean isFinished = (isObsidian && isNotWaterAbove);
    Debug.logInternal("isFinished: " + isFinished);
    return isFinished;
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.construction.PlaceObsidianBucketTask) {
      adris.altoclef.tasks.construction.PlaceObsidianBucketTask task = (adris.altoclef.tasks.construction.PlaceObsidianBucketTask)other;
      boolean isEqual = task.getPos().equals(getPos());
      Debug.logInternal("isEqual: " + isEqual);
      return isEqual;
    } 
    Debug.logInternal("isEqual: false");
    return false;
  }
  
  protected String toDebugString() {
    return "Placing obsidian at " + String.valueOf(this._pos) + " with a cast";
  }
  
  public BlockPos getPos() {
    Debug.logInternal("Entering getPos()");
    return this._pos;
  }
}
