package adris.altoclef.util.time;

public class Stopwatch {
  boolean running = false;
  
  private double startTime = 0.0D;
  
  private static double currentTime() {
    return System.currentTimeMillis() / 1000.0D;
  }
  
  public void begin() {
    this.startTime = currentTime();
    this.running = true;
  }
  
  public double time() {
    if (!this.running)
      return 0.0D; 
    return currentTime() - this.startTime;
  }
}
