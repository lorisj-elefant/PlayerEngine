package adris.altoclef.tasks.examples;

import adris.altoclef.AltoClefController;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class ExampleTask extends Task {
  private final int numberOfStonePickaxesToGrab;
  
  private final BlockPos whereToPlaceCobblestone;
  
  public ExampleTask(int numberOfStonePickaxesToGrab, BlockPos whereToPlaceCobblestone) {
    this.numberOfStonePickaxesToGrab = numberOfStonePickaxesToGrab;
    this.whereToPlaceCobblestone = whereToPlaceCobblestone;
  }
  
  protected void onStart() {
    AltoClefController mod = controller;
    mod.getBehaviour().push();
    mod.getBehaviour().addProtectedItems(new Item[] { Items.COBBLESTONE });
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
    if (mod.getItemStorage().getItemCount(new Item[] { Items.STONE_PICKAXE }) < this.numberOfStonePickaxesToGrab)
      return (Task)TaskCatalogue.getItemTask(Items.STONE_PICKAXE, this.numberOfStonePickaxesToGrab); 
    if (!mod.getItemStorage().hasItem(new Item[] { Items.COBBLESTONE }))
      return (Task)TaskCatalogue.getItemTask(Items.COBBLESTONE, 1); 
    if (mod.getChunkTracker().isChunkLoaded(this.whereToPlaceCobblestone)) {
      if (mod.getWorld().getBlockState(this.whereToPlaceCobblestone).getBlock() != Blocks.COBBLESTONE)
        return (Task)new PlaceBlockTask(this.whereToPlaceCobblestone, new Block[] { Blocks.COBBLESTONE }); 
      return null;
    } 
    return (Task)new GetToBlockTask(this.whereToPlaceCobblestone);
  }
  
  protected void onStop(Task interruptTask) {
    controller.getBehaviour().pop();
  }
  
  public boolean isFinished() {
    AltoClefController mod = controller;
    return (
      
      mod.getItemStorage().getItemCount(new Item[] { Items.STONE_PICKAXE }) >= this.numberOfStonePickaxesToGrab && mod
      .getWorld().getBlockState(this.whereToPlaceCobblestone).getBlock() == Blocks.COBBLESTONE);
  }
  
  protected boolean isEqual(Task other) {
    if (other instanceof adris.altoclef.tasks.examples.ExampleTask) {
      adris.altoclef.tasks.examples.ExampleTask task = (adris.altoclef.tasks.examples.ExampleTask)other;
      return (task.numberOfStonePickaxesToGrab == this.numberOfStonePickaxesToGrab && task.whereToPlaceCobblestone
        .equals(this.whereToPlaceCobblestone));
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Boofin";
  }
}
