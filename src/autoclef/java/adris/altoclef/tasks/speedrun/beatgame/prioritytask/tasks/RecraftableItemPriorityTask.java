package adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import adris.altoclef.AltoClefController;
import adris.altoclef.util.RecipeTarget;
import java.util.function.Function;

public class RecraftableItemPriorityTask extends CraftItemPriorityTask {
  private final double recraftPriority;
  
  public RecraftableItemPriorityTask(double priority, double recraftPriority, RecipeTarget toCraft, Function<AltoClefController, Boolean> canCall) {
    super(priority, toCraft, canCall);
    this.recraftPriority = recraftPriority;
  }
  
  protected double getPriority(AltoClefController mod) {
    if (isSatisfied())
      return this.recraftPriority; 
    return super.getPriority(mod);
  }
}
