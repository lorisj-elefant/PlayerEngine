//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package adris.altoclef.util.serialization;

import com.fasterxml.jackson.core.JsonToken;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class BlockPosDeserializer extends AbstractVectorDeserializer<BlockPos, Integer> {
    public BlockPosDeserializer() {
    }

    protected String getTypeName() {
        return "BlockPos";
    }

    protected String[] getComponents() {
        return new String[]{"x", "y", "z"};
    }

    protected Integer parseUnit(String unit) throws Exception {
        return Integer.parseInt(unit);
    }

    protected BlockPos deserializeFromUnits(List<Integer> units) {
        return new BlockPos((Integer) units.get(0), (Integer) units.get(1), (Integer) units.get(2));
    }

    protected boolean isUnitTokenValid(JsonToken token) {
        return token == JsonToken.VALUE_NUMBER_INT;
    }
}
