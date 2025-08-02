package adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators;

public class StaticItemPriorityCalculator extends ItemPriorityCalculator {
    private final int priority;

    public static adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.StaticItemPriorityCalculator of(int priority) {
        return new adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.StaticItemPriorityCalculator(priority, 1, 1);
    }

    public StaticItemPriorityCalculator(int priority, int minCount, int maxCount) {
        super(minCount, maxCount);
        this.priority = priority;
    }

    double calculatePriority(int count) {
        return this.priority;
    }
}
