package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class CarveThenCollectTask extends ResourceTask {
  private final ItemTarget target;
  
  private final Block[] targetBlocks;
  
  private final ItemTarget toCarve;
  
  private final Block[] toCarveBlocks;
  
  private final ItemTarget carveWith;
  
  public CarveThenCollectTask(ItemTarget target, Block[] targetBlocks, ItemTarget toCarve, Block[] toCarveBlocks, ItemTarget carveWith) {
    super(target);
    this .target = target;
    this .targetBlocks = targetBlocks;
    this .toCarve = toCarve;
    this .toCarveBlocks = toCarveBlocks;
    this .carveWith = carveWith;
  }
  
  public CarveThenCollectTask(Item target, int targetCount, Block targetBlock, Item toCarve, Block toCarveBlock, Item carveWith) {
    this(new ItemTarget(target, targetCount), new Block[] { targetBlock }, new ItemTarget(toCarve, targetCount), new Block[] { toCarveBlock }, new ItemTarget(carveWith, 1));
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    if (mod.getBlockScanner().anyFound(this .targetBlocks)) {
      setDebugState("Breaking carved/target block");
      return (Task)new DoToClosestBlockTask(adris.altoclef.tasks.construction.DestroyBlockTask::new, this .targetBlocks);
    } 
    if (!StorageHelper.itemTargetsMetInventory(mod, new ItemTarget[] { this .carveWith })) {
      setDebugState("Collect our carve tool");
      return (Task)TaskCatalogue.getItemTask(this .carveWith);
    } 
    if (mod.getBlockScanner().anyFound(this .toCarveBlocks)) {
      setDebugState("Carving block");
      return (Task)new DoToClosestBlockTask(blockPos -> new InteractWithBlockTask(this .carveWith, blockPos, false), this .toCarveBlocks);
    } 
    int neededCarveItems = this .target.getTargetCount() - mod.getItemStorage().getItemCount(new ItemTarget[] { this .target });
    int currentCarveItems = mod.getItemStorage().getItemCount(new ItemTarget[] { this .toCarve });
    if (neededCarveItems > currentCarveItems) {
      setDebugState("Collecting more blocks to carve");
      return (Task)TaskCatalogue.getItemTask(this .toCarve);
    } 
    setDebugState("Placing blocks to carve down");
    return (Task)new PlaceBlockNearbyTask(this .toCarveBlocks);
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    if (other instanceof adris.altoclef.tasks.resources.CarveThenCollectTask) {
      adris.altoclef.tasks.resources.CarveThenCollectTask task = (adris.altoclef.tasks.resources.CarveThenCollectTask)other;
      return (task .target.equals(this .target) && task .toCarve.equals(this .toCarve) && Arrays.equals(task .targetBlocks, this .targetBlocks) && Arrays.equals(task .toCarveBlocks, this .toCarveBlocks));
    } 
    return false;
  }
  
  protected String toDebugStringName() {
    return "Getting after carving: " + String.valueOf(this .target);
  }
}
