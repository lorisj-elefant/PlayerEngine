package adris.altoclef.chains;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.time.Stopwatch;

public class UserTaskChain extends SingleTaskChain {
    private final Stopwatch taskStopwatch = new Stopwatch();

    private Runnable currentOnFinish = null;

    private boolean runningIdleTask;

    private boolean nextTaskIdleFlag;

    public UserTaskChain(TaskRunner runner) {
        super(runner);
    }

    private static String prettyPrintTimeDuration(double seconds) {
        int minutes = (int) (seconds / 60.0D);
        int hours = minutes / 60;
        int days = hours / 24;
        String result = "";
        if (days != 0)
            result = result + result + " days ";
        if (hours != 0)
            result = result + result + " hours ";
        if (minutes != 0)
            result = result + result + " minutes ";
        if (!result.isEmpty())
            result = result + "and ";
        result = result + result;
        return result;
    }

    protected void onTick() {
        if (!AltoClefController.inGame())
            return;
        super.onTick();
    }

    public void cancel(AltoClefController mod) {
        if (this.mainTask != null && this.mainTask.isActive()) {
            stop();
            onTaskFinish(mod);
        }
    }

    public float getPriority() {
        return 50.0F;
    }

    public String getName() {
        return "User Tasks";
    }

    public void runTask(AltoClefController mod, Task task, Runnable onFinish) {
        this.runningIdleTask = this.nextTaskIdleFlag;
        this.nextTaskIdleFlag = false;
        this.currentOnFinish = onFinish;
        if (!this.runningIdleTask)
            Debug.logMessage("User Task Set: " + task.toString());
        mod.getTaskRunner().enable();
        this.taskStopwatch.begin();
        setTask(task);
        if (mod.getModSettings().failedToLoad())
            Debug.logWarning("Settings file failed to load at some point. Check logs for more info, or delete the file to re-load working settings.");
    }

    protected void onTaskFinish(AltoClefController mod) {
        boolean shouldIdle = mod.getModSettings().shouldRunIdleCommandWhenNotActive();
        double seconds = this.taskStopwatch.time();
        Task oldTask = this.mainTask;
        this.mainTask = null;
        if (!shouldIdle) {
            mod.stop();
        } else {
            mod.getBaritone().getPathingBehavior().forceCancel();
            mod.getBaritone().getInputOverrideHandler().clearAllKeys();
        }
        if (this.currentOnFinish != null)
            this.currentOnFinish.run();
        boolean actuallyDone = (this.mainTask == null);
        if (actuallyDone) {
            if (!this.runningIdleTask) {
                Debug.logMessage("User task FINISHED. Took %s seconds.", new Object[]{prettyPrintTimeDuration(seconds)});
            }
            if (shouldIdle) {
                controller.getCommandExecutor().executeWithPrefix(mod.getModSettings().getIdleCommand());
                signalNextTaskToBeIdleTask();
                this.runningIdleTask = true;
            }
        }
    }

    public boolean isRunningIdleTask() {
        return (isActive() && this.runningIdleTask);
    }

    public void signalNextTaskToBeIdleTask() {
        this.nextTaskIdleFlag = true;
    }
}
