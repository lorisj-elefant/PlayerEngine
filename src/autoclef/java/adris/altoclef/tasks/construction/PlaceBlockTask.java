package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.schematic.AbstractSchematic;
import baritone.api.schematic.ISchematic;
import baritone.api.utils.BlockOptionalMeta;
import baritone.api.utils.input.Input;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.ArrayUtils;

public class PlaceBlockTask extends Task implements ITaskRequiresGrounded {
  private static final int MIN_MATERIALS = 1;
  
  private static final int PREFERRED_MATERIALS = 32;
  
  private final BlockPos target;
  
  private final Block[] toPlace;
  
  private final boolean useThrowaways;
  
  private final boolean autoCollectStructureBlocks;
  
  private final MovementProgressChecker progressChecker = new MovementProgressChecker();
  
  private final TimeoutWanderTask wanderTask = new TimeoutWanderTask(5.0F);
  
  private Task materialTask;
  
  private int failCount = 0;
  
  public PlaceBlockTask(BlockPos target, Block[] toPlace, boolean useThrowaways, boolean autoCollectStructureBlocks) {
    this.target = target;
    this.toPlace = toPlace;
    this.useThrowaways = useThrowaways;
    this.autoCollectStructureBlocks = autoCollectStructureBlocks;
  }
  
  public PlaceBlockTask(BlockPos target, Block... toPlace) {
    this(target, toPlace, false, false);
  }
  
  public int getMaterialCount(AltoClefController mod) {
    int count = mod.getItemStorage().getItemCount(ItemHelper.blocksToItems(this.toPlace));
    if (this.useThrowaways)
      count += mod.getItemStorage().getItemCount((Item[])((List)(mod.getBaritoneSettings()).acceptableThrowawayItems.get()).toArray(new Item[0]));
    return count;
  }
  
  public static Task getMaterialTask(int count) {
    return (Task)TaskCatalogue.getSquashedItemTask(new ItemTarget[] { new ItemTarget(Items.DIRT, count), new ItemTarget(Items.COBBLESTONE, count), new ItemTarget(Items.NETHERRACK, count), new ItemTarget(Items.COBBLED_DEEPSLATE, count) });
  }
  
  protected void onStart() {
    this.progressChecker.reset();
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
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
    if (this.wanderTask.isActive() && !this.wanderTask.isFinished()) {
      setDebugState("Wandering.");
      this.progressChecker.reset();
      return (Task)this.wanderTask;
    } 
    if (this.autoCollectStructureBlocks) {
      if (this.materialTask != null && this.materialTask.isActive() && !this.materialTask.isFinished()) {
        setDebugState("No structure items, collecting cobblestone + dirt as default.");
        if (getMaterialCount(mod) < 32)
          return this.materialTask; 
        this.materialTask = null;
      } 
      if (getMaterialCount(mod) < 1) {
        this.materialTask = getMaterialTask(32);
        this.progressChecker.reset();
        return this.materialTask;
      } 
    } 
    if (!this.progressChecker.check(mod)) {
      this.failCount++;
      if (!tryingAlternativeWay()) {
        Debug.logMessage("Failed to place, wandering timeout.");
        return (Task)this.wanderTask;
      } 
      Debug.logMessage("Trying alternative way of placing block...");
    } 
    if (tryingAlternativeWay()) {
      setDebugState("Alternative way: Trying to go above block to place block.");
      return (Task)new GetToBlockTask(this.target.up(), false);
    } 
    setDebugState("Letting baritone place a block.");
    if (!mod.getBaritone().getBuilderProcess().isActive()) {
      Debug.logInternal("Run Structure Build");
      PlaceStructureSchematic placeStructureSchematic = new PlaceStructureSchematic(mod);
      mod.getBaritone().getBuilderProcess().build("structure", (ISchematic)placeStructureSchematic, (Vec3i)this.target);
    } 
    return null;
  }
  
  protected void onStop(Task interruptTask) {
    controller.getBaritone().getBuilderProcess().onLostControl();
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.construction.PlaceBlockTask) {
      adris.altoclef.tasks.construction.PlaceBlockTask task = (adris.altoclef.tasks.construction.PlaceBlockTask)other;
      return (task.target.equals(this.target) && task.useThrowaways == this.useThrowaways && Arrays.equals(task.toPlace, this.toPlace));
    } 
    return false;
  }
  
  public boolean isFinished() {
    assert controller.getWorld() != null;
    if (this.useThrowaways)
      return WorldHelper.isSolidBlock(controller, this.target);
    BlockState state = controller.getWorld().getBlockState(this.target);
    return ArrayUtils.contains(this.toPlace, state.getBlock());
  }
  
  protected String toDebugString() {
    return "Place structure" + ArrayUtils.toString(this.toPlace) + " at " + this.target.toShortString();
  }
  
  private boolean tryingAlternativeWay() {
    return (this.failCount % 4 == 3);
  }

  private class PlaceStructureSchematic extends AbstractSchematic {

    private final AltoClefController mod;

    public PlaceStructureSchematic(AltoClefController mod) {
      super(1, 1, 1);
      this.mod = mod;
    }

    @Override
    public BlockState desiredState(int x, int y, int z, BlockState blockState, List<BlockState> available) {
      if (x == 0 && y == 0 && z == 0) {
        // Place!!
        if (!available.isEmpty()) {
          for (BlockState possible : available) {
            if (possible == null) continue;
            if (useThrowaways && mod.getBaritone().settings().acceptableThrowawayItems.get().contains(possible.getBlock().asItem())) {
              return possible;
            }
            if (Arrays.asList(toPlace).contains(possible.getBlock())) {
              return possible;
            }
          }
        }
        Debug.logInternal("Failed to find throwaway block");
        // No throwaways available!!
        return new BlockOptionalMeta(mod.getWorld(), Blocks.COBBLESTONE).getAnyBlockState();
      }
      // Don't care.
      return blockState;
    }
  }
}
