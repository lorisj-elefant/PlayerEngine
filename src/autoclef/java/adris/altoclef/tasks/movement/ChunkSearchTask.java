package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.ChunkLoadEvent;
import adris.altoclef.tasksystem.Task;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

abstract class ChunkSearchTask extends Task {
  private final BlockPos _startPoint;
  
  private final Object _searchMutex = new Object();
  
  private final Set<ChunkPos> _consideredAlready = new HashSet<>();
  
  private final Set<ChunkPos> _searchedAlready = new HashSet<>();
  
  private final ArrayList<ChunkPos> _searchLater = new ArrayList<>();
  
  private final ArrayList<ChunkPos> _justLoaded = new ArrayList<>();
  
  private boolean _first = true;
  
  private boolean _finished = false;
  
  private Subscription<ChunkLoadEvent> _onChunkLoad;
  
  public ChunkSearchTask(BlockPos startPoint) {
    this._startPoint = startPoint;
  }
  
  public ChunkSearchTask(ChunkPos chunkPos) {
    this(chunkPos.getStartPos().add(1, 1, 1));
  }
  
  public Set<ChunkPos> getSearchedChunks() {
    return this._searchedAlready;
  }
  
  public boolean finished() {
    return this._finished;
  }
  
  protected void onStart() {
    if (this._first) {
      this._finished = false;
      this._first = false;
      ChunkPos startPos = controller.getWorld().getChunk(this._startPoint).getPos();
      synchronized (this._searchMutex) {
        searchChunkOrQueueSearch(controller, startPos);
      } 
    } 
    this._onChunkLoad = EventBus.subscribe(ChunkLoadEvent.class, evt -> {
          WorldChunk chunk = evt.chunk;
          if (chunk == null)
            return; 
          synchronized (this._searchMutex) {
            if (!this._searchedAlready.contains(chunk.getPos()))
              this._justLoaded.add(chunk.getPos()); 
          } 
        });
  }
  
  protected Task onTick() {
    synchronized (this._searchMutex) {
      if (!this._justLoaded.isEmpty())
        for (ChunkPos justLoaded : this._justLoaded) {
          if (this._searchLater.contains(justLoaded))
            if (trySearchChunk(controller, justLoaded))
              this._searchLater.remove(justLoaded);  
        }  
      this._justLoaded.clear();
    } 
    ChunkPos closest = getBestChunk(controller, this._searchLater);
    if (closest == null) {
      this._finished = true;
      Debug.logWarning("Failed to find any chunks to go to. If we finish, that means we scanned all possible chunks.");
      return null;
    } 
    return (Task)new GetToChunkTask(closest);
  }
  
  protected ChunkPos getBestChunk(AltoClefController mod, List<ChunkPos> chunks) {
    double lowestScore = Double.POSITIVE_INFINITY;
    ChunkPos bestChunk = null;
    if (!chunks.isEmpty())
      for (ChunkPos toSearch : chunks) {
        double cx = (toSearch.getStartX() + toSearch.getEndX() + 1) / 2.0D, cz = (toSearch.getStartZ() + toSearch.getEndZ() + 1) / 2.0D;
        double px = mod.getPlayer().getX(), pz = mod.getPlayer().getZ();
        double distanceSq = (cx - px) * (cx - px) + (cz - pz) * (cz - pz);
        double distanceToCenterSq = (new Vec3d(this._startPoint.getX() - cx, 0.0D, this._startPoint.getZ() - cz)).lengthSquared();
        double score = distanceSq + distanceToCenterSq * 0.8D;
        if (score < lowestScore) {
          lowestScore = score;
          bestChunk = toSearch;
        } 
      }  
    return bestChunk;
  }
  
  protected void onStop(Task interruptTask) {
    EventBus.unsubscribe(this._onChunkLoad);
  }
  
  public boolean isFinished() {
    return (this._searchLater.size() == 0);
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.ChunkSearchTask) {
      adris.altoclef.tasks.movement.ChunkSearchTask task = (adris.altoclef.tasks.movement.ChunkSearchTask)other;
      if (!task._startPoint.equals(this._startPoint))
        return false; 
      return isChunkSearchEqual(task);
    } 
    return false;
  }
  
  private void searchChunkOrQueueSearch(AltoClefController mod, ChunkPos pos) {
    if (this._consideredAlready.contains(pos))
      return; 
    this._consideredAlready.add(pos);
    if (!trySearchChunk(mod, pos))
      if (!this._searchedAlready.contains(pos))
        this._searchLater.add(pos);  
  }
  
  private boolean trySearchChunk(AltoClefController mod, ChunkPos pos) {
    if (this._searchedAlready.contains(pos))
      return true; 
    if (mod.getChunkTracker().isChunkLoaded(pos)) {
      this._searchedAlready.add(pos);
      if (isChunkPartOfSearchSpace(mod, pos)) {
        searchChunkOrQueueSearch(mod, new ChunkPos(pos.x + 1, pos.z));
        searchChunkOrQueueSearch(mod, new ChunkPos(pos.x - 1, pos.z));
        searchChunkOrQueueSearch(mod, new ChunkPos(pos.x, pos.z + 1));
        searchChunkOrQueueSearch(mod, new ChunkPos(pos.x, pos.z - 1));
      } 
      return true;
    } 
    return false;
  }
  
  protected abstract boolean isChunkPartOfSearchSpace(AltoClefController paramAltoClefController, ChunkPos paramChunkPos);
  
  protected abstract boolean isChunkSearchEqual(adris.altoclef.tasks.movement.ChunkSearchTask paramChunkSearchTask);
}
