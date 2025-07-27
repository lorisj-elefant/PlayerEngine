package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.tasks.movement.GetWithinRangeOfBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;

public class KillEndermanTask extends ResourceTask {
  private final int _count;
  
  private final TimerGame _lookDelay = new TimerGame(0.2D);
  
  public KillEndermanTask(int count) {
    super(new ItemTarget(Items.ENDER_PEARL, count));
    this._count = count;
    forceDimension(Dimension.NETHER);
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    if (!mod.getEntityTracker().entityFound(new Class[] { EndermanEntity.class })) {
      if (WorldHelper.getCurrentDimension(mod) != Dimension.NETHER)
        return getToCorrectDimensionTask(mod); 
      Optional<BlockPos> nearest = mod.getBlockScanner().getNearestBlock(new Block[] { Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WARPED_HYPHAE, Blocks.WARPED_NYLIUM });
      if (nearest.isPresent()) {
        if (WorldHelper.inRangeXZ(nearest.get(), mod.getPlayer().getBlockPos(), 40.0D)) {
          setDebugState("Waiting for endermen to spawn...");
          return null;
        } 
        setDebugState("Getting to warped forest biome");
        return (Task)new GetWithinRangeOfBlockTask(nearest.get(), 35);
      } 
      setDebugState("Warped forest biome not found");
      return (Task)new TimeoutWanderTask();
    } 
    Predicate<Entity> belowNetherRoof = entity -> (WorldHelper.getCurrentDimension(mod) != Dimension.NETHER || entity.getY() < 125.0D);
    int TOO_FAR_AWAY = (WorldHelper.getCurrentDimension(mod) == Dimension.NETHER) ? 10 : 256;
    for (EndermanEntity entity : mod.getEntityTracker().getTrackedEntities(EndermanEntity.class)) {
      if (!entity.isAlive())
        continue; 
      if (belowNetherRoof.test(entity) && entity.isAngry() && entity.getPos().isInRange((Position)mod.getPlayer().getPos(), TOO_FAR_AWAY))
        return (Task)new KillEntityTask((Entity)entity); 
    } 
    return (Task)new KillEntitiesTask(belowNetherRoof, new Class[] { EndermanEntity.class });
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.resources.KillEndermanTask) {
      adris.altoclef.tasks.resources.KillEndermanTask task = (adris.altoclef.tasks.resources.KillEndermanTask)other;
      return (task._count == this._count);
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Hunting endermen for pearls - " + controller.getItemStorage().getItemCount(new Item[] { Items.ENDER_PEARL }) + "/" + this._count;
  }
}
