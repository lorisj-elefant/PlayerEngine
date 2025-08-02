package adris.altoclef.tasks.construction;

import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import baritone.api.process.IBuilderProcess;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

/**
 * A task that tells Automatone to destroy a single block at a specific position.
 * This is now a lightweight wrapper around Automatone's highly optimized BuilderProcess.
 */
public class DestroyBlockTask extends Task implements ITaskRequiresGrounded {

  private final BlockPos pos;
  private boolean isClear;

  public DestroyBlockTask(BlockPos pos) {
    this .pos = pos;
  }

  @Override
  protected void onStart() {
    isClear = false;
    IBuilderProcess builder = controller.getBaritone().getBuilderProcess();
    // Tell baritone to clear a 1x1x1 area at our target position.
    builder.clearArea(pos, pos);
  }

  @Override
  protected Task onTick() {
    IBuilderProcess builder = controller.getBaritone().getBuilderProcess();

    // If the builder process stops for any reason, assume we're done or something went wrong.
    if (!builder.isActive()) {
      isClear = true; // Let isFinished handle the final check.
      return null;
    }

    setDebugState("Automatone is breaking the block.");
    return null; // Let Automatone do its thing.
  }

  @Override
  protected void onStop(Task interruptTask) {
    // If our task is interrupted, we must stop the builder process.
    IBuilderProcess builder = controller.getBaritone().getBuilderProcess();
    if (builder.isActive()) {
      builder.onLostControl();
    }
  }

  @Override
  public boolean isFinished() {
    // The task is finished if the block is air.
    return isClear || controller.getWorld().isAir(pos);
  }

  @Override
  protected boolean isEqual(Task other) {
    if (other instanceof DestroyBlockTask task) {
      return Objects.equals(task .pos, this .pos);
    }
    return false;
  }

  @Override
  protected String toDebugString() {
    return "Destroying block at " + pos.toShortString();
  }
}