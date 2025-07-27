//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package adris.altoclef.trackers.blacklisting;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class EntityLocateBlacklist extends AbstractObjectBlacklist<Entity> {
    public EntityLocateBlacklist() {
    }

    protected Vec3d getPos(Entity item) {
        return item.getPos();
    }
}
