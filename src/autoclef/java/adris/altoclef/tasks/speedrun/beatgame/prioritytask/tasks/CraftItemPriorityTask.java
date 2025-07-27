package adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.CraftingHelper;
import java.util.function.Function;

public class CraftItemPriorityTask extends PriorityTask {
  public final double priority;
  
  public final RecipeTarget recipeTarget;
  
  private boolean satisfied = false;
  
  public CraftItemPriorityTask(double priority, RecipeTarget toCraft) {
    this(priority, toCraft, mod -> Boolean.valueOf(true));
  }
  
  public CraftItemPriorityTask(double priority, RecipeTarget toCraft, Function<AltoClefController, Boolean> canCall) {
    this(priority, toCraft, canCall, false, true, true);
  }
  
  public CraftItemPriorityTask(double priority, RecipeTarget toCraft, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
    this(priority, toCraft, mod -> Boolean.valueOf(true), shouldForce, canCache, bypassForceCooldown);
  }
  
  public CraftItemPriorityTask(double priority, RecipeTarget toCraft, Function<AltoClefController, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
    super(canCall, shouldForce, canCache, bypassForceCooldown);
    this.priority = priority;
    this.recipeTarget = toCraft;
  }
  
  public Task getTask(AltoClefController mod) {
    if (this.recipeTarget.getRecipe().isBig())
      return (Task)new CraftInTableTask(this.recipeTarget); 
    return (Task)new CraftInInventoryTask(this.recipeTarget);
  }
  
  public String getDebugString() {
    return "Crafting " + String.valueOf(this.recipeTarget);
  }
  
  protected double getPriority(AltoClefController mod) {
    if (BeatMinecraftTask.hasItem(mod, this.recipeTarget.getOutputItem())) {
      Debug.logInternal("THIS IS SATISFIED " + String.valueOf(this.recipeTarget.getOutputItem()));
      this.satisfied = true;
    } 
    Debug.logInternal("NOT SATISFIED");
    if (this.satisfied)
      return Double.NEGATIVE_INFINITY; 
    return this.priority;
  }
  
  public boolean needCraftingOnStart(AltoClefController mod) {
    return CraftingHelper.canCraftItemNow(mod, this.recipeTarget.getOutputItem());
  }
  
  public boolean isSatisfied() {
    return this.satisfied;
  }
}
