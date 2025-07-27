package adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.PriorityCalculator;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Pair;
import java.util.function.Function;

public class ActionPriorityTask extends PriorityTask {
  private final TaskAndPriorityProvider taskAndPriorityProvider;
  
  private Task lastTask = null;
  
  public ActionPriorityTask(TaskProvider taskProvider, PriorityCalculator priorityCalculator) {
    this(taskProvider, priorityCalculator, a -> Boolean.valueOf(true), false, true, false);
  }
  
  public ActionPriorityTask(TaskProvider taskProvider, PriorityCalculator priorityCalculator, Function<AltoClefController, Boolean> canCall) {
    this(mod -> new Pair(taskProvider.getTask(mod), Double.valueOf(priorityCalculator.getPriority())), canCall);
  }
  
  public ActionPriorityTask(TaskAndPriorityProvider taskAndPriorityProvider) {
    this(taskAndPriorityProvider, a -> Boolean.valueOf(true));
  }
  
  public ActionPriorityTask(TaskAndPriorityProvider taskAndPriorityProvider, Function<AltoClefController, Boolean> canCall) {
    this(taskAndPriorityProvider, canCall, false, true, false);
  }
  
  public ActionPriorityTask(TaskProvider taskProvider, PriorityCalculator priorityCalculator, Function<AltoClefController, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
    this(mod -> new Pair(taskProvider.getTask(mod), Double.valueOf(priorityCalculator.getPriority())), canCall, shouldForce, canCache, bypassForceCooldown);
  }
  
  public ActionPriorityTask(TaskAndPriorityProvider taskAndPriorityProvider, Function<AltoClefController, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
    super(canCall, shouldForce, canCache, bypassForceCooldown);
    this.taskAndPriorityProvider = taskAndPriorityProvider;
  }
  
  public Task getTask(AltoClefController mod) {
    this.lastTask = (Task)getTaskAndPriority(mod).getLeft();
    return this.lastTask;
  }
  
  public String getDebugString() {
    return "Performing an action: " + String.valueOf(this.lastTask);
  }
  
  protected double getPriority(AltoClefController mod) {
    return ((Double)getTaskAndPriority(mod).getRight()).doubleValue();
  }
  
  private Pair<Task, Double> getTaskAndPriority(AltoClefController mod) {
    Pair<Task, Double> pair = this.taskAndPriorityProvider.getTaskAndPriority(mod);
    if (pair == null)
      pair = new Pair(null, Double.valueOf(0.0D)); 
    if (((Double)pair.getRight()).doubleValue() <= 0.0D || pair.getLeft() == null) {
      pair.setLeft(null);
      pair.setRight(Double.valueOf(Double.NEGATIVE_INFINITY));
    } 
    return pair;
  }

  public interface TaskProvider {
    Task getTask(AltoClefController mod);
  }


  public interface TaskAndPriorityProvider {
    Pair<Task, Double> getTaskAndPriority(AltoClefController mod);
  }
}
