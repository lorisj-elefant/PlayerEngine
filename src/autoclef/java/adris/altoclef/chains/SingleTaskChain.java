package adris.altoclef.chains;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskChain;
import adris.altoclef.tasksystem.TaskRunner;

public abstract class SingleTaskChain extends TaskChain {
  protected Task mainTask = null;
  
  private boolean interrupted = false;
  
  private final AltoClefController mod;
  
  public SingleTaskChain(TaskRunner runner) {
    super(runner);
    this.mod = runner.getMod();
  }
  
  protected void onTick() {
    if (!isActive())
      return; 
    if (this.interrupted) {
      this.interrupted = false;
      if (this.mainTask != null)
        this.mainTask.reset(); 
    } 
    if (this.mainTask != null)
      if (this.mainTask.isFinished() || this.mainTask.stopped()) {
        onTaskFinish(this.mod);
      } else {
        this.mainTask.tick(this);
      }  
  }
  
  protected void onStop() {
    if (isActive() && this.mainTask != null) {
      this.mainTask.stop();
      this.mainTask = null;
    } 
  }
  
  public void setTask(Task task) {
    if (this.mainTask == null || !this.mainTask.equals(task)) {
      if (this.mainTask != null)
        this.mainTask.stop(task); 
      this.mainTask = task;
      if (task != null)
        task.reset(); 
    } 
  }
  
  public boolean isActive() {
    return (this.mainTask != null);
  }
  
  protected abstract void onTaskFinish(AltoClefController paramAltoClefController);
  
  public void onInterrupt(TaskChain other) {
    if (other != null)
      Debug.logInternal("Chain Interrupted: " + String.valueOf(this) + " by " + String.valueOf(other)); 
    this.interrupted = true;
    if (this.mainTask != null && this.mainTask.isActive())
      this.mainTask.interrupt(null); 
  }
  
  protected boolean isCurrentlyRunning(AltoClefController mod) {
    return (!this.interrupted && this.mainTask.isActive() && !this.mainTask.isFinished());
  }
  
  public Task getCurrentTask() {
    return this.mainTask;
  }
}
