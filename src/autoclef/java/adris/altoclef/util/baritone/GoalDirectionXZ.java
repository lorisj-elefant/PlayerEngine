package adris.altoclef.util.baritone;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.Vec3d;

public class GoalDirectionXZ implements Goal {
  private final double originx;
  
  private final double originz;
  
  private final double dirx;
  
  private final double dirz;
  
  private final double sidePenalty;
  
  public GoalDirectionXZ(Vec3d origin, Vec3d offset, double sidePenalty) {
    this.originx = origin.getX();
    this.originz = origin.getZ();
    offset = offset.multiply(1.0D, 0.0D, 1.0D);
    offset = offset.normalize();
    this.dirx = offset.x;
    this.dirz = offset.z;
    if (this.dirx == 0.0D && this.dirz == 0.0D)
      throw new IllegalArgumentException(String.valueOf(offset)); 
    this.sidePenalty = sidePenalty;
  }
  
  private static String maybeCensor(double value) {
    return ((Boolean)(BaritoneAPI.getGlobalSettings()).censorCoordinates.get()).booleanValue() ? "<censored>" : Double.toString(value);
  }
  
  public boolean isInGoal(int x, int y, int z) {
    return false;
  }
  
  public double heuristic(int x, int y, int z) {
    double dx = x - this.originx;
    double dz = z - this.originz;
    double correctDistance = dx * this.dirx + dz * this.dirz;
    double px = this.dirx * correctDistance;
    double pz = this.dirz * correctDistance;
    double perpendicularDistance = (dx - px) * (dx - px) + (dz - pz) * (dz - pz);
    return -correctDistance * ((Double)(BaritoneAPI.getGlobalSettings()).costHeuristic.get()).doubleValue() + perpendicularDistance * this.sidePenalty;
  }
  
  public String toString() {
    return String.format("GoalDirection{x=%s, z=%s, dx=%s, dz=%s}", new Object[] { maybeCensor(this.originx), maybeCensor(this.originz), maybeCensor(this.dirx), maybeCensor(this.dirz) });
  }
}
