package adris.altoclef.control;

import adris.altoclef.AltoClefController;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockBreakingCancelEvent;
import adris.altoclef.eventbus.events.BlockBreakingEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class PlayerExtraController {
    private final AltoClefController mod;

    private BlockPos blockBreakPos;

    public PlayerExtraController(AltoClefController mod) {
        this.mod = mod;
        EventBus.subscribe(BlockBreakingEvent.class, evt -> onBlockBreak(evt.blockPos));
        EventBus.subscribe(BlockBreakingCancelEvent.class, evt -> onBlockStopBreaking());
    }

    private void onBlockBreak(BlockPos pos) {
        this.blockBreakPos = pos;
    }

    private void onBlockStopBreaking() {
        this.blockBreakPos = null;
    }

    public BlockPos getBreakingBlockPos() {
        return this.blockBreakPos;
    }

    public boolean isBreakingBlock() {
        return (this.blockBreakPos != null);
    }

    public boolean inRange(Entity entity) {
        return this.mod.getPlayer().isInRange(entity, this.mod.getModSettings().getEntityReachRange());
    }

    public void attack(Entity entity) {
        if (inRange(entity)) {
            this.mod.getPlayer().tryAttack(entity);
            this.mod.getPlayer().swingHand(Hand.MAIN_HAND);
        }
    }
}
