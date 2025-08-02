package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.util.baritone.GoalRunAwayFromEntities;
import baritone.api.pathing.goals.Goal;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.entity.Entity;

public abstract class RunAwayFromEntitiesTask extends CustomBaritoneGoalTask {
  private final Supplier<List<Entity>> runAwaySupplier;
  
  private final double distanceToRun;
  
  private final boolean xz;
  
  private final double penalty;
  
  public RunAwayFromEntitiesTask(Supplier<List<Entity>> toRunAwayFrom, double distanceToRun, boolean xz, double penalty) {
    this .runAwaySupplier = toRunAwayFrom;
    this .distanceToRun = distanceToRun;
    this .xz = xz;
    this .penalty = penalty;
  }
  
  public RunAwayFromEntitiesTask(Supplier<List<Entity>> toRunAwayFrom, double distanceToRun, double penalty) {
    this(toRunAwayFrom, distanceToRun, false, penalty);
  }
  
  protected Goal newGoal(AltoClefController mod) {
    return (Goal)new GoalRunAwayStuff( mod, this .distanceToRun, this .xz);
  }

  private class GoalRunAwayStuff extends GoalRunAwayFromEntities {

    public GoalRunAwayStuff(AltoClefController mod, double distance, boolean xz) {
      super(mod, distance, xz, penalty);
    }

    @Override
    protected List<net.minecraft.entity.Entity> getEntities(AltoClefController mod) {
      return runAwaySupplier.get();
    }
  }
}
