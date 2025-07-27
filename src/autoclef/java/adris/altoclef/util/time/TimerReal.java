package adris.altoclef.util.time;

import adris.altoclef.util.time.BaseTimer;

public class TimerReal extends BaseTimer {
  public TimerReal(double intervalSeconds) {
    super(intervalSeconds);
  }
  
  protected double currentTime() {
    return System.currentTimeMillis() / 1000.0D;
  }
}
