package adris.altoclef.tasks.movement;

import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.IBaritone;
import baritone.api.utils.input.Input;

public class SafeRandomShimmyTask extends Task {
    private final TimerGame lookTimer;

    public SafeRandomShimmyTask(float randomLookInterval) {
        this.lookTimer = new TimerGame(randomLookInterval);
    }

    public SafeRandomShimmyTask() {
        this(5.0F);
    }

    protected void onStart() {
        this.lookTimer.reset();
    }

    protected Task onTick() {
        if (this.lookTimer.elapsed()) {
            Debug.logMessage("Random Orientation");
            this.lookTimer.reset();
            LookHelper.randomOrientation(controller);
        }
        IBaritone baritone = controller.getBaritone();
        baritone.getInputOverrideHandler().setInputForceState(Input.SNEAK, true);
        baritone.getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
        return null;
    }

    protected void onStop(Task interruptTask) {
        IBaritone baritone = controller.getBaritone();
        baritone.getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
        baritone.getInputOverrideHandler().setInputForceState(Input.SNEAK, false);
        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, false);
    }

    protected boolean isEqual(Task other) {
        return other instanceof adris.altoclef.tasks.movement.SafeRandomShimmyTask;
    }

    protected String toDebugString() {
        return "Shimmying";
    }
}
