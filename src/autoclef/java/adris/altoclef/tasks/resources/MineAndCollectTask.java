package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.ToolMaterialVer;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.AbstractDoToClosestObjectTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.CursorSlot;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolItem;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MineAndCollectTask extends ResourceTask {
  private final Block[] _blocksToMine;
  
  private final MiningRequirement _requirement;
  
  private final TimerGame _cursorStackTimer = new TimerGame(3.0D);
  
  private final MineOrCollectTask _subtask;
  
  public MineAndCollectTask(ItemTarget[] itemTargets, Block[] blocksToMine, MiningRequirement requirement) {
    super(itemTargets);
    this._requirement = requirement;
    this._blocksToMine = blocksToMine;
    this._subtask = new MineOrCollectTask(this._blocksToMine, itemTargets);
  }
  
  public MineAndCollectTask(ItemTarget[] blocksToMine, MiningRequirement requirement) {
    this(blocksToMine, itemTargetToBlockList(blocksToMine), requirement);
  }
  
  public MineAndCollectTask(ItemTarget target, Block[] blocksToMine, MiningRequirement requirement) {
    this(new ItemTarget[] { target }, blocksToMine, requirement);
  }
  
  public MineAndCollectTask(Item item, int count, Block[] blocksToMine, MiningRequirement requirement) {
    this(new ItemTarget(item, count), blocksToMine, requirement);
  }
  
  public static Block[] itemTargetToBlockList(ItemTarget[] targets) {
    List<Block> result = new ArrayList<>(targets.length);
    for (ItemTarget target : targets) {
      for (Item item : target.getMatches()) {
        Block block = Block.getBlockFromItem(item);
        if (block != null && !WorldHelper.isAir(block))
          result.add(block); 
      } 
    } 
    return (Block[])result.toArray(x$0 -> new Block[x$0]);
  }
  
  protected void onResourceStart(AltoClefController mod) {
    mod.getBehaviour().push();
    mod.getBehaviour().addProtectedItems(new Item[] { Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE });
    this._subtask.resetSearch();
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return true;
  }
  
  protected Task onResourceTick(AltoClefController mod) {
    if (!StorageHelper.miningRequirementMet(mod, this._requirement))
      return (Task)new SatisfyMiningRequirementTask(this._requirement); 
    if (this._subtask.isMining())
      makeSureToolIsEquipped(mod); 
    if (this._subtask.wasWandering() && isInWrongDimension(mod) && !mod.getBlockScanner().anyFound(this._blocksToMine))
      return getToCorrectDimensionTask(mod); 
    return (Task)this._subtask;
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    mod.getBehaviour().pop();
  }
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.resources.MineAndCollectTask) {
      adris.altoclef.tasks.resources.MineAndCollectTask task = (adris.altoclef.tasks.resources.MineAndCollectTask)other;
      return Arrays.equals(task._blocksToMine, this._blocksToMine);
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Mine And Collect";
  }
  
  private void makeSureToolIsEquipped(AltoClefController mod) {
    if (this._cursorStackTimer.elapsed() && !mod.getFoodChain().needsToEat()) {
      assert controller.getPlayer() != null;
      ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot(controller);
      if (cursorStack != null && !cursorStack.isEmpty()) {
        Item item = cursorStack.getItem();
        if (item.isSuitableFor(mod.getWorld().getBlockState(this._subtask.miningPos()))) {
          Item currentlyEquipped = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot(mod.getInventory())).getItem();
          if (item instanceof MiningToolItem)
            if (currentlyEquipped instanceof MiningToolItem) {
              MiningToolItem currentPick = (MiningToolItem)currentlyEquipped;
              MiningToolItem swapPick = (MiningToolItem)item;
              if (ToolMaterialVer.getMiningLevel((ToolItem)swapPick) > ToolMaterialVer.getMiningLevel((ToolItem)currentPick))
                mod.getSlotHandler().forceEquipSlot(controller, (Slot)CursorSlot.SLOT);
            } else {
              mod.getSlotHandler().forceEquipSlot(controller, (Slot)CursorSlot.SLOT);
            }  
        } 
      } 
      this._cursorStackTimer.reset();
    } 
  }

  public static class MineOrCollectTask extends AbstractDoToClosestObjectTask<Object> {

    private final Block[] _blocks;
    private final ItemTarget[] _targets;
    private final Set<BlockPos> blacklist = new HashSet<>();
    private final MovementProgressChecker progressChecker = new MovementProgressChecker();
    private final Task _pickupTask;
    private BlockPos miningPos;

    public MineOrCollectTask(Block[] blocks, ItemTarget[] targets) {
      _blocks = blocks;
      _targets = targets;
      _pickupTask = new PickupDroppedItemTask(_targets, true);
    }

    @Override
    protected Vec3d getPos(AltoClefController mod, Object obj) {
      if (obj instanceof BlockPos b) {
        return WorldHelper.toVec3d(b);
      }
      if (obj instanceof ItemEntity item) {
        return item.getPos();
      }
      throw new UnsupportedOperationException("Shouldn't try to get the position of object " + obj + " of type " + (obj != null ? obj.getClass().toString() : "(null object)"));
    }

    @Override
    protected Optional<Object> getClosestTo(AltoClefController mod, Vec3d pos) {
      Pair<Double, Optional<BlockPos>> closestBlock = getClosestBlock(mod,pos,  _blocks);
      Pair<Double, Optional<ItemEntity>> closestDrop = getClosestItemDrop(mod,pos,  _targets);

      double blockSq = closestBlock.getLeft();
      double dropSq = closestDrop.getLeft();

      // We can't mine right now.
      if (mod.getExtraBaritoneSettings().isInteractionPaused()) {
        return closestDrop.getRight().map(Object.class::cast);
      }

      if (dropSq <= blockSq) {
        return closestDrop.getRight().map(Object.class::cast);
      } else {
        return closestBlock.getRight().map(Object.class::cast);
      }
    }

    public static Pair<Double, Optional<ItemEntity>> getClosestItemDrop(AltoClefController mod,Vec3d pos, ItemTarget... items) {
      Optional<ItemEntity> closestDrop = Optional.empty();
      if (mod.getEntityTracker().itemDropped(items)) {
        closestDrop = mod.getEntityTracker().getClosestItemDrop(pos, items);
      }

      return new Pair<>(
              // + 5 to make the bot stop mining a bit less
              closestDrop.map(itemEntity -> itemEntity.squaredDistanceTo(pos) + 10).orElse(Double.POSITIVE_INFINITY),
              closestDrop
      );
    }

    public static Pair<Double,Optional<BlockPos> > getClosestBlock(AltoClefController mod,Vec3d pos ,Block... blocks) {
      Optional<BlockPos> closestBlock = mod.getBlockScanner().getNearestBlock(pos, check -> {

        if (mod.getBlockScanner().isUnreachable(check)) return false;
        return WorldHelper.canBreak(mod, check);
      }, blocks);

      return new Pair<>(
              closestBlock.map(blockPos -> BlockPosVer.getSquaredDistance(blockPos, pos)).orElse(Double.POSITIVE_INFINITY),
              closestBlock
      );
    }

    @Override
    protected Vec3d getOriginPos(AltoClefController mod) {
      return mod.getPlayer().getPos();
    }

    @Override
    protected Task onTick() {
      AltoClefController mod = controller;

      if (mod.getBaritone().getPathingBehavior().isPathing()) {
        progressChecker.reset();
      }
      if (miningPos != null && !progressChecker.check(mod)) {
        mod.getBaritone().getPathingBehavior().forceCancel();
        Debug.logMessage("Failed to mine block. Suggesting it may be unreachable.");
        mod.getBlockScanner().requestBlockUnreachable(miningPos, 2);
        blacklist.add(miningPos);
        miningPos = null;
        progressChecker.reset();
      }
      return super.onTick();
    }

    @Override
    protected Task getGoalTask(Object obj) {
      if (obj instanceof BlockPos newPos) {
        if (miningPos == null || !miningPos.equals(newPos)) {
          progressChecker.reset();
        }
        miningPos = newPos;
        return new DestroyBlockTask(miningPos);
      }
      if (obj instanceof ItemEntity) {
        miningPos = null;
        return _pickupTask;
      }
      throw new UnsupportedOperationException("Shouldn't try to get the goal from object " + obj + " of type " + (obj != null ? obj.getClass().toString() : "(null object)"));
    }

    @Override
    protected boolean isValid(AltoClefController mod, Object obj) {
      if (obj instanceof BlockPos b) {
        return mod.getBlockScanner().isBlockAtPosition(b, _blocks) && WorldHelper.canBreak(controller, b);
      }
      if (obj instanceof ItemEntity drop) {
        Item item = drop.getStack().getItem();
        if (_targets != null) {
          for (ItemTarget target : _targets) {
            if (target.matches(item)) return true;
          }
        }
        return false;
      }
      return false;
    }

    @Override
    protected void onStart() {
      progressChecker.reset();
      miningPos = null;
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
      if (other instanceof MineOrCollectTask task) {
        return Arrays.equals(task._blocks, _blocks) && Arrays.equals(task._targets, _targets);
      }
      return false;
    }

    @Override
    protected String toDebugString() {
      return "Mining or Collecting";
    }

    public boolean isMining() {
      return miningPos != null;
    }

    public BlockPos miningPos() {
      return miningPos;
    }
  }

}
