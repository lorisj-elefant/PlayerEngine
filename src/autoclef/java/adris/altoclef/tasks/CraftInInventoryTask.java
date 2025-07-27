// File: adris/altoclef/tasks/CraftInInventoryTask.java
package adris.altoclef.tasks;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasks.resources.CollectRecipeCataloguedResourcesTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.StorageHelper;
import baritone.api.entity.IInventoryProvider;
import baritone.api.entity.LivingEntityInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

public class CraftInInventoryTask extends ResourceTask {

  private final RecipeTarget _target;
  private final boolean _collect;
  private final boolean _ignoreUncataloguedSlots;

  public CraftInInventoryTask(RecipeTarget target, boolean collect, boolean ignoreUncataloguedSlots) {
    super(new ItemTarget(target.getOutputItem(), target.getTargetCount()));
    this._target = target;
    this._collect = collect;
    this._ignoreUncataloguedSlots = ignoreUncataloguedSlots;

    if (target.getRecipe().isBig()) {
      Debug.logError("CraftInInventoryTask was used for a 3x3 recipe. This is not supported. Use CraftInTableTask instead.");
    }
  }

  public CraftInInventoryTask(RecipeTarget target) {
    this(target, true, false);
  }

  @Override
  protected boolean shouldAvoidPickingUp(AltoClefController controller) {
    return false;
  }

  @Override
  protected void onResourceStart(AltoClefController controller) {
    // No setup needed as we are not interacting with a screen.
  }

  @Override
  protected Task onResourceTick(AltoClefController controller) {
    int targetCount = _target.getTargetCount();
    Item outputItem = _target.getOutputItem();

    // If we have enough, we are done.
    if (controller.getItemStorage().getItemCount(outputItem) >= targetCount) {
      return null;
    }

    // Collect resources if we need to.
    if (_collect && !StorageHelper.hasRecipeMaterialsOrTarget(controller, _target)) {
      setDebugState("Collecting ingredients for " + outputItem.getName().getString());
      return new CollectRecipeCataloguedResourcesTask(_ignoreUncataloguedSlots, _target);
    }

    // Now, craft.
    setDebugState("Crafting " + outputItem.getName().getString());

    // How many times we need to craft
    int craftsNeeded = (int) Math.ceil((double) (targetCount - controller.getItemStorage().getItemCount(outputItem)) / _target.getRecipe().outputCount());
    if (craftsNeeded <= 0) {
      return null;
    }

    LivingEntityInventory inventory = ((IInventoryProvider) controller.getEntity()).getLivingInventory();

    for (int i = 0; i < craftsNeeded; i++) {
      // Ensure we have ingredients for ONE craft.
      if (!StorageHelper.hasRecipeMaterialsOrTarget(controller, new RecipeTarget(_target.getOutputItem(), _target.getRecipe().outputCount(), _target.getRecipe()))) {
        Debug.logWarning("Failed to craft " + outputItem.getName().getString() + ", not enough ingredients even though we passed the initial check.");
        break;
      }

      // Consume ingredients
      for (ItemTarget ingredient : _target.getRecipe().getSlots()) {
        if (ingredient == null || ingredient.isEmpty()) continue;
        inventory.remove(stack -> ingredient.matches(stack.getItem()), ingredient.getTargetCount(), inventory);
      }

      // Add result
      ItemStack result = new ItemStack(_target.getOutputItem(), _target.getRecipe().outputCount());
      inventory.insertStack(result);
      controller.getItemStorage().registerSlotAction();
    }

    // Crafting is instant on the server, so we are done in one tick.
    return null;
  }

  @Override
  protected void onResourceStop(AltoClefController controller, Task interruptTask) {
    // No cleanup needed.
  }

  @Override
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof CraftInInventoryTask task) {
      return task._target.equals(this._target);
    }
    return false;
  }

  @Override
  protected String toDebugStringName() {
    return "Craft in inventory: " + _target.getOutputItem().getName().getString();
  }

  public RecipeTarget getRecipeTarget(){
    return _target;
  }
}