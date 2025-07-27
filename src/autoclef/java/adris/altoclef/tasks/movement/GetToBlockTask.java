package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.util.math.BlockPos;

public class GetToBlockTask extends CustomBaritoneGoalTask implements ITaskRequiresGrounded {
  private final BlockPos _position;
  
  private final boolean _preferStairs;
  
  private final Dimension _dimension;
  
  private int finishedTicks = 0;
  
  private final TimerGame wanderTimer = new TimerGame(2.0D);
  
  public GetToBlockTask(BlockPos position, boolean preferStairs) {
    this(position, preferStairs, null);
  }
  
  public GetToBlockTask(BlockPos position, Dimension dimension) {
    this(position, false, dimension);
  }
  
  public GetToBlockTask(BlockPos position, boolean preferStairs, Dimension dimension) {
    this._dimension = dimension;
    this._position = position;
    this._preferStairs = preferStairs;
  }
  
  public GetToBlockTask(BlockPos position) {
    this(position, false);
  }
  
  protected Task onTick() {
    if (this._dimension != null && WorldHelper.getCurrentDimension(controller) != this._dimension)
      return (Task)new DefaultGoToDimensionTask(this._dimension); 
    if (isFinished()) {
      this.finishedTicks++;
    } else {
      this.finishedTicks = 0;
    } 
    if (this.finishedTicks > 200) {
      this.wanderTimer.reset();
      Debug.logWarning("GetToBlock was finished for 10 seconds yet is still being called, wandering");
      this.finishedTicks = 0;
      return (Task)new TimeoutWanderTask();
    } 
    if (!this.wanderTimer.elapsed())
      return (Task)new TimeoutWanderTask(); 
    return super.onTick();
  }
  
  protected void onStart() {
    super.onStart();
    if (this._preferStairs) {
      controller.getBehaviour().push();
      controller.getBehaviour().setPreferredStairs(true);
    } 
  }
  
  protected void onStop(Task interruptTask) {
    super.onStop(interruptTask);
    if (this._preferStairs)
      controller.getBehaviour().pop();
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.GetToBlockTask) {
      adris.altoclef.tasks.movement.GetToBlockTask task = (adris.altoclef.tasks.movement.GetToBlockTask)other;
      return (task._position.equals(this._position) && task._preferStairs == this._preferStairs && task._dimension == this._dimension);
    } 
    return false;
  }
  
  public boolean isFinished() {
    return (super.isFinished() && (this._dimension == null || this._dimension == WorldHelper.getCurrentDimension(controller)));
  }
  
  protected String toDebugString() {
    return "Getting to block " + String.valueOf(this._position) + ((this._dimension != null) ? (" in dimension " + String.valueOf(this._dimension)) : "");
  }
  
  protected Goal newGoal(AltoClefController mod) {
    return (Goal)new GoalBlock(this._position);
  }
  
  protected void onWander(AltoClefController mod) {
    super.onWander(mod);
    mod.getBlockScanner().requestBlockUnreachable(this._position);
  }
}
