package adris.altoclef.tasks;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import java.util.HashMap;
import java.util.Optional;
import java.util.OptionalDouble;

import net.minecraft.util.math.Vec3d;

public abstract class AbstractDoToClosestObjectTask<T> extends Task {
  private final HashMap<T, CachedHeuristic> heuristicMap = new HashMap<>();
  
  private T currentlyPursuing = null;
  
  private boolean wasWandering;
  
  private Task goalTask = null;
  
  protected abstract Vec3d getPos(AltoClefController paramAltoClefController, T paramT);
  
  protected abstract Optional<T> getClosestTo(AltoClefController paramAltoClefController, Vec3d paramVec3d);
  
  protected abstract Vec3d getOriginPos(AltoClefController paramAltoClefController);
  
  protected abstract Task getGoalTask(T paramT);
  
  protected abstract boolean isValid(AltoClefController paramAltoClefController, T paramT);
  
  protected Task getWanderTask(AltoClefController mod) {
    return (Task)new TimeoutWanderTask(true);
  }
  
  public void resetSearch() {
    this.currentlyPursuing = null;
    this.heuristicMap.clear();
    this.goalTask = null;
  }
  
  public boolean wasWandering() {
    return this.wasWandering;
  }
  
  private double getCurrentCalculatedHeuristic(AltoClefController mod) {
    OptionalDouble ticksRemainingOp = mod.getBaritone().getPathingBehavior().ticksRemainingInSegment();
    return ((Double)ticksRemainingOp.orElse(Double.valueOf(Double.POSITIVE_INFINITY))).doubleValue();
  }
  
  protected Task onTick() {
    this.wasWandering = false;
    AltoClefController mod = controller;
    if (this.currentlyPursuing != null && !isValid(mod, this.currentlyPursuing)) {
      this.heuristicMap.remove(this.currentlyPursuing);
      this.currentlyPursuing = null;
    } 
    Optional<T> checkNewClosest = getClosestTo(mod, getOriginPos(mod));
    if (checkNewClosest.isPresent() && !checkNewClosest.get().equals(this.currentlyPursuing)) {
      T newClosest = checkNewClosest.get();
      if (this.currentlyPursuing == null) {
        this.currentlyPursuing = newClosest;
      } else if (this.goalTask != null) {
        setDebugState("Moving towards closest...");
        double currentHeuristic = getCurrentCalculatedHeuristic(mod);
        double closestDistanceSqr = getPos(mod, this.currentlyPursuing).squaredDistanceTo(mod.getPlayer().getPos());
        int lastTick = controller.getWorld().getServer().getTicks();
        if (!this.heuristicMap.containsKey(this.currentlyPursuing))
          this.heuristicMap.put(this.currentlyPursuing, new CachedHeuristic()); 
        CachedHeuristic h = this.heuristicMap.get(this.currentlyPursuing);
        h.updateHeuristic(currentHeuristic);
        h.updateDistance(closestDistanceSqr);
        h.setTickAttempted(lastTick);
        if (this.heuristicMap.containsKey(newClosest)) {
          CachedHeuristic maybeReAttempt = this.heuristicMap.get(newClosest);
          double maybeClosestDistance = getPos(mod, newClosest).squaredDistanceTo(mod.getPlayer().getPos());
          if (maybeReAttempt.getHeuristicValue() < h.getHeuristicValue() || maybeClosestDistance < maybeReAttempt.getClosestDistanceSqr() / 4.0D) {
            setDebugState("Retrying old heuristic!");
            this.currentlyPursuing = newClosest;
            maybeReAttempt.updateDistance(maybeClosestDistance);
          } 
        } else {
          setDebugState("Trying out NEW pursuit");
          this.currentlyPursuing = newClosest;
        } 
      } else {
        setDebugState("Waiting for move task to kick in...");
      } 
    } 
    if (this.currentlyPursuing != null) {
      this.goalTask = getGoalTask(this.currentlyPursuing);
      return this.goalTask;
    } 
    this.goalTask = null;
    if (checkNewClosest.isEmpty()) {
      setDebugState("Waiting for calculations I think (wandering)");
      this.wasWandering = true;
      return getWanderTask(mod);
    } 
    setDebugState("Waiting for calculations I think (NOT wandering)");
    return null;
  }

  private static class CachedHeuristic {

    private double closestDistanceSqr;
    private int tickAttempted;
    private double heuristicValue;

    public CachedHeuristic() {
      closestDistanceSqr = Double.POSITIVE_INFINITY;
      heuristicValue = Double.POSITIVE_INFINITY;
    }

    public CachedHeuristic(double closestDistanceSqr, int tickAttempted, double heuristicValue) {
      closestDistanceSqr = closestDistanceSqr;
      tickAttempted = tickAttempted;
      heuristicValue = heuristicValue;
    }

    public double getHeuristicValue() {
      return heuristicValue;
    }

    public void updateHeuristic(double heuristicValue) {
      heuristicValue = Math.min(heuristicValue, heuristicValue);
    }

    public double getClosestDistanceSqr() {
      return closestDistanceSqr;
    }

    public void updateDistance(double closestDistanceSqr) {
      closestDistanceSqr = Math.min(closestDistanceSqr, closestDistanceSqr);
    }

    public int getTickAttempted() {
      return tickAttempted;
    }

    public void setTickAttempted(int tickAttempted) {
      tickAttempted = tickAttempted;
    }
  }
}
