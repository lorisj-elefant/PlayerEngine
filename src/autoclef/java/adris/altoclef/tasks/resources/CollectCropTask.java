package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StlHelper;
import adris.altoclef.util.helpers.WorldHelper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class CollectCropTask extends ResourceTask {
  private final ItemTarget cropToCollect;
  
  private final Item[] cropSeed;
  
  private final Predicate<BlockPos> canBreak;
  
  private final Block[] cropBlock;
  
  private final Set<BlockPos> emptyCropland = new HashSet<>();
  
  private final Task collectSeedTask;
  
  private final HashSet<BlockPos> wasFullyGrown = new HashSet<>();
  
  public CollectCropTask(ItemTarget cropToCollect, Block[] cropBlock, Item[] cropSeed, Predicate<BlockPos> canBreak) {
    super(cropToCollect);
    this .cropToCollect = cropToCollect;
    this .cropSeed = cropSeed;
    this .canBreak = canBreak;
    this .cropBlock = cropBlock;
    this .collectSeedTask = (Task)new PickupDroppedItemTask(new ItemTarget(cropSeed, 1), true);
  }
  
  public CollectCropTask(ItemTarget cropToCollect, Block[] cropBlock, Item... cropSeed) {
    this(cropToCollect, cropBlock, cropSeed, canBreak -> true);
  }
  
  public CollectCropTask(ItemTarget cropToCollect, Block cropBlock, Item... cropSeed) {
    this(cropToCollect, new Block[] { cropBlock }, cropSeed);
  }
  
  public CollectCropTask(Item cropItem, int count, Block cropBlock, Item... cropSeed) {
    this(new ItemTarget(cropItem, count), cropBlock, cropSeed);
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    if (hasEmptyCrops(mod) && mod.getModSettings().shouldReplantCrops() && !mod.getItemStorage().hasItem(this .cropSeed)) {
      if (this .collectSeedTask.isActive() && !this .collectSeedTask.isFinished()) {
        setDebugState("Picking up dropped seeds");
        return this .collectSeedTask;
      } 
      if (mod.getEntityTracker().itemDropped(this .cropSeed)) {
        Optional<ItemEntity> closest = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), this .cropSeed);
        if (closest.isPresent() && ((ItemEntity)closest.get()).isInRange((Entity)mod.getPlayer(), 7.0D))
          return this .collectSeedTask; 
      } 
    } 
    if (shouldReplantNow(mod)) {
      setDebugState("Replanting...");
      this .emptyCropland.removeIf(blockPos -> !isEmptyCrop(mod, blockPos));
      assert !this .emptyCropland.isEmpty();
      Objects.requireNonNull(this .emptyCropland);
      return new DoToClosestBlockTask(
              blockPos -> new InteractWithBlockTask(new ItemTarget(cropSeed, 1), Direction.UP, blockPos.down(), true),
              pos -> emptyCropland.stream().min(StlHelper.compareValues(block -> BlockPosVer.getSquaredDistance(block,pos))),
              emptyCropland::contains,
              Blocks.FARMLAND); // Blocks.FARMLAND is useless to be put here
      }
    Predicate<BlockPos> validCrop = blockPos -> !this .canBreak.test(blockPos) ? false : (
      
      (mod.getModSettings().shouldReplantCrops() && !isMature(mod, blockPos)) ? false : ((mod.getWorld().getBlockState(blockPos).getBlock() == Blocks.WHEAT) ? isMature(mod, blockPos) : true));
    if (isInWrongDimension(mod) && !mod.getBlockScanner().anyFound(validCrop, this .cropBlock))
      return getToCorrectDimensionTask(mod); 
    setDebugState("Breaking crops.");
    return new DoToClosestBlockTask(
            blockPos -> {
              emptyCropland.add(blockPos);
              return new DestroyBlockTask(blockPos);
            },
            validCrop,
            cropBlock
    );
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  public boolean isFinished() {
    if (shouldReplantNow(controller))
      return false; 
    return super.isFinished();
  }
  
  private boolean shouldReplantNow(AltoClefController mod) {
    return (mod.getModSettings().shouldReplantCrops() && hasEmptyCrops(mod) && mod.getItemStorage().hasItem(this .cropSeed));
  }
  
  private boolean hasEmptyCrops(AltoClefController mod) {
    for (BlockPos pos : this .emptyCropland) {
      if (isEmptyCrop(mod, pos))
        return true; 
    } 
    return false;
  }
  
  private boolean isEmptyCrop(AltoClefController mod, BlockPos pos) {
    return WorldHelper.isAir(mod.getWorld().getBlockState(pos).getBlock());
  }
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.resources.CollectCropTask) {
      adris.altoclef.tasks.resources.CollectCropTask task = (adris.altoclef.tasks.resources.CollectCropTask)other;
      return (Arrays.equals(task .cropSeed, this .cropSeed) && Arrays.equals(task .cropBlock, this .cropBlock) && task .cropToCollect.equals(this .cropToCollect));
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Collecting crops: " + String.valueOf(this .cropToCollect);
  }
  
  private boolean isMature(AltoClefController mod, BlockPos blockPos) {
    if (!mod.getChunkTracker().isChunkLoaded(blockPos) || !WorldHelper.canReach(controller, blockPos))
      return this .wasFullyGrown.contains(blockPos); 
    BlockState s = mod.getWorld().getBlockState(blockPos);
    Block block = s.getBlock();
    if (block instanceof CropBlock) {
      CropBlock crop = (CropBlock)block;
      boolean mature = crop.isMature(s);
      if (this .wasFullyGrown.contains(blockPos)) {
        if (!mature)
          this .wasFullyGrown.remove(blockPos); 
      } else if (mature) {
        this .wasFullyGrown.add(blockPos);
      } 
      return mature;
    } 
    return false;
  }
}
