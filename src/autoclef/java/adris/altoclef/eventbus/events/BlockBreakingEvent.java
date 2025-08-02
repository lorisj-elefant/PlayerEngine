package adris.altoclef.eventbus.events;

import net.minecraft.util.math.BlockPos;

public class BlockBreakingEvent {
    public BlockPos blockPos;

    public BlockBreakingEvent(BlockPos blockPos) {
        this.blockPos = blockPos;
    }
}
