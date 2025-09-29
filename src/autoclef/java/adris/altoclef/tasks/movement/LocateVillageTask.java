package adris.altoclef.tasks.movement;

import java.util.Map;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class LocateVillageTask extends SearchChunksExploreTask {

    // https://github.com/st3rbenn/VillageFinder-1.19.3/blob/main/src/main/java/com/anthonincolas/villagefinder/Item/VillageCompassProperties.java
    // @Override
    // protected void onStart() {
    // }

    // @Override
    // protected Task onTick() {
    // BlockPos villagePos = WorldHelper.getAVillage(this.controller);
    // if (villagePos != null) {
    // this.finalPos = villagePos.above(14);
    // }

    // if (this.finalPos != null) {
    // this.setDebugState("Going to found village");
    // return new GetToBlockTask(this.finalPos, false);
    // } else {
    // return new SearchWithinBiomeTask(Biomes.DESERT);
    // }
    // }

    // @Override
    // protected void onStop(Task interruptTask) {
    // }

    // @Override
    // protected boolean isEqual(Task other) {
    // return other instanceof LocateDesertTempleTask;
    // }

    // @Override
    // protected String toDebugString() {
    // return "Searchin' for temples";
    // }

    // @Override
    // public boolean isFinished() {
    // return this.controller.getPlayer().blockPosition().equals(this.finalPos);
    // }

    public LocateVillageTask() {

    }

    @Override
    protected boolean isChunkWithinSearchSpace(AltoClefController var1, ChunkPos var2) {
        Map<Structure, StructureStart> structures = var1.getWorld().getChunk(var1.getPlayer().getOnPos())
                .getAllStarts();
        boolean hasStructure = false;

        for (var entry : structures.entrySet()) {
            Structure feature = entry.getKey();
            System.out.println(feature.toString());
            hasStructure = true;
        }
        return hasStructure;
    }

    @Override
    protected boolean isEqual(Task var1) {
        // TODO Auto-generated method stub
        return var1 instanceof LocateVillageTask;
    }

    @Override
    protected String toDebugString() {
        // TODO Auto-generated method stub
        return "Searching for village";
    }
}
