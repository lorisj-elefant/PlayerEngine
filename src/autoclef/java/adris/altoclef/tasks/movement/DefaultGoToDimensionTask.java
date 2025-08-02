package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.construction.compound.ConstructNetherPortalBucketTask;
import adris.altoclef.tasks.construction.compound.ConstructNetherPortalObsidianTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Optional;

/**
 * Some generic tasks require us to go to the nether/overworld/end.
 * <p>
 * The user should be able to specify how this should be done in settings
 * (ex, craft a new portal from scratch or check particular portal areas first or highway or whatever)
 */
public class DefaultGoToDimensionTask extends Task {

  private final Dimension target;
  // Cached to keep build properties alive if this task pauses/resumes.
  private final Task cachedNetherBucketConstructionTask = new ConstructNetherPortalBucketTask();

  public DefaultGoToDimensionTask(Dimension target) {
    this.target = target;
  }

  @Override
  protected void onStart() {

  }

  @Override
  protected Task onTick() {
    if (WorldHelper.getCurrentDimension(controller) == target) return null;

    switch (target) {
      case OVERWORLD:
        switch (WorldHelper.getCurrentDimension(controller)) {
          case NETHER:
            return goToOverworldFromNetherTask();
          case END:
            return goToOverworldFromEndTask();
        }
        break;
      case NETHER:
        switch (WorldHelper.getCurrentDimension(controller)) {
          case OVERWORLD:
            return goToNetherFromOverworldTask();
          case END:
            // First go to the overworld
            return goToOverworldFromEndTask();
        }
        break;
      case END:
        switch (WorldHelper.getCurrentDimension(controller)) {
          case NETHER:
            // First go to the overworld
            return goToOverworldFromNetherTask();
          case OVERWORLD:
            return goToEndTask();
        }
        break;
    }

    setDebugState(WorldHelper.getCurrentDimension(controller) + " -> " + target + " is NOT IMPLEMENTED YET!");
    return null;
  }

  @Override
  protected void onStop(Task interruptTask) {

  }

  @Override
  protected boolean isEqual(Task other) {
    if (other instanceof DefaultGoToDimensionTask task) {
      return task .target == target;
    }
    return false;
  }

  @Override
  protected String toDebugString() {
    return "Going to dimension: " + target + " (default version)";
  }

  @Override
  public boolean isFinished() {
    return WorldHelper.getCurrentDimension(controller) == target;
  }

  private Task goToOverworldFromNetherTask() {

    if (netherPortalIsClose(controller)) {
      setDebugState("Going to nether portal");
      return new EnterNetherPortalTask(Dimension.NETHER);
    }

    Optional<BlockPos> closest = controller.getMiscBlockTracker().getLastUsedNetherPortal(Dimension.NETHER);
    if (closest.isPresent()) {
      setDebugState("Going to last nether portal pos");
      return new GetToBlockTask(closest.get());
    }

    setDebugState("Constructing nether portal with obsidian");
    return new ConstructNetherPortalObsidianTask();
  }

  private Task goToOverworldFromEndTask() {
    setDebugState("TODO: Go to center portal (at 0,0). If it doesn't exist, kill ender dragon lol");
    return null;
  }

  private Task goToNetherFromOverworldTask() {

    if (netherPortalIsClose(controller)) {
      setDebugState("Going to nether portal");
      return new EnterNetherPortalTask(Dimension.NETHER);
    }
    return switch (controller.getModSettings().getOverworldToNetherBehaviour()) {
      case BUILD_PORTAL_VANILLA -> cachedNetherBucketConstructionTask;
      case GO_TO_HOME_BASE -> new GetToBlockTask(controller.getModSettings().getHomeBasePosition());
    };
  }

  private Task goToEndTask() {
    // Keep in mind that getting to the end requires going to the nether first.
    setDebugState("TODO: Get to End, Same as BeatMinecraft");
    return null;
  }

  private boolean netherPortalIsClose(AltoClefController mod) {
    if (mod.getBlockScanner().anyFound(Blocks.NETHER_PORTAL)) {
      Optional<BlockPos> closest = mod.getBlockScanner().getNearestBlock( Blocks.NETHER_PORTAL);
      return closest.isPresent() && closest.get().isWithinDistance(new Vec3i((int) mod.getPlayer().getPos().x, (int) mod.getPlayer().getPos().y, (int) mod.getPlayer().getPos().z),2000);
    }
    return false;
  }

  public enum OVERWORLD_TO_NETHER_BEHAVIOUR {
    BUILD_PORTAL_VANILLA,
    GO_TO_HOME_BASE
  }
}