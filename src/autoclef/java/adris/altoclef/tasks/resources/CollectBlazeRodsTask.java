package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PutOutFireTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.RunAwayFromHostilesTask;
import adris.altoclef.tasks.movement.SearchChunkForBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class CollectBlazeRodsTask extends ResourceTask {
  private static final double SPAWNER_BLAZE_RADIUS = 32.0D;
  
  private static final double TOO_LITTLE_HEALTH_BLAZE = 10.0D;
  
  private static final int TOO_MANY_BLAZES = 5;
  
  private final int count;
  
  private final Task searcher = (Task)new SearchChunkForBlockTask(new Block[] { Blocks.NETHER_BRICKS });
  
  private BlockPos foundBlazeSpawner = null;
  
  public CollectBlazeRodsTask(int count) {
    super(Items.BLAZE_ROD, count);
    this .count = count;
  }
  
  private static boolean isHoveringAboveLavaOrTooHigh(AltoClefController mod, Entity entity) {
    int MAX_HEIGHT = 11;
    for (BlockPos check = entity.getBlockPos(); entity.getBlockPos().getY() - check.getY() < MAX_HEIGHT; check = check.down()) {
      if (mod.getWorld().getBlockState(check).getBlock() == Blocks.LAVA)
        return true; 
      if (WorldHelper.isSolidBlock(mod, check))
        return false; 
    } 
    return true;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    if (WorldHelper.getCurrentDimension(controller) != Dimension.NETHER) {
      setDebugState("Going to nether");
      return (Task)new DefaultGoToDimensionTask(Dimension.NETHER);
    } 
    Optional<Entity> toKill = Optional.empty();
    if (mod.getEntityTracker().entityFound(new Class[] { BlazeEntity.class })) {
      toKill = mod.getEntityTracker().getClosestEntity(new Class[] { BlazeEntity.class });
      if (toKill.isPresent() && 
        mod.getPlayer().getHealth() <= 10.0D && mod
        .getEntityTracker().getTrackedEntities(BlazeEntity.class).size() >= 5) {
        setDebugState("Running away as there are too many blazes nearby.");
        return (Task)new RunAwayFromHostilesTask(30.0D, true);
      } 
      if (this .foundBlazeSpawner != null && toKill.isPresent()) {
        Entity kill = toKill.get();
        Vec3d nearest = kill.getPos();
        double sqDistanceToPlayer = nearest.squaredDistanceTo(mod.getPlayer().getPos());
        if (sqDistanceToPlayer > 1024.0D) {
          BlockHitResult hit = mod.getWorld().raycast(new RaycastContext(mod.getPlayer().getCameraPosVec(1.0F), kill.getCameraPosVec(1.0F), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)mod.getPlayer()));
          if (hit != null && BlockPosVer.getSquaredDistance(hit.getBlockPos(), (Position)mod.getPlayer().getPos()) < sqDistanceToPlayer)
            toKill = Optional.empty(); 
        } 
      } 
    } 
    if (toKill.isPresent() && ((Entity)toKill.get()).isAlive() && !isHoveringAboveLavaOrTooHigh(mod, toKill.get())) {
      setDebugState("Killing blaze");
      Predicate<Entity> safeToPursue = entity -> !isHoveringAboveLavaOrTooHigh(mod, entity);
      return (Task)new KillEntitiesTask(safeToPursue, new Class[] { ((Entity)toKill.get()).getClass() });
    } 
    if (this .foundBlazeSpawner != null && mod.getChunkTracker().isChunkLoaded(this .foundBlazeSpawner) && !isValidBlazeSpawner(mod, this .foundBlazeSpawner)) {
      Debug.logMessage("Blaze spawner at " + String.valueOf(this .foundBlazeSpawner) + " too far away or invalid. Re-searching.");
      this .foundBlazeSpawner = null;
    } 
    if (this .foundBlazeSpawner != null) {
      if (!this .foundBlazeSpawner.isCenterWithinDistance((Position)mod.getPlayer().getPos(), 4.0D)) {
        setDebugState("Going to blaze spawner");
        return (Task)new GetToBlockTask(this .foundBlazeSpawner.up(), false);
      } 
      Optional<BlockPos> nearestFire = mod.getBlockScanner().getNearestWithinRange(this .foundBlazeSpawner, 5.0D, new Block[] { Blocks.FIRE });
      if (nearestFire.isPresent()) {
        setDebugState("Clearing fire around spawner to prevent loss of blaze rods.");
        return (Task)new PutOutFireTask(nearestFire.get());
      } 
      setDebugState("Waiting near blaze spawner for blazes to spawn");
      return null;
    } 
    Optional<BlockPos> pos = mod.getBlockScanner().getNearestBlock(blockPos -> isValidBlazeSpawner(mod, blockPos), new Block[] { Blocks.SPAWNER });
    pos.ifPresent(blockPos -> this .foundBlazeSpawner = blockPos);
    setDebugState("Searching for fortress/Traveling around fortress");
    return this .searcher;
  }
  
  private boolean isValidBlazeSpawner(AltoClefController mod, BlockPos pos) {
    if (!mod.getChunkTracker().isChunkLoaded(pos))
      return false; 
    return WorldHelper.getSpawnerEntity(mod, pos) instanceof BlazeEntity;
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectBlazeRodsTask;
  }
  
  protected String toDebugStringName() {
    return "Collect blaze rods - " + controller.getItemStorage().getItemCount(new Item[] { Items.BLAZE_ROD }) + "/" + this .count;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
}
