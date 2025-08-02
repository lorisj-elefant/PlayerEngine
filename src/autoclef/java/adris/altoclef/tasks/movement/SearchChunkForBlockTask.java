package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import net.minecraft.block.Block;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashSet;

public class SearchChunkForBlockTask extends SearchChunksExploreTask {
    private final HashSet<Block> toSearchFor = new HashSet<>();

    public SearchChunkForBlockTask(Block... blocks) {
        this.toSearchFor.addAll(Arrays.asList(blocks));
    }

    protected boolean isChunkWithinSearchSpace(AltoClefController mod, ChunkPos pos) {
        return mod.getChunkTracker().scanChunk(pos, (block) -> {
            return this.toSearchFor.contains(mod.getWorld().getBlockState(block).getBlock());
        });
    }

    protected boolean isEqual(Task other) {
        if (other instanceof adris.altoclef.tasks.movement.SearchChunkForBlockTask) {
            adris.altoclef.tasks.movement.SearchChunkForBlockTask blockTask = (adris.altoclef.tasks.movement.SearchChunkForBlockTask) other;
            return Arrays.equals(blockTask.toSearchFor.toArray(x$0 -> new Block[x$0]), this.toSearchFor.toArray(x$0 -> new Block[x$0]));
        }
        return false;
    }

    protected String toDebugString() {
        return "Searching chunk for blocks " + ArrayUtils.toString(this.toSearchFor.toArray(x$0 -> new Block[x$0]));
    }
}
