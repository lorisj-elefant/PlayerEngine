package adris.altoclef.util.time;

public abstract class BaseTimer {
    private double prevTime = 0.0D;

    private double interval;

    public BaseTimer(double intervalSeconds) {
        this.interval = intervalSeconds;
    }

    public double getDuration() {
        return currentTime() - this.prevTime;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    public boolean elapsed() {
        return (getDuration() > this.interval);
    }

    public void reset() {
        this.prevTime = currentTime();
    }

    public void forceElapse() {
        this.prevTime = 0.0D;
    }

    protected abstract double currentTime();

    protected void setPrevTimeForce(double toSet) {
        this.prevTime = toSet;
    }

    protected double getPrevTime() {
        return this.prevTime;
    }
}
