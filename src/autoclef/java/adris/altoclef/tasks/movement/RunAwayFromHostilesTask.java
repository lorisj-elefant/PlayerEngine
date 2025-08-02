package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalRunAwayFromEntities;
import adris.altoclef.util.helpers.BaritoneHelper;
import baritone.api.pathing.goals.Goal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SkeletonEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunAwayFromHostilesTask extends CustomBaritoneGoalTask {
    private final double distanceToRun;

    private final boolean includeSkeletons;

    public RunAwayFromHostilesTask(double distance, boolean includeSkeletons) {
        this.distanceToRun = distance;
        this.includeSkeletons = includeSkeletons;
    }

    public RunAwayFromHostilesTask(double distance) {
        this(distance, false);
    }

    protected Goal newGoal(AltoClefController mod) {
        mod.getBaritone().getPathingBehavior().forceCancel();
        return (Goal) new GoalRunAwayFromHostiles(mod, this.distanceToRun);
    }

    protected boolean isEqual(Task other) {
        if (other instanceof adris.altoclef.tasks.movement.RunAwayFromHostilesTask) {
            adris.altoclef.tasks.movement.RunAwayFromHostilesTask task = (adris.altoclef.tasks.movement.RunAwayFromHostilesTask) other;
            return (Math.abs(task.distanceToRun - this.distanceToRun) < 1.0D);
        }
        return false;
    }

    protected String toDebugString() {
        return "NIGERUNDAYOO, SUMOOKEYY! distance=" + this.distanceToRun + ", skeletons=" + this.includeSkeletons;
    }

    private class GoalRunAwayFromHostiles extends GoalRunAwayFromEntities {

        public GoalRunAwayFromHostiles(AltoClefController mod, double distance) {
            super(mod, distance, false, 0.8);
        }

        @Override
        protected List<Entity> getEntities(AltoClefController mod) {
            Stream<LivingEntity> stream = mod.getEntityTracker().getHostiles().stream();
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                if (!includeSkeletons) {
                    stream = stream.filter(hostile -> !(hostile instanceof SkeletonEntity));
                }
                return stream.collect(Collectors.toList());
            }
        }
    }
}
