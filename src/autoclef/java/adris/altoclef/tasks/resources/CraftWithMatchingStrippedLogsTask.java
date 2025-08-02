package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import java.util.function.Function;
import net.minecraft.item.Item;

public class CraftWithMatchingStrippedLogsTask extends CraftWithMatchingMaterialsTask {
  private final ItemTarget visualTarget;
  
  private final Function<ItemHelper.WoodItems, Item> getTargetItem;
  
  public CraftWithMatchingStrippedLogsTask(Item[] validTargets, Function<ItemHelper.WoodItems, Item> getTargetItem, CraftingRecipe recipe, boolean[] sameMask, int count) {
    super(new ItemTarget(validTargets, count), recipe, sameMask);
    this .getTargetItem = getTargetItem;
    this .visualTarget = new ItemTarget(validTargets, count);
  }
  
  protected Task getSpecificSameResourceTask(AltoClefController mod, Item[] toGet) {
    for (Item strippedLogToGet : toGet) {
      Item log = ItemHelper.strippedToLogs(strippedLogToGet);
      if (mod.getItemStorage().getItemCount(new Item[] { log }) >= 1)
        return (Task)TaskCatalogue.getItemTask(strippedLogToGet, 1); 
    } 
    Debug.logError("CraftWithMatchingStrippedLogs: Should never happen!");
    return null;
  }
  
  protected Item getSpecificItemCorrespondingToMajorityResource(Item majority) {
    for (ItemHelper.WoodItems woodItems : ItemHelper.getWoodItems()) {
      if (woodItems.strippedLog == majority)
        return this .getTargetItem.apply(woodItems); 
    } 
    return null;
  }
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.resources.CraftWithMatchingStrippedLogsTask) {
      adris.altoclef.tasks.resources.CraftWithMatchingStrippedLogsTask task = (adris.altoclef.tasks.resources.CraftWithMatchingStrippedLogsTask)other;
      return task .visualTarget.equals(this .visualTarget);
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Getting: " + String.valueOf(this .visualTarget);
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
}
