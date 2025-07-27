package adris.altoclef.util.baritone;

import adris.altoclef.AltoClefController;
import adris.altoclef.util.helpers.BaritoneHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.pathing.goals.GoalYLevel;
import java.util.List;
import net.minecraft.entity.Entity;

public abstract class GoalRunAwayFromEntities implements Goal {
  private final AltoClefController mod;
  
  private final double distance;
  
  private final boolean xzOnly;
  
  private final double penaltyFactor;
  
  public GoalRunAwayFromEntities(AltoClefController mod, double distance, boolean xzOnly, double penaltyFactor) {
    this.mod = mod;
    this.distance = distance;
    this.xzOnly = xzOnly;
    this.penaltyFactor = penaltyFactor;
  }
  
  public boolean isInGoal(int x, int y, int z) {
    List<Entity> entities = getEntities(this.mod);
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      if (!entities.isEmpty())
        for (Entity entity : entities) {
          double sqDistance;
          if (entity == null || !entity.isAlive())
            continue; 
          if (this.xzOnly) {
            sqDistance = entity.getPos().subtract(x, y, z).multiply(1.0D, 0.0D, 1.0D).lengthSquared();
          } else {
            sqDistance = entity.squaredDistanceTo(x, y, z);
          } 
          if (sqDistance < this.distance * this.distance)
            return false; 
        }  
    } 
    return true;
  }
  
  public double heuristic(int x, int y, int z) {
    double costSum = 0.0D;
    List<Entity> entities = getEntities(this.mod);
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      int max = 10;
      int counter = 0;
      if (!entities.isEmpty())
        for (Entity entity : entities) {
          counter++;
          if (entity == null || !entity.isAlive())
            continue; 
          double cost = getCostOfEntity(entity, x, y, z);
          if (cost != 0.0D) {
            costSum += 1.0D / cost;
          } else {
            costSum += 1000.0D;
          } 
          if (counter >= max)
            break; 
        }  
      if (counter > 0)
        costSum /= counter; 
      return costSum * this.penaltyFactor;
    } 
  }
  
  protected abstract List<Entity> getEntities(AltoClefController paramAltoClefController);
  
  protected double getCostOfEntity(Entity entity, int x, int y, int z) {
    double heuristic = 0.0D;
    if (!this.xzOnly)
      heuristic += GoalYLevel.calculate(entity.getBlockPos().getY(), y); 
    heuristic += GoalXZ.calculate((entity.getBlockPos().getX() - x), (entity.getBlockPos().getZ() - z));
    return heuristic;
  }
}
