package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;

public class SleepThroughNightTask extends Task {
  protected void onStart() {}
  
  protected Task onTick() {
    return (Task)(new PlaceBedAndSetSpawnTask()).stayInBed();
  }
  
  protected void onStop(Task interruptTask) {}
  
  protected boolean isEqual(Task other) {
    return other instanceof adris.altoclef.tasks.misc.SleepThroughNightTask;
  }
  
  protected String toDebugString() {
    return "Sleeping through the night";
  }
  
  public boolean isFinished() {
    int time = (int)(controller.getWorld().getTimeOfDay() % 24000L);
    return (0 <= time && time < 13000);
  }
}
