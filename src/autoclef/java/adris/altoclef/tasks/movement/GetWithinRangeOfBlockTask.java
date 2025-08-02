package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import net.minecraft.util.math.BlockPos;

public class GetWithinRangeOfBlockTask extends CustomBaritoneGoalTask {
    public final BlockPos blockPos;

    public final int range;

    public GetWithinRangeOfBlockTask(BlockPos blockPos, int range) {
        this.blockPos = blockPos;
        this.range = range;
    }

    protected Goal newGoal(AltoClefController mod) {
        return (Goal) new GoalNear(this.blockPos, this.range);
    }

    protected boolean isEqual(Task other) {
        if (other instanceof adris.altoclef.tasks.movement.GetWithinRangeOfBlockTask) {
            adris.altoclef.tasks.movement.GetWithinRangeOfBlockTask task = (adris.altoclef.tasks.movement.GetWithinRangeOfBlockTask) other;
            return (task.blockPos.equals(this.blockPos) && task.range == this.range);
        }
        return false;
    }

    protected String toDebugString() {
        return "Getting within " + this.range + " blocks of " + this.blockPos.toShortString();
    }
}
