//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package adris.altoclef.util.serialization;

import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Collection;

public class Vec3dSerializer extends AbstractVectorSerializer<Vec3d> {
    public Vec3dSerializer() {
    }

    protected Collection<String> getParts(Vec3d value) {
        return Arrays.asList("" + value.getX(), "" + value.getY(), "" + value.getZ());
    }
}
