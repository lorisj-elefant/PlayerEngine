package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.util.math.BlockPos;

public class GetToBlockTask extends CustomBaritoneGoalTask implements ITaskRequiresGrounded {
    private final BlockPos position;

    private final boolean preferStairs;

    private final Dimension dimension;

    private int finishedTicks = 0;

    private final TimerGame wanderTimer = new TimerGame(2.0D);

    public GetToBlockTask(BlockPos position, boolean preferStairs) {
        this(position, preferStairs, null);
    }

    public GetToBlockTask(BlockPos position, Dimension dimension) {
        this(position, false, dimension);
    }

    public GetToBlockTask(BlockPos position, boolean preferStairs, Dimension dimension) {
        this.dimension = dimension;
        this.position = position;
        this.preferStairs = preferStairs;
    }

    public GetToBlockTask(BlockPos position) {
        this(position, false);
    }

    protected Task onTick() {
        if (this.dimension != null && WorldHelper.getCurrentDimension(controller) != this.dimension)
            return (Task) new DefaultGoToDimensionTask(this.dimension);
        if (isFinished()) {
            this.finishedTicks++;
        } else {
            this.finishedTicks = 0;
        }
        if (this.finishedTicks > 200) {
            this.wanderTimer.reset();
            Debug.logWarning("GetToBlock was finished for 10 seconds yet is still being called, wandering");
            this.finishedTicks = 0;
            return (Task) new TimeoutWanderTask();
        }
        if (!this.wanderTimer.elapsed())
            return (Task) new TimeoutWanderTask();
        return super.onTick();
    }

    protected void onStart() {
        super.onStart();
        if (this.preferStairs) {
            controller.getBehaviour().push();
            controller.getBehaviour().setPreferredStairs(true);
        }
    }

    protected void onStop(Task interruptTask) {
        super.onStop(interruptTask);
        if (this.preferStairs)
            controller.getBehaviour().pop();
    }

    protected boolean isEqual(Task other) {
        if (other instanceof adris.altoclef.tasks.movement.GetToBlockTask) {
            adris.altoclef.tasks.movement.GetToBlockTask task = (adris.altoclef.tasks.movement.GetToBlockTask) other;
            return (task.position.equals(this.position) && task.preferStairs == this.preferStairs && task.dimension == this.dimension);
        }
        return false;
    }

    public boolean isFinished() {
        return (super.isFinished() && (this.dimension == null || this.dimension == WorldHelper.getCurrentDimension(controller)));
    }

    protected String toDebugString() {
        return "Getting to block " + String.valueOf(this.position) + ((this.dimension != null) ? (" in dimension " + String.valueOf(this.dimension)) : "");
    }

    protected Goal newGoal(AltoClefController mod) {
        return (Goal) new GoalBlock(this.position);
    }

    protected void onWander(AltoClefController mod) {
        super.onWander(mod);
        mod.getBlockScanner().requestBlockUnreachable(this.position);
    }
}
