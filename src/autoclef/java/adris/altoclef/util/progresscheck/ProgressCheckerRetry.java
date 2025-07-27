package adris.altoclef.util.progresscheck;

import adris.altoclef.util.progresscheck.IProgressChecker;

public class ProgressCheckerRetry<T> implements IProgressChecker<T> {
  private final IProgressChecker<T> subChecker;
  
  private final int allowedAttempts;
  
  private int failCount;
  
  public ProgressCheckerRetry(IProgressChecker<T> subChecker, int allowedAttempts) {
    this.subChecker = subChecker;
    this.allowedAttempts = allowedAttempts;
  }
  
  public void setProgress(T progress) {
    this.subChecker.setProgress(progress);
    if (this.subChecker.failed()) {
      this.failCount++;
      this.subChecker.reset();
    } 
  }
  
  public boolean failed() {
    return (this.failCount >= this.allowedAttempts);
  }
  
  public void reset() {
    this.subChecker.reset();
    this.failCount = 0;
  }
}
