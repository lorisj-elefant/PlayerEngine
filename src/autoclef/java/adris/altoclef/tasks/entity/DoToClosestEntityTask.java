//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.AbstractDoToClosestObjectTask;
import adris.altoclef.tasksystem.Task;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class DoToClosestEntityTask extends AbstractDoToClosestObjectTask<Entity> {
    private final Class[] targetEntities;
    private final Supplier<Vec3d> getOriginPos;
    private final Function<Entity, Task> getTargetTask;
    private final Predicate<Entity> shouldInteractWith;

    public DoToClosestEntityTask(Supplier<Vec3d> getOriginSupplier, Function<Entity, Task> getTargetTask, Predicate<Entity> shouldInteractWith, Class... entities) {
        this.getOriginPos = getOriginSupplier;
        this.getTargetTask = getTargetTask;
        this.shouldInteractWith = shouldInteractWith;
        this.targetEntities = entities;
    }

    public DoToClosestEntityTask(Supplier<Vec3d> getOriginSupplier, Function<Entity, Task> getTargetTask, Class... entities) {
        this(getOriginSupplier, getTargetTask, (entity) -> true, entities);
    }

    public DoToClosestEntityTask(Function<Entity, Task> getTargetTask, Predicate<Entity> shouldInteractWith, Class... entities) {
        this((Supplier)null, getTargetTask, shouldInteractWith, entities);
    }

    public DoToClosestEntityTask(Function<Entity, Task> getTargetTask, Class... entities) {
        this((Supplier)null, getTargetTask, (entity) -> true, entities);
    }

    protected Vec3d getPos(AltoClefController mod, Entity obj) {
        return obj.getPos();
    }

    protected Optional<Entity> getClosestTo(AltoClefController mod, Vec3d pos) {
        return !mod.getEntityTracker().entityFound(this.targetEntities) ? Optional.empty() : mod.getEntityTracker().getClosestEntity(pos, this.shouldInteractWith, this.targetEntities);
    }

    protected Vec3d getOriginPos(AltoClefController mod) {
        return this.getOriginPos != null ? (Vec3d)this.getOriginPos.get() : mod.getPlayer().getPos();
    }

    protected Task getGoalTask(Entity obj) {
        return (Task)this.getTargetTask.apply(obj);
    }

    protected boolean isValid(AltoClefController mod, Entity obj) {
        return obj.isAlive() && mod.getEntityTracker().isEntityReachable(obj);
    }

    protected void onStart() {
    }

    protected void onStop(Task interruptTask) {
    }

    protected boolean isEqual(Task other) {
        if (other instanceof DoToClosestEntityTask task) {
            return Arrays.equals(task.targetEntities, this.targetEntities);
        } else {
            return false;
        }
    }

    protected String toDebugString() {
        return "Doing something to closest entity...";
    }
}
