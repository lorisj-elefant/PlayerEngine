package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.ShearSheepTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.ItemHelper;
import java.util.Arrays;
import java.util.HashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;

public class CollectWoolTask extends ResourceTask {
  private final int _count;
  
  private final HashSet<DyeColor> _colors;
  
  private final Item[] _wools;
  
  public CollectWoolTask(DyeColor[] colors, int count) {
    super(new ItemTarget(ItemHelper.WOOL, count));
    this._colors = new HashSet<>(Arrays.asList(colors));
    this._count = count;
    this._wools = getWoolColorItems(colors);
  }
  
  public CollectWoolTask(DyeColor color, int count) {
    this(new DyeColor[] { color }, count);
  }
  
  public CollectWoolTask(int count) {
    this(DyeColor.values(), count);
  }
  
  private static Item[] getWoolColorItems(DyeColor[] colors) {
    Item[] result = new Item[colors.length];
    for (int i = 0; i < result.length; i++)
      result[i] = (ItemHelper.getColorfulItems(colors[i])).wool; 
    return result;
  }
  
  protected boolean shouldAvoidPickingUp(AltoClefController mod) {
    return false;
  }
  
  protected void onResourceStart(AltoClefController mod) {}
  
  protected Task onResourceTick(AltoClefController mod) {
    Block[] woolBlocks = ItemHelper.itemsToBlocks(this._wools);
    if (mod.getBlockScanner().anyFound(woolBlocks))
      return (Task)new MineAndCollectTask(new ItemTarget(this._wools), woolBlocks, MiningRequirement.HAND); 
    if (isInWrongDimension(mod) && !mod.getEntityTracker().entityFound(new Class[] { SheepEntity.class }))
      return getToCorrectDimensionTask(mod); 
    if (mod.getItemStorage().hasItem(new Item[] { Items.SHEARS }))
      return (Task)new ShearSheepTask(); 
    return (Task)new KillAndLootTask(SheepEntity.class, entity -> {
          if (entity instanceof SheepEntity) {
            SheepEntity sheep = (SheepEntity)entity;
            return (this._colors.contains(sheep.getColor()) && !sheep.isSheared());
          } 
          return false;
        }, new ItemTarget[] { new ItemTarget(this._wools, this._count) });
  }
  
  protected void onResourceStop(AltoClefController mod, Task interruptTask) {}
  
  protected boolean isEqualResource(ResourceTask other) {
    return (other instanceof adris.altoclef.tasks.resources.CollectWoolTask && ((adris.altoclef.tasks.resources.CollectWoolTask)other)._count == this._count);
  }
  
  protected String toDebugStringName() {
    return "Collect " + this._count + " wool.";
  }
}
