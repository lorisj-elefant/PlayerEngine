package adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;

import java.util.function.Function;

public abstract class PriorityTask {
    private final Function<AltoClefController, Boolean> canCall;

    private final boolean shouldForce;

    private final boolean canCache;

    public final boolean bypassForceCooldown;

    public PriorityTask(Function<AltoClefController, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        this.canCall = canCall;
        this.shouldForce = shouldForce;
        this.canCache = canCache;
        this.bypassForceCooldown = bypassForceCooldown;
    }

    public final double calculatePriority(AltoClefController mod) {
        if (!((Boolean) this.canCall.apply(mod)).booleanValue())
            return Double.NEGATIVE_INFINITY;
        return getPriority(mod);
    }

    public String toString() {
        return getDebugString();
    }

    public abstract Task getTask(AltoClefController paramAltoClefController);

    public abstract String getDebugString();

    protected abstract double getPriority(AltoClefController paramAltoClefController);

    public boolean needCraftingOnStart(AltoClefController mod) {
        return false;
    }

    public boolean shouldForce() {
        return this.shouldForce;
    }

    public boolean canCache() {
        return this.canCache;
    }
}
