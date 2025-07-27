package adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators;

import adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.DistancePriorityCalculator;

public class DistanceItemPriorityCalculator extends DistancePriorityCalculator {
  private final double multiplier;
  
  private final double unneededMultiplier;
  
  private final double unneededDistanceThreshold;
  
  public DistanceItemPriorityCalculator(double multiplier, double unneededMultiplier, double unneededDistanceThreshold, int minCount, int maxCount) {
    super(minCount, maxCount);
    this.multiplier = multiplier;
    this.unneededMultiplier = unneededMultiplier;
    this.unneededDistanceThreshold = unneededDistanceThreshold;
  }
  
  protected double calculatePriority(double distance) {
    double priority = 1.0D / distance;
    if (this.minCountSatisfied) {
      if (distance < this.unneededDistanceThreshold)
        return priority * this.unneededMultiplier; 
      return Double.NEGATIVE_INFINITY;
    } 
    return priority * this.multiplier;
  }
}
