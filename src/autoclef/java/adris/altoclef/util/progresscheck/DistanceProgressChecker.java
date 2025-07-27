//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package adris.altoclef.util.progresscheck;

import net.minecraft.util.math.Vec3d;

public class DistanceProgressChecker implements IProgressChecker<Vec3d> {
    private final IProgressChecker<Double> distanceChecker;
    private final boolean reduceDistance;
    private Vec3d start;
    private Vec3d prevPos;

    public DistanceProgressChecker(IProgressChecker<Double> distanceChecker, boolean reduceDistance) {
        this.distanceChecker = distanceChecker;
        this.reduceDistance = reduceDistance;
        if (reduceDistance) {
            this.distanceChecker.setProgress(Double.NEGATIVE_INFINITY);
        }

        this.reset();
    }

    public DistanceProgressChecker(double timeout, double minDistanceToMake, boolean reduceDistance) {
        this(new LinearProgressChecker(timeout, minDistanceToMake), reduceDistance);
    }

    public DistanceProgressChecker(double timeout, double minDistanceToMake) {
        this(timeout, minDistanceToMake, false);
    }

    public void setProgress(Vec3d position) {
        if (this.start == null) {
            this.start = position;
        } else {
            double delta = position.distanceTo(this.start);
            if (this.reduceDistance) {
                delta *= (double)-1.0F;
            }

            this.prevPos = position;
            this.distanceChecker.setProgress(delta);
        }
    }

    public boolean failed() {
        return this.distanceChecker.failed();
    }

    public void reset() {
        this.start = null;
        this.distanceChecker.setProgress((double)0.0F);
        this.distanceChecker.reset();
    }
}
