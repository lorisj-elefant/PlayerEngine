package adris.altoclef.tasks.misc;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.UseItemOnPositionTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;

public class UseItemOnBlockTask extends Task {
    String itemName;
    boolean isFinished = false;
    Block block;
    private static final Logger LOGGER = LogManager.getLogger();

    public UseItemOnBlockTask(String itemName, Block block) {
        this.itemName = itemName;
        this.block = block;
    }

    @Override
    protected boolean isEqual(Task var1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void onStart() {
        isFinished = false;
    }

    @Override
    protected void onStop(Task var1) {
        // TODO Auto-generated method stub

    }

    @Override
    protected Task onTick() {
        AltoClefController mod = this.controller;
        Optional<BlockPos> ma = mod.getBlockScanner().getNearestBlock(block);
        if (ma.isEmpty()) {
            LOGGER.warn("No block %s found nearby", block.getName().toString());
            this.fail(String.format("No block (%s) found nearby",
                    block.getName().toString()));
            // isFinished = true;
            return null;
        }
        BlockPos pos = ma.get();
        UseItemOnPositionTask task = new UseItemOnPositionTask(itemName, pos, () -> {
            // what to do when close enough:
            LOGGER.info("Running close enough inside");
            mod.copiedServerPlayer.getPlayerCopy().interact(mod.copiedServerPlayer.getPlayerCopy(),
                    InteractionHand.MAIN_HAND);
            mod.copiedServerPlayer.getPlayerCopy().interactAt(mod.copiedServerPlayer.getPlayerCopy(),
                    pos.getCenter(),
                    InteractionHand.MAIN_HAND);
            // mod.copiedServerPlayer.getPlayerCopy().getItemInHand(InteractionHand.MAIN_HAND)
            // mod.getInteractionManager().processRightClick(mod.getPlayer(),
            // mod.getWorld(), InteractionHand.OFF_HAND);
            isFinished = true;
            return null;
        });
        if (task.isFinished()) {
            isFinished = true;
            return null;
        }

        return task;
    }

    @Override
    protected String toDebugString() {
        return String.format("Use %s on block %s", itemName, block.getName().getString());
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

}
