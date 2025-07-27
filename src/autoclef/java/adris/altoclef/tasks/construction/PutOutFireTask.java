package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class PutOutFireTask extends Task {
  private final BlockPos _firePosition;
  
  public PutOutFireTask(BlockPos firePosition) {
    this._firePosition = firePosition;
  }
  
  protected void onStart() {}
  
  protected Task onTick() {
    return (Task)new InteractWithBlockTask(ItemTarget.EMPTY, null, this._firePosition, Input.CLICK_LEFT, false, false);
  }
  
  protected void onStop(Task interruptTask) {}
  
  public boolean isFinished() {
    BlockState s = controller.getWorld().getBlockState(this._firePosition);
    return (s.getBlock() != Blocks.FIRE && s.getBlock() != Blocks.SOUL_FIRE);
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.construction.PutOutFireTask) {
      adris.altoclef.tasks.construction.PutOutFireTask task = (adris.altoclef.tasks.construction.PutOutFireTask)other;
      return task._firePosition.equals(this._firePosition);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Putting out fire at " + String.valueOf(this._firePosition);
  }
}
