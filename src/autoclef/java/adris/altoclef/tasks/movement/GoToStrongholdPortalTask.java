package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class GoToStrongholdPortalTask extends Task {
  private LocateStrongholdCoordinatesTask locateCoordsTask;
  
  private final int targetEyes;
  
  private final int MINIMUM_EYES = 12;
  
  private BlockPos strongholdCoordinates;
  
  public GoToStrongholdPortalTask(int targetEyes) {
    this .targetEyes = targetEyes;
    this .strongholdCoordinates = null;
    this .locateCoordsTask = new LocateStrongholdCoordinatesTask(targetEyes);
  }
  
  protected void onStart() {}
  
  protected Task onTick() {
    AltoClefController mod = controller;
    if (this .strongholdCoordinates == null) {
      this .strongholdCoordinates = this .locateCoordsTask.getStrongholdCoordinates().orElse(null);
      if (this .strongholdCoordinates == null) {
        if (mod.getItemStorage().getItemCount(new Item[] { Items.ENDER_EYE }) < 12 && mod.getEntityTracker().itemDropped(new Item[] { Items.ENDER_EYE })) {
          setDebugState("Picking up dropped eye");
          return (Task)new PickupDroppedItemTask(Items.ENDER_EYE, 12);
        } 
        setDebugState("Triangulating stronghold...");
        return (Task)this .locateCoordsTask;
      } 
    } 
    if (mod.getPlayer().getPos().distanceTo(WorldHelper.toVec3d(this .strongholdCoordinates)) < 10.0D && !mod.getBlockScanner().anyFound(new Block[] { Blocks.END_PORTAL_FRAME })) {
      mod.log("Something went wrong whilst triangulating the stronghold... either the action got disrupted or the second eye went to a different stronghold");
      mod.log("We will try to triangulate again now...");
      this .strongholdCoordinates = null;
      this .locateCoordsTask = new LocateStrongholdCoordinatesTask(this .targetEyes);
      return null;
    } 
    setDebugState("Searching for Stronghold...");
    return (Task)new FastTravelTask(this .strongholdCoordinates, Integer.valueOf(300), true);
  }
  
  protected void onStop(Task interruptTask) {}
  
  protected boolean isEqual(Task other) {
    return other instanceof adris.altoclef.tasks.movement.GoToStrongholdPortalTask;
  }
  
  protected String toDebugString() {
    return "Locating Stronghold";
  }
}
