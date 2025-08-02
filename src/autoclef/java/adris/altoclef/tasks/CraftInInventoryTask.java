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

public class CraftInInventoryTask extends ResourceTask {

  private final RecipeTarget target;
  private final boolean collect;
  private final boolean ignoreUncataloguedSlots;

  public CraftInInventoryTask(RecipeTarget target, boolean collect, boolean ignoreUncataloguedSlots) {
    super(new ItemTarget(target.getOutputItem(), target.getTargetCount()));
    this .target = target;
    this .collect = collect;
    this .ignoreUncataloguedSlots = ignoreUncataloguedSlots;

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
  }

  @Override
  protected Task onResourceTick(AltoClefController controller) {
    int targetCount = target.getTargetCount();
    Item outputItem = target.getOutputItem();

    if (controller.getItemStorage().getItemCount(outputItem) >= targetCount) {
      return null;
    }

    if (collect && !StorageHelper.hasRecipeMaterialsOrTarget(controller, target)) {
      setDebugState("Collecting ingredients for " + outputItem.getName().getString());
      return new CollectRecipeCataloguedResourcesTask(ignoreUncataloguedSlots, target);
    }

    setDebugState("Crafting " + outputItem.getName().getString());

    int craftsNeeded = (int) Math.ceil((double) (targetCount - controller.getItemStorage().getItemCount(outputItem)) / target.getRecipe().outputCount());
    if (craftsNeeded <= 0) {
      return null;
    }

    LivingEntityInventory inventory = ((IInventoryProvider) controller.getEntity()).getLivingInventory();

    for (int i = 0; i < craftsNeeded; i++) {
      if (!StorageHelper.hasRecipeMaterialsOrTarget(controller, new RecipeTarget(target.getOutputItem(), target.getRecipe().outputCount(), target.getRecipe()))) {
        Debug.logWarning("Failed to craft " + outputItem.getName().getString() + ", not enough ingredients even though we passed the initial check.");
        break;
      }

      for (ItemTarget ingredient : target.getRecipe().getSlots()) {
        if (ingredient == null || ingredient.isEmpty()) continue;
        inventory.remove(stack -> ingredient.matches(stack.getItem()), ingredient.getTargetCount(), inventory);
      }

      ItemStack result = new ItemStack(target.getOutputItem(), target.getRecipe().outputCount());
      inventory.insertStack(result);
      controller.getItemStorage().registerSlotAction();
    }

    return null;
  }

  @Override
  protected void onResourceStop(AltoClefController controller, Task interruptTask) {
  }

  @Override
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof CraftInInventoryTask task) {
      return task .target.equals(this .target);
    }
    return false;
  }

  @Override
  protected String toDebugStringName() {
    return "Craft in inventory: " + target.getOutputItem().getName().getString();
  }

  public RecipeTarget getRecipeTarget(){
    return target;
  }
}