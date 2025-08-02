package adris.altoclef.tasks.construction;

import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class PutOutFireTask extends Task {
    private final BlockPos firePosition;

    public PutOutFireTask(BlockPos firePosition) {
        this.firePosition = firePosition;
    }

    protected void onStart() {
    }

    protected Task onTick() {
        return (Task) new InteractWithBlockTask(ItemTarget.EMPTY, null, this.firePosition, Input.CLICK_LEFT, false, false);
    }

    protected void onStop(Task interruptTask) {
    }

    public boolean isFinished() {
        BlockState s = controller.getWorld().getBlockState(this.firePosition);
        return (s.getBlock() != Blocks.FIRE && s.getBlock() != Blocks.SOUL_FIRE);
    }

    protected boolean isEqual(Task other) {
        if (other instanceof adris.altoclef.tasks.construction.PutOutFireTask) {
            adris.altoclef.tasks.construction.PutOutFireTask task = (adris.altoclef.tasks.construction.PutOutFireTask) other;
            return task.firePosition.equals(this.firePosition);
        }
        return false;
    }

    protected String toDebugString() {
        return "Putting out fire at " + String.valueOf(this.firePosition);
    }
}
