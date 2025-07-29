package adris.altoclef.tasks.container;

import adris.altoclef.AltoClefController;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import baritone.api.entity.IInventoryProvider;
import baritone.api.entity.LivingEntityInventory;
import net.minecraft.block.Blocks;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;
import java.util.Optional;

public class UpgradeInSmithingTableTask extends ResourceTask {

  private final ItemTarget _tool;
  private final ItemTarget _template;
  private final ItemTarget _material;
  private final ItemTarget _output;

  private BlockPos _tablePos = null;

  public UpgradeInSmithingTableTask(ItemTarget tool, ItemTarget material, ItemTarget output) {
    super(output);
    this._tool = new ItemTarget(tool, output.getTargetCount());
    this._material = new ItemTarget(material, output.getTargetCount());
    this._template = new ItemTarget(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, output.getTargetCount());
    this._output = output;
  }

  @Override
  protected boolean shouldAvoidPickingUp(AltoClefController controller) {
    return false;
  }

  @Override
  protected void onResourceStart(AltoClefController controller) {
    controller.getBehaviour().addProtectedItems(_tool.getMatches());
    controller.getBehaviour().addProtectedItems(_material.getMatches());
    controller.getBehaviour().addProtectedItems(_template.getMatches());
    controller.getBehaviour().addProtectedItems(Items.SMITHING_TABLE);
  }

  @Override
  protected Task onResourceTick(AltoClefController controller) {
    int desiredOutputCount = _output.getTargetCount();
    int currentOutputCount = controller.getItemStorage().getItemCount(_output);
    if (currentOutputCount >= desiredOutputCount) {
      return null; // We are done
    }

    int needed = desiredOutputCount - currentOutputCount;

    // Ensure we have enough materials.
    if (controller.getItemStorage().getItemCount(_tool) < needed ||
            controller.getItemStorage().getItemCount(_material) < needed ||
            controller.getItemStorage().getItemCount(_template) < needed) {
      setDebugState("Getting materials for upgrade");
      return new CataloguedResourceTask(
              new ItemTarget(_tool, needed),
              new ItemTarget(_material, needed),
              new ItemTarget(_template, needed)
      );
    }

    // If armor is equipped, unequip it.
    if (StorageHelper.isArmorEquipped(controller, _tool.getMatches())) {
      setDebugState("Unequipping armor before upgrading.");
      return new EquipArmorTask(new ItemTarget[0]); // Passing no args unequips all armor. This is a simplification.
    }

    // Find or place a smithing table.
    if (_tablePos == null || !controller.getWorld().getBlockState(_tablePos).isOf(Blocks.SMITHING_TABLE)) {
      Optional<BlockPos> nearestTable = controller.getBlockScanner().getNearestBlock(Blocks.SMITHING_TABLE);
      if (nearestTable.isPresent()) {
        _tablePos = nearestTable.get();
      } else {
        if (controller.getItemStorage().hasItem(Items.SMITHING_TABLE)) {
          setDebugState("Placing smithing table.");
          return new PlaceBlockNearbyTask(Blocks.SMITHING_TABLE);
        }
        setDebugState("Obtaining smithing table.");
        return TaskCatalogue.getItemTask(Items.SMITHING_TABLE, 1);
      }
    }

    // Go to the smithing table
    if (!_tablePos.isWithinDistance(new Vec3i((int) controller.getEntity().getPos().x, (int) controller.getEntity().getPos().y, (int) controller.getEntity().getPos().z), 4.5)) {
      setDebugState("Going to smithing table.");
      return new GetToBlockTask(_tablePos);
    }

    // Perform the upgrade (server-side simulation)
    setDebugState("Upgrading item...");

    LivingEntityInventory inventory = ((IInventoryProvider) controller.getEntity()).getLivingInventory();

    // Consume one of each ingredient
    inventory.remove(stack -> _template.matches(stack.getItem()), 1, inventory);
    inventory.remove(stack -> _tool.matches(stack.getItem()), 1, inventory);
    inventory.remove(stack -> _material.matches(stack.getItem()), 1, inventory);

    // Add the output
    inventory.insertStack(new ItemStack(_output.getMatches()[0], 1));

    controller.getItemStorage().registerSlotAction();

    // Since this is instant, we let the main loop re-evaluate.
    // If more items are needed, the task will run again.
    return null;
  }

  @Override
  protected void onResourceStop(AltoClefController controller, Task interruptTask) {
    controller.getBehaviour().pop();
  }

  @Override
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof UpgradeInSmithingTableTask task) {
      return task._tool.equals(this._tool) &&
              task._output.equals(this._output) &&
              task._material.equals(this._material);
    }
    return false;
  }

  @Override
  protected String toDebugStringName() {
    return "Upgrading in Smithing Table";
  }

  public ItemTarget getMaterials(){
    return _material;
  }

  public ItemTarget getTools(){
    return _tool;
  }

  public ItemTarget getTemplate(){
    return _template;
  }
}