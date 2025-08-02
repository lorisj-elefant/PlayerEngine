package adris.altoclef.tasks.examples;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Optional;

public class ExampleTask2 extends Task {
    private BlockPos target = null;

    protected void onStart() {
        AltoClefController mod = controller;
        mod.getBehaviour().push();
        mod.getBehaviour().avoidBlockBreaking(blockPos -> {
            BlockState s = mod.getWorld().getBlockState(blockPos);
            return (s.getBlock() == Blocks.OAK_LEAVES || s.getBlock() == Blocks.OAK_LOG);
        });
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        if (this.target != null)
            return (Task) new GetToBlockTask(this.target);
        if (mod.getBlockScanner().anyFound(new Block[]{Blocks.OAK_LOG})) {
            Optional<BlockPos> nearest = mod.getBlockScanner().getNearestBlock(new Block[]{Blocks.OAK_LOG});
            if (nearest.isPresent()) {
                BlockPos check = new BlockPos((Vec3i) nearest.get());
                while (mod.getWorld().getBlockState(check).getBlock() == Blocks.OAK_LOG || mod
                        .getWorld().getBlockState(check).getBlock() == Blocks.OAK_LEAVES)
                    check = check.up();
                this.target = check;
            }
            return null;
        }
        return (Task) new TimeoutWanderTask();
    }

    protected void onStop(Task interruptTask) {
        controller.getBehaviour().pop();
    }

    protected boolean isEqual(Task other) {
        return other instanceof adris.altoclef.tasks.examples.ExampleTask2;
    }

    public boolean isFinished() {
        if (this.target != null)
            return controller.getPlayer().getBlockPos().equals(this.target);
        return super.isFinished();
    }

    protected String toDebugString() {
        return "Standing on a tree";
    }
}
