package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.util.baritone.GoalRunAwayFromEntities;
import baritone.api.pathing.goals.Goal;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.entity.Entity;

public abstract class RunAwayFromEntitiesTask extends CustomBaritoneGoalTask {
  private final Supplier<List<Entity>> _runAwaySupplier;
  
  private final double _distanceToRun;
  
  private final boolean _xz;
  
  private final double _penalty;
  
  public RunAwayFromEntitiesTask(Supplier<List<Entity>> toRunAwayFrom, double distanceToRun, boolean xz, double penalty) {
    this._runAwaySupplier = toRunAwayFrom;
    this._distanceToRun = distanceToRun;
    this._xz = xz;
    this._penalty = penalty;
  }
  
  public RunAwayFromEntitiesTask(Supplier<List<Entity>> toRunAwayFrom, double distanceToRun, double penalty) {
    this(toRunAwayFrom, distanceToRun, false, penalty);
  }
  
  protected Goal newGoal(AltoClefController mod) {
    return (Goal)new GoalRunAwayStuff( mod, this._distanceToRun, this._xz);
  }

  private class GoalRunAwayStuff extends GoalRunAwayFromEntities {

    public GoalRunAwayStuff(AltoClefController mod, double distance, boolean xz) {
      super(mod, distance, xz, _penalty);
    }

    @Override
    protected List<net.minecraft.entity.Entity> getEntities(AltoClefController mod) {
      return _runAwaySupplier.get();
    }
  }
}
