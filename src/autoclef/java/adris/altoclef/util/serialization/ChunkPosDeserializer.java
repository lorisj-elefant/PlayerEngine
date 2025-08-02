//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package adris.altoclef.util.serialization;

import com.fasterxml.jackson.core.JsonToken;
import net.minecraft.util.math.ChunkPos;

import java.util.List;

public class ChunkPosDeserializer extends AbstractVectorDeserializer<ChunkPos, Integer> {
    public ChunkPosDeserializer() {
    }

    protected String getTypeName() {
        return "ChunkPos";
    }

    protected String[] getComponents() {
        return new String[]{"x", "z"};
    }

    protected Integer parseUnit(String unit) throws Exception {
        return Integer.parseInt(unit);
    }

    protected ChunkPos deserializeFromUnits(List<Integer> units) {
        return new ChunkPos((Integer) units.get(0), (Integer) units.get(1));
    }

    protected boolean isUnitTokenValid(JsonToken unitToken) {
        return false;
    }
}
