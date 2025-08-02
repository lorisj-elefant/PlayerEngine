package adris.altoclef.trackers;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleChunkTracker {
    private final AltoClefController mod;

    private final Set<ChunkPos> loaded = new HashSet<>();

    public SimpleChunkTracker(AltoClefController mod) {
        this.mod = mod;
        ServerChunkEvents.CHUNK_LOAD.register((evt, chunk) -> {
            onLoad(chunk.getPos());
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((evt, chunk) -> {
            onUnload(chunk.getPos());
        });
    }

    private void onLoad(ChunkPos pos) {
        this.loaded.add(pos);
    }

    private void onUnload(ChunkPos pos) {
        this.loaded.remove(pos);
    }

    public boolean isChunkLoaded(ChunkPos pos) {
        return !(this.mod.getWorld().getChunk(pos.x, pos.z) instanceof net.minecraft.world.chunk.EmptyChunk);
    }

    public boolean isChunkLoaded(BlockPos pos) {
        return isChunkLoaded(new ChunkPos(pos));
    }

    public List<ChunkPos> getLoadedChunks() {
        List<ChunkPos> result = new ArrayList<>(this.loaded);
        result = (List<ChunkPos>) result.stream().filter(this::isChunkLoaded).distinct().collect(Collectors.toList());
        return result;
    }

    public boolean scanChunk(ChunkPos chunk, Predicate<BlockPos> onBlockStop) {
        if (!isChunkLoaded(chunk))
            return false;
        int bottomY = this.mod.getWorld().getBottomY();
        int topY = this.mod.getWorld().getTopY();
        for (int xx = chunk.getStartX(); xx <= chunk.getEndX(); xx++) {
            for (int yy = bottomY; yy <= topY; yy++) {
                for (int zz = chunk.getStartZ(); zz <= chunk.getEndZ(); zz++) {
                    if (onBlockStop.test(new BlockPos(xx, yy, zz)))
                        return true;
                }
            }
        }
        return false;
    }

    public void scanChunk(ChunkPos chunk, Consumer<BlockPos> onBlock) {
        scanChunk(chunk, block -> {
            onBlock.accept(block);
            return false;
        });
    }

    public void reset(AltoClefController mod) {
        Debug.logInternal("CHUNKS RESET");
        this.loaded.clear();
    }
}
