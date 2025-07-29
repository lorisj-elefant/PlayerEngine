package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.StorageHelper;
import java.util.Arrays;
import java.util.HashMap;
import net.minecraft.item.Item;
import org.apache.commons.lang3.ArrayUtils;

public class CollectRecipeCataloguedResourcesTask extends Task {
  private final RecipeTarget[] _targets;
  
  private final boolean _ignoreUncataloguedSlots;
  
  private boolean _finished = false;
  
  public CollectRecipeCataloguedResourcesTask(boolean ignoreUncataloguedSlots, RecipeTarget... targets) {
    this._targets = targets;
    this._ignoreUncataloguedSlots = ignoreUncataloguedSlots;
  }
  
  protected void onStart() {
    this._finished = false;
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
    HashMap<String, Integer> catalogueCount = new HashMap<>();
    HashMap<Item, Integer> itemCount = new HashMap<>();
    for (RecipeTarget target : this._targets) {
      if (target != null) {
        int weNeed = target.getTargetCount() - mod.getItemStorage().getItemCount(new Item[] { target.getOutputItem() });
        if (weNeed > 0) {
          CraftingRecipe recipe = target.getRecipe();
          for (int i = 0; i < recipe.getSlotCount(); i++) {
            ItemTarget slot = recipe.getSlot(i);
            if (slot == null || slot.isEmpty()) continue;
            int numberOfRepeats = (int) Math.floor(-0.1 + (double) weNeed / target.getRecipe().outputCount()) + 1;
            if (!slot.isCatalogueItem()) {
              if (slot.getMatches().length != 1) {
                if (!_ignoreUncataloguedSlots) {
                  Debug.logWarning("Recipe collection for recipe " + recipe + " slot " + i
                          + " is not catalogued. Please define an explicit"
                          + " collectRecipeSubTask() function for this item target:" + slot
                  );
                }
              } else {
                Item item = slot.getMatches()[0];
                itemCount.put(item, itemCount.getOrDefault(item, 0) + numberOfRepeats);
              }
            } else {
              String targetName = slot.getCatalogueName();
              catalogueCount.put(targetName, catalogueCount.getOrDefault(targetName, 0) + numberOfRepeats);
            }
          } 
        } 
      } 
    } 
    for (String catalogueMaterialName : catalogueCount.keySet()) {
      int count = ((Integer)catalogueCount.get(catalogueMaterialName)).intValue();
      ItemTarget itemTarget = new ItemTarget(catalogueMaterialName, count);
      if (count > 0 && !StorageHelper.itemTargetsMet(mod, new ItemTarget[] { itemTarget })) {
        setDebugState("Getting " + String.valueOf(itemTarget));
        return (Task)TaskCatalogue.getItemTask(catalogueMaterialName, count);
      } 
    } 
    for (Item item : itemCount.keySet()) {
      int count = ((Integer)itemCount.get(item)).intValue();
      if (count > 0 && 
        mod.getItemStorage().getItemCount(new Item[] { item }) < count) {
        setDebugState("Getting " + item.getTranslationKey());
        return (Task)TaskCatalogue.getItemTask(item, count);
      } 
    } 
    this._finished = true;
    return null;
  }
  
  protected void onStop(Task interruptTask) {}
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.resources.CollectRecipeCataloguedResourcesTask) {
      adris.altoclef.tasks.resources.CollectRecipeCataloguedResourcesTask task = (adris.altoclef.tasks.resources.CollectRecipeCataloguedResourcesTask)other;
      return Arrays.equals(task._targets, this._targets);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Collect Recipe Resources: " + ArrayUtils.toString(this._targets);
  }
  
  public boolean isFinished() {
    if (this._finished && 
      !StorageHelper.hasRecipeMaterialsOrTarget(controller, this._targets)) {
      this._finished = false;
      Debug.logMessage("Invalid collect recipe \"finished\" state, resetting.");
    } 
    return this._finished;
  }
}
