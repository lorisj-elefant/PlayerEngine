package adris.altoclef.trackers;

import adris.altoclef.AltoClefController;

import java.util.ArrayList;

public class TrackerManager {
  private final ArrayList<Tracker> _trackers = new ArrayList<>();
  
  private final AltoClefController _mod;
  
  private boolean _wasInGame = false;
  
  public TrackerManager(AltoClefController mod) {
    this._mod = mod;
  }
  
  public void tick() {
    boolean inGame = AltoClefController.inGame();
    if (!inGame && this._wasInGame) {
      for (Tracker tracker : this._trackers)
        tracker.reset(); 
      this._mod.getChunkTracker().reset(this._mod);
      this._mod.getMiscBlockTracker().reset();
    } 
    this._wasInGame = inGame;
    for (Tracker tracker : this._trackers)
      tracker.setDirty(); 
  }
  
  public void addTracker(Tracker tracker) {
    tracker.mod = this._mod;
    this._trackers.add(tracker);
  }

  public AltoClefController getController() {
    return _mod;
  }
}
