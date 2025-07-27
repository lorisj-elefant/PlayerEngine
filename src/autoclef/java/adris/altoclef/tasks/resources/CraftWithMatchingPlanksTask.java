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

public class CraftWithMatchingPlanksTask extends CraftWithMatchingMaterialsTask {
  private final ItemTarget _visualTarget;
  
  private final Function<ItemHelper.WoodItems, Item> _getTargetItem;
  
  public CraftWithMatchingPlanksTask(Item[] validTargets, Function<ItemHelper.WoodItems, Item> getTargetItem, CraftingRecipe recipe, boolean[] sameMask, int count) {
    super(new ItemTarget(validTargets, count), recipe, sameMask);
    this._getTargetItem = getTargetItem;
    this._visualTarget = new ItemTarget(validTargets, count);
  }
  
  protected int getExpectedTotalCountOfSameItem(AltoClefController mod, Item sameItem) {
    return mod.getItemStorage().getItemCount(new Item[] { sameItem }) + mod.getItemStorage().getItemCount(new Item[] { ItemHelper.planksToLog(sameItem) }) * 4;
  }
  
  protected Task getSpecificSameResourceTask(AltoClefController mod, Item[] toGet) {
    for (Item plankToGet : toGet) {
      Item log = ItemHelper.planksToLog(plankToGet);
      if (mod.getItemStorage().getItemCount(new Item[] { log }) >= 1)
        return (Task)TaskCatalogue.getItemTask(plankToGet, 1); 
    } 
    Debug.logError("CraftWithMatchingPlanks: Should never happen!");
    return null;
  }
  
  protected Item getSpecificItemCorrespondingToMajorityResource(Item majority) {
    for (ItemHelper.WoodItems woodItems : ItemHelper.getWoodItems()) {
      if (woodItems.planks == majority)
        return this._getTargetItem.apply(woodItems); 
    } 
    return null;
  }
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.resources.CraftWithMatchingPlanksTask) {
      adris.altoclef.tasks.resources.CraftWithMatchingPlanksTask task = (adris.altoclef.tasks.resources.CraftWithMatchingPlanksTask)other;
      return task._visualTarget.equals(this._visualTarget);
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Crafting: " + String.valueOf(this._visualTarget);
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
}
