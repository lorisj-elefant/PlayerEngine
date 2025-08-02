package adris.altoclef.tasks.container;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.resources.CollectRecipeCataloguedResourcesTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.StorageHelper;
import baritone.api.entity.IInventoryProvider;
import baritone.api.entity.LivingEntityInventory;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.unmapped.C_rcqaryar;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CraftInTableTask extends ResourceTask {

  private final RecipeTarget[] targets;
  private BlockPos craftingTablePos = null;

  public CraftInTableTask(RecipeTarget[] targets) {
    super(extractItemTargets(targets));
    this .targets = targets;
  }

  public CraftInTableTask(RecipeTarget target) {
    this(new RecipeTarget[]{target});
  }

  private static ItemTarget[] extractItemTargets(RecipeTarget[] recipeTargets) {
    return Arrays.stream(recipeTargets)
            .map(t -> new ItemTarget(t.getOutputItem(), t.getTargetCount()))
            .toArray(ItemTarget[]::new);
  }

  @Override
  protected boolean shouldAvoidPickingUp(AltoClefController controller) {
    return false;
  }

  @Override
  protected void onResourceStart(AltoClefController controller) {
    // Protect items to avoid accidentally throwing them away
    for (RecipeTarget target : targets) {
      for (ItemTarget ingredient : target.getRecipe().getSlots()) {
        if (ingredient != null && !ingredient.isEmpty()) {
          controller.getBehaviour().addProtectedItems(ingredient.getMatches());
        }
      }
    }
  }

  @Override
  protected Task onResourceTick(AltoClefController controller) {
    // 1. Check if all crafts are already done
    boolean allDone = Arrays.stream(targets).allMatch(target ->
            controller.getItemStorage().getItemCount(target.getOutputItem()) >= target.getTargetCount()
    );
    if (allDone) {
      return null; // All target items have been crafted
    }

    // 2. Collect resources for crafting
    if (!StorageHelper.hasRecipeMaterialsOrTarget(controller, targets)) {
      setDebugState("Collecting ingredients");
      return new CollectRecipeCataloguedResourcesTask(false, targets);
    }

    // 3. Find or place a crafting table
    if (craftingTablePos == null || !controller.getWorld().getBlockState(craftingTablePos).isOf(Blocks.CRAFTING_TABLE)) {
      Optional<BlockPos> nearestTable = controller.getBlockScanner().getNearestBlock(Blocks.CRAFTING_TABLE);
      if (nearestTable.isPresent()) {
        craftingTablePos = nearestTable.get();
        setDebugState("Found crafting table: " + craftingTablePos.toShortString());
      } else {
        craftingTablePos = null;
        setDebugState("Crafting table not found.");
      }
    }

    // 4. If table is needed but missing or far, get/build one
    if (craftingTablePos == null) {
      if (controller.getItemStorage().hasItem(Items.CRAFTING_TABLE)) {
        setDebugState("Placing crafting table.");
        return new PlaceBlockNearbyTask(Blocks.CRAFTING_TABLE);
      }
      setDebugState("Obtaining crafting table.");
      return TaskCatalogue.getItemTask(Items.CRAFTING_TABLE, 1);
    }

    // 5. Go to the crafting table
    if (!craftingTablePos.isWithinDistance(new Vec3i((int) controller.getEntity().getPos().x, (int) controller.getEntity().getPos().y, (int) controller.getEntity().getPos().z), 3.5)) {
      setDebugState("Going to crafting table at: " + craftingTablePos.toShortString());
      return new GetToBlockTask(craftingTablePos);
    }

    // 6. Perform the craft
    setDebugState("Crafting...");
    for (RecipeTarget target : targets) {
      int currentAmount = controller.getItemStorage().getItemCount(target.getOutputItem());
      if (currentAmount < target.getTargetCount()) {
        // How many we need to craft
        int craftsNeeded = (int) Math.ceil((double) (target.getTargetCount() - currentAmount) / target.getRecipe().outputCount());

        for (int i = 0; i < craftsNeeded; i++) {
          if (!StorageHelper.hasRecipeMaterialsOrTarget(controller, new RecipeTarget(target.getOutputItem(), target.getRecipe().outputCount(), target.getRecipe()))) {
            Debug.logWarning("Not enough ingredients to craft, even though the check passed. Aborting.");
            return new CollectRecipeCataloguedResourcesTask(false, targets);
          }

          // Consume ingredients
          LivingEntityInventory inventory = ((IInventoryProvider) controller.getEntity()).getLivingInventory();
          for (ItemTarget ingredient : target.getRecipe().getSlots()) {
            if (ingredient != null && !ingredient.isEmpty()) {
              inventory.remove(stack -> ingredient.matches(stack.getItem()), ingredient.getTargetCount(), inventory);
            }
          }

          // Add result
          ItemStack result = new ItemStack(target.getOutputItem(), target.getRecipe().outputCount());
          inventory.insertStack(result);
          controller.getItemStorage().registerSlotAction();
        }
      }
    }

    // This task is now effectively synchronous and should complete in one tick once conditions are met.
    return null;
  }

  @Override
  protected void onResourceStop(AltoClefController controller, Task interruptTask) {
    controller.getBehaviour().pop();
  }

  @Override
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof CraftInTableTask task) {
      return Arrays.equals(task .targets, this .targets);
    }
    return false;
  }

  @Override
  protected String toDebugStringName() {
    return "Craft on table: " + Arrays.toString(
            Arrays.stream(targets).map(t -> t.getOutputItem().getName().getString()).toArray()
    );
  }

  public RecipeTarget[] getRecipeTargets(){
    return targets;
  }
}