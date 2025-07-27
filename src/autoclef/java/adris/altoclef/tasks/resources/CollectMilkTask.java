package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.AbstractDoToEntityTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.Optional;

public class CollectMilkTask extends ResourceTask {
  private final int count;
  
  public CollectMilkTask(int targetCount) {
    super(Items.MILK_BUCKET, targetCount);
    this.count = targetCount;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    if (!mod.getItemStorage().hasItem(new Item[] { Items.BUCKET }))
      return (Task)TaskCatalogue.getItemTask(Items.BUCKET, 1); 
    if (!mod.getEntityTracker().entityFound(new Class[] { CowEntity.class }) && isInWrongDimension(mod))
      return getToCorrectDimensionTask(mod); 
    return (Task)new MilkCowTask();
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    return other instanceof adris.altoclef.tasks.resources.CollectMilkTask;
  }
  
  protected String toDebugStringName() {
    return "Collecting " + this.count + " milk buckets.";
  }

  static class MilkCowTask extends AbstractDoToEntityTask {

    public MilkCowTask() {
      super(0, -1, -1);
    }

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
      return other instanceof MilkCowTask;
    }

    @Override
    protected Task onEntityInteract(AltoClefController mod, Entity entity) {
      if (!mod.getItemStorage().hasItem(Items.BUCKET)) {
        Debug.logWarning("Failed to milk cow because you have no bucket.");
        return null;
      }
      if (mod.getSlotHandler().forceEquipItem(Items.BUCKET)) {
        mod.getInventory().setStack(mod.getInventory().selectedSlot, new ItemStack(Items.MILK_BUCKET));
      }
      return null;
    }

    @Override
    protected Optional<Entity> getEntityTarget(AltoClefController mod) {
      return mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), CowEntity.class);
    }

    @Override
    protected String toDebugString() {
      return "Milking Cow";
    }
  }
}
