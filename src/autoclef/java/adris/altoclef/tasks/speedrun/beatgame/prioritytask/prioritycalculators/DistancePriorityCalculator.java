package adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators;

public abstract class DistancePriorityCalculator {
  public final int minCount;
  
  public final int maxCount;
  
  protected boolean minCountSatisfied = false;
  
  protected boolean maxCountSatisfied = false;
  
  public DistancePriorityCalculator(int minCount, int maxCount) {
    this.minCount = minCount;
    this.maxCount = maxCount;
  }
  
  public void update(int count) {
    if (count >= this.minCount)
      this.minCountSatisfied = true; 
    if (count >= this.maxCount)
      this.maxCountSatisfied = true; 
  }
  
  public double getPriority(double distance) {
    if (Double.isInfinite(distance) || distance == 2.147483647E9D || this.maxCountSatisfied)
      return Double.NEGATIVE_INFINITY; 
    return calculatePriority(distance);
  }
  
  abstract double calculatePriority(double paramDouble);
}
