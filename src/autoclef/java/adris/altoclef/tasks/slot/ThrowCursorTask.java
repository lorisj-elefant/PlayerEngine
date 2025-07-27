package adris.altoclef.tasks.slot;

import adris.altoclef.tasks.slot.ClickSlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.slots.Slot;

public class ThrowCursorTask extends Task {
  private final Task throwTask = (Task)new ClickSlotTask(Slot.UNDEFINED);
  
  protected void onStart() {}
  
  protected Task onTick() {
    return this.throwTask;
  }
  
  protected void onStop(Task interruptTask) {}
  
  protected boolean isEqual(Task obj) {
    return obj instanceof adris.altoclef.tasks.slot.ThrowCursorTask;
  }
  
  protected String toDebugString() {
    return "Throwing Cursor";
  }
  
  public boolean isFinished() {
    return this.throwTask.isFinished();
  }
}
