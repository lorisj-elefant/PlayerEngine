//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package adris.altoclef.util.serialization;

import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collection;

public class BlockPosSerializer extends AbstractVectorSerializer<BlockPos> {
    public BlockPosSerializer() {
    }

    protected Collection<String> getParts(BlockPos value) {
        return Arrays.asList("" + value.getX(), "" + value.getY(), "" + value.getZ());
    }
}
