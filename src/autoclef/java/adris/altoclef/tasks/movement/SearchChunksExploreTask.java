package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SearchChunksExploreTask extends Task {
    private final Object searcherMutex = new Object();

    private final Set<ChunkPos> alreadyExplored = new HashSet<>();

    private ChunkSearchTask searcher;

    protected ChunkPos getBestChunkOverride(AltoClefController mod, List<ChunkPos> chunks) {
        return null;
    }

    protected void onStart() {
        ServerChunkEvents.CHUNK_LOAD.register((evt, chunk) -> {
            onChunkLoad(chunk.getPos());
        });
        resetSearch();
    }

    protected Task onTick() {
        synchronized (this.searcherMutex) {
            if (this.searcher == null) {
                setDebugState("Exploring/Searching for valid chunk");
                return getWanderTask();
            }
            if (this.searcher.isActive() && this.searcher.isFinished()) {
                Debug.logWarning("Target object search failed.");
                this.alreadyExplored.addAll(this.searcher.getSearchedChunks());
                this.searcher = null;
            } else if (this.searcher.finished()) {
                setDebugState("Searching for target object...");
                Debug.logMessage("Search finished.");
                this.alreadyExplored.addAll(this.searcher.getSearchedChunks());
                this.searcher = null;
            }
            setDebugState("Searching within chunks...");
            return (Task) this.searcher;
        }
    }

    protected void onStop(Task interruptTask) {
    }

    private void onChunkLoad(ChunkPos pos) {
        if (this.searcher != null)
            return;
        if (!isActive())
            return;
        if (isChunkWithinSearchSpace(controller, pos))
            synchronized (this.searcherMutex) {
                if (!this.alreadyExplored.contains(pos)) {
                    Debug.logMessage("New searcher: " + String.valueOf(pos));
                    this.searcher = (ChunkSearchTask) new SearchSubTask(pos);
                }
            }
    }

    protected Task getWanderTask() {
        return (Task) new TimeoutWanderTask(true);
    }

    public boolean failedSearch() {
        return (this.searcher == null);
    }

    public void resetSearch() {
        this.searcher = null;
        this.alreadyExplored.clear();
        for (ChunkPos start : controller.getChunkTracker().getLoadedChunks())
            onChunkLoad(start);
    }

    protected abstract boolean isChunkWithinSearchSpace(AltoClefController paramAltoClefController, ChunkPos paramChunkPos);

    class SearchSubTask extends ChunkSearchTask {

        public SearchSubTask(ChunkPos start) {
            super(start);
        }

        @Override
        protected boolean isChunkPartOfSearchSpace(AltoClefController mod, ChunkPos pos) {
            return isChunkWithinSearchSpace(mod, pos);
        }

        @Override
        public ChunkPos getBestChunk(AltoClefController mod, List<ChunkPos> chunks) {
            ChunkPos override = getBestChunkOverride(mod, chunks);
            if (override != null) return override;
            return super.getBestChunk(mod, chunks);
        }

        @Override
        protected boolean isChunkSearchEqual(ChunkSearchTask other) {
            // Since we're keeping track of "_searcher", we expect the subchild routine to ALWAYS be consistent!
            return other == this;//return other instanceof SearchSubTask;
        }

        @Override
        protected String toDebugString() {
            return "Searching chunks...";
        }
    }
}
