package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.multiversion.world.WorldVer;
import adris.altoclef.tasksystem.Task;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class SearchWithinBiomeTask extends SearchChunksExploreTask {
    private final RegistryKey<Biome> toSearch;

    public SearchWithinBiomeTask(RegistryKey<Biome> toSearch) {
        this.toSearch = toSearch;
    }

    protected boolean isChunkWithinSearchSpace(AltoClefController mod, ChunkPos pos) {
        return WorldVer.isBiomeAtPos((World) mod.getWorld(), this.toSearch, pos.getStartPos().add(1, 1, 1));
    }

    protected boolean isEqual(Task other) {
        if (other instanceof adris.altoclef.tasks.movement.SearchWithinBiomeTask) {
            adris.altoclef.tasks.movement.SearchWithinBiomeTask task = (adris.altoclef.tasks.movement.SearchWithinBiomeTask) other;
            return (task.toSearch == this.toSearch);
        }
        return false;
    }

    protected String toDebugString() {
        return "Searching for+within biome: " + String.valueOf(this.toSearch);
    }
}
