package adris.altoclef.tasksystem;

import adris.altoclef.AltoClefController;

import java.util.ArrayList;
import java.util.List;

public abstract class TaskChain {

    protected AltoClefController controller;

    private final List<Task> cachedTaskChain = new ArrayList<>();

    public TaskChain(TaskRunner runner) {
        runner.addTaskChain(this);
        controller = runner.getMod();
    }

    public void tick() {
        cachedTaskChain.clear();
        onTick();
    }

    public void stop() {
        cachedTaskChain.clear();
        onStop();
    }

    protected abstract void onStop();

    public abstract void onInterrupt(TaskChain other);

    protected abstract void onTick();

    public abstract float getPriority();

    public abstract boolean isActive();

    public abstract String getName();

    public List<Task> getTasks() {
        return cachedTaskChain;
    }

    void addTaskToChain(Task task) {
        cachedTaskChain.add(task);
    }

    public String toString() {
        return getName();
    }

}