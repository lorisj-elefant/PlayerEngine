package adris.altoclef.tasks.movement;

import adris.altoclef.Playground;
import adris.altoclef.tasksystem.Task;

public class IdleTask extends Task {
    protected void onStart() {
    }

    protected Task onTick() {
        Playground.IDLE_TEST_TICK_FUNCTION(controller);
        return null;
    }

    protected void onStop(Task interruptTask) {
    }

    public boolean isFinished() {
        return false;
    }

    protected boolean isEqual(Task other) {
        return other instanceof adris.altoclef.tasks.movement.IdleTask;
    }

    protected String toDebugString() {
        return "Idle";
    }
}
