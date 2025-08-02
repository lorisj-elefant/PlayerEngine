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
  
  private final MovementProgressChecker progressChecker = new MovementProgressChecker();
  
  private final BlockPos pos;
  
  private BlockPos currentCastTarget;
  
  private BlockPos currentDestroyTarget;
  
  public PlaceObsidianBucketTask(BlockPos pos) {
    this .pos = pos;
  }
  
  protected void onStart() {
    BotBehaviour botBehaviour = controller.getBehaviour();
    botBehaviour.push();
    botBehaviour.avoidBlockBreaking(this::isBlockInCastFrame);
    botBehaviour.avoidBlockPlacing(this::isBlockInCastWaterOrLava);
    this .progressChecker.reset();
    Debug.logInternal("Started onStart method");
    Debug.logInternal("Behaviour pushed");
    Debug.logInternal("Avoiding block breaking");
    Debug.logInternal("Avoiding block placing");
    Debug.logInternal("Progress checker reset");
  }
  
  private boolean isBlockInCastFrame(BlockPos block) {
    Objects.requireNonNull(this .pos);
    Objects.requireNonNull(block);
    return Arrays.<Vec3i>stream(CAST_FRAME).map(this .pos::add).anyMatch(block::equals);
  }
  
  private boolean isBlockInCastWaterOrLava(BlockPos blockPos) {
    BlockPos waterTarget = this .pos.up();
    Debug.logInternal("blockPos: " + String.valueOf(blockPos));
    Debug.logInternal("waterTarget: " + String.valueOf(waterTarget));
    return (blockPos.equals(this .pos) || blockPos.equals(waterTarget));
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
    if (mod.getBaritone().getPathingBehavior().isPathing())
      this .progressChecker.reset(); 
    if (mod.getBlockScanner().isBlockAtPosition(this .pos, new Block[] { Blocks.OBSIDIAN }) && mod.getBlockScanner().isBlockAtPosition(this .pos.up(), new Block[] { Blocks.WATER }))
      return (Task)new ClearLiquidTask(this .pos.up()); 
    if (!mod.getItemStorage().hasItem(new Item[] { Items.WATER_BUCKET })) {
      this .progressChecker.reset();
      return (Task)TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
    } 
    if (!mod.getItemStorage().hasItem(new Item[] { Items.LAVA_BUCKET }))
      if (!mod.getBlockScanner().isBlockAtPosition(this .pos, new Block[] { Blocks.LAVA })) {
        this .progressChecker.reset();
        return (Task)TaskCatalogue.getItemTask(Items.LAVA_BUCKET, 1);
      }  
    if (!this .progressChecker.check(mod)) {
      mod.getBaritone().getPathingBehavior().forceCancel();
      mod.getBlockScanner().requestBlockUnreachable(this .pos);
      this .progressChecker.reset();
      return (Task)new TimeoutWanderTask(5.0F);
    } 
    if (this .currentCastTarget != null)
      if (WorldHelper.isSolidBlock(controller, this .currentCastTarget)) {
        this .currentCastTarget = null;
      } else {
        return (Task)new PlaceBlockTask(this .currentCastTarget, 
            (Block[])Arrays.<Block>stream(ItemHelper.itemsToBlocks(mod.getModSettings().getThrowawayItems(mod))).filter(b -> !Arrays.<Block>stream(ItemHelper.itemsToBlocks(ItemHelper.LEAVES)).toList().contains(b)).toArray(x$0 -> new Block[x$0]));
      }  
    if (this .currentDestroyTarget != null)
      if (!WorldHelper.isSolidBlock(controller, this .currentDestroyTarget)) {
        this .currentDestroyTarget = null;
      } else {
        return (Task)new DestroyBlockTask(this .currentDestroyTarget);
      }  
    if (this .currentCastTarget != null && WorldHelper.isSolidBlock(controller, this .currentCastTarget))
      this .currentCastTarget = null; 
    for (Vec3i castPosRelative : CAST_FRAME) {
      BlockPos castPos = this .pos.add(castPosRelative);
      if (!WorldHelper.isSolidBlock(controller, castPos)) {
        this .currentCastTarget = castPos;
        Debug.logInternal("Building cast frame...");
        return null;
      } 
    } 
    if (mod.getWorld().getBlockState(this .pos).getBlock() != Blocks.LAVA) {
      BlockPos targetPos = this .pos.add(-1, 1, 0);
      if (!mod.getPlayer().getBlockPos().equals(targetPos) && mod.getItemStorage().hasItem(new Item[] { Items.LAVA_BUCKET })) {
        Debug.logInternal("Positioning player before placing lava...");
        return (Task)new GetToBlockTask(targetPos, false);
      } 
      if (WorldHelper.isSolidBlock(controller, this .pos)) {
        Debug.logInternal("Clearing space around lava...");
        this .currentDestroyTarget = this .pos;
        return null;
      } 
      if (WorldHelper.isSolidBlock(controller, this .pos.up())) {
        Debug.logInternal("Clearing space around lava...");
        this .currentDestroyTarget = this .pos.up();
        return null;
      } 
      if (WorldHelper.isSolidBlock(controller, this .pos.up(2))) {
        Debug.logInternal("Clearing space around lava...");
        this .currentDestroyTarget = this .pos.up(2);
        return null;
      } 
      Debug.logInternal("Placing lava for cast...");
      return (Task)new InteractWithBlockTask(new ItemTarget(Items.LAVA_BUCKET, 1), Direction.WEST, this .pos.add(1, 0, 0), false);
    } 
    BlockPos waterCheck = this .pos.up();
    if (mod.getWorld().getBlockState(waterCheck).getBlock() != Blocks.WATER) {
      Debug.logInternal("Placing water for cast...");
      BlockPos targetPos = this .pos.add(-1, 1, 0);
      if (!mod.getPlayer().getBlockPos().equals(targetPos) && mod.getItemStorage().hasItem(new Item[] { Items.WATER_BUCKET })) {
        Debug.logInternal("Positioning player before placing water...");
        return (Task)new GetToBlockTask(targetPos, false);
      } 
      if (WorldHelper.isSolidBlock(controller, waterCheck)) {
        this .currentDestroyTarget = waterCheck;
        return null;
      } 
      if (WorldHelper.isSolidBlock(controller, waterCheck.up())) {
        this .currentDestroyTarget = waterCheck.up();
        return null;
      } 
      return (Task)new InteractWithBlockTask(new ItemTarget(Items.WATER_BUCKET, 1), Direction.WEST, this .pos.add(1, 1, 0), true);
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
    BlockPos pos = this .pos;
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
    return "Placing obsidian at " + String.valueOf(this .pos) + " with a cast";
  }
  
  public BlockPos getPos() {
    Debug.logInternal("Entering getPos()");
    return this .pos;
  }
}
