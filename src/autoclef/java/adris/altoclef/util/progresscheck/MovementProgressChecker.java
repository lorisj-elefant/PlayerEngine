package adris.altoclef.util.progresscheck;

import adris.altoclef.AltoClefController;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MovementProgressChecker {
  private final IProgressChecker<Vec3d> distanceChecker;
  
  private final IProgressChecker<Double> mineChecker;
  
  public BlockPos lastBreakingBlock = null;
  
  public MovementProgressChecker(double distanceTimeout, double minDistance, double mineTimeout, double minMineProgress, int attempts) {
    this.distanceChecker = (IProgressChecker<Vec3d>)new ProgressCheckerRetry((IProgressChecker)new DistanceProgressChecker(distanceTimeout, minDistance), attempts);
    this.mineChecker = (IProgressChecker<Double>)new LinearProgressChecker(mineTimeout, minMineProgress);
  }
  
  public MovementProgressChecker(double distanceTimeout, double minDistance, double mineTimeout, double minMineProgress) {
    this(distanceTimeout, minDistance, mineTimeout, minMineProgress, 1);
  }
  
  public MovementProgressChecker(int attempts) {
    this(6.0D, 0.1D, 10.0D, 0.001D, attempts);
  }
  
  public MovementProgressChecker() {
    this(1);
  }
  
  public boolean check(AltoClefController mod) {
    if (mod.getFoodChain().needsToEat()) {
      this.distanceChecker.reset();
      this.mineChecker.reset();
    } 
    if (mod.getControllerExtras().isBreakingBlock()) {
      BlockPos breakBlock = mod.getControllerExtras().getBreakingBlockPos();
      if (this.lastBreakingBlock != null && WorldHelper.isAir(mod.getWorld().getBlockState(this.lastBreakingBlock).getBlock())) {
        this.distanceChecker.reset();
        this.mineChecker.reset();
      } 
      this.lastBreakingBlock = breakBlock;
      this.mineChecker.setProgress(0d);
      return !this.mineChecker.failed();
    } 
    this.mineChecker.reset();
    this.distanceChecker.setProgress(mod.getPlayer().getPos());
    return !this.distanceChecker.failed();
  }
  
  public void reset() {
    this.distanceChecker.reset();
    this.mineChecker.reset();
  }
}
