//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package adris.altoclef.trackers.blacklisting;

import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class WorldLocateBlacklist extends AbstractObjectBlacklist<BlockPos> {
    public WorldLocateBlacklist() {
    }

    protected Vec3d getPos(BlockPos item) {
        return WorldHelper.toVec3d(item);
    }
}
