package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.tasksystem.Task;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

abstract class ChunkSearchTask extends Task {
  private final BlockPos startPoint;
  
  private final Object searchMutex = new Object();
  
  private final Set<ChunkPos> consideredAlready = new HashSet<>();
  
  private final Set<ChunkPos> searchedAlready = new HashSet<>();
  
  private final ArrayList<ChunkPos> searchLater = new ArrayList<>();
  
  private final ArrayList<ChunkPos> justLoaded = new ArrayList<>();
  
  private boolean first = true;
  
  private boolean finished = false;
  
  public ChunkSearchTask(BlockPos startPoint) {
    this .startPoint = startPoint;
  }
  
  public ChunkSearchTask(ChunkPos chunkPos) {
    this(chunkPos.getStartPos().add(1, 1, 1));
  }
  
  public Set<ChunkPos> getSearchedChunks() {
    return this .searchedAlready;
  }
  
  public boolean finished() {
    return this .finished;
  }
  
  protected void onStart() {
    if (this .first) {
      this .finished = false;
      this .first = false;
      ChunkPos startPos = controller.getWorld().getChunk(this .startPoint).getPos();
      synchronized (this .searchMutex) {
        searchChunkOrQueueSearch(controller, startPos);
      } 
    }
    ServerChunkEvents.CHUNK_LOAD.register((evt, chunk)->{
      if (chunk == null)
        return;
      synchronized (this .searchMutex) {
        if (!this .searchedAlready.contains(chunk.getPos()))
          this .justLoaded.add(chunk.getPos());
      }
    });
  }
  
  protected Task onTick() {
    synchronized (this .searchMutex) {
      if (!this .justLoaded.isEmpty())
        for (ChunkPos justLoaded : this .justLoaded) {
          if (this .searchLater.contains(justLoaded))
            if (trySearchChunk(controller, justLoaded))
              this .searchLater.remove(justLoaded);  
        }  
      this .justLoaded.clear();
    } 
    ChunkPos closest = getBestChunk(controller, this .searchLater);
    if (closest == null) {
      this .finished = true;
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
        double distanceToCenterSq = (new Vec3d(this .startPoint.getX() - cx, 0.0D, this .startPoint.getZ() - cz)).lengthSquared();
        double score = distanceSq + distanceToCenterSq * 0.8D;
        if (score < lowestScore) {
          lowestScore = score;
          bestChunk = toSearch;
        } 
      }  
    return bestChunk;
  }
  
  protected void onStop(Task interruptTask) {
  }
  
  public boolean isFinished() {
    return (this .searchLater.size() == 0);
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.movement.ChunkSearchTask) {
      adris.altoclef.tasks.movement.ChunkSearchTask task = (adris.altoclef.tasks.movement.ChunkSearchTask)other;
      if (!task .startPoint.equals(this .startPoint))
        return false; 
      return isChunkSearchEqual(task);
    } 
    return false;
  }
  
  private void searchChunkOrQueueSearch(AltoClefController mod, ChunkPos pos) {
    if (this .consideredAlready.contains(pos))
      return; 
    this .consideredAlready.add(pos);
    if (!trySearchChunk(mod, pos))
      if (!this .searchedAlready.contains(pos))
        this .searchLater.add(pos);  
  }
  
  private boolean trySearchChunk(AltoClefController mod, ChunkPos pos) {
    if (this .searchedAlready.contains(pos))
      return true; 
    if (mod.getChunkTracker().isChunkLoaded(pos)) {
      this .searchedAlready.add(pos);
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
