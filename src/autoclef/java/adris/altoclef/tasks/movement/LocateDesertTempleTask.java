package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biomes;

public class LocateDesertTempleTask extends Task {
  private BlockPos finalPos;
  
  protected void onStart() {}
  
  protected Task onTick() {
    BlockPos desertTemplePos = WorldHelper.getADesertTemple(controller);
    if (desertTemplePos != null)
      this .finalPos = desertTemplePos.up(14); 
    if (this .finalPos != null) {
      setDebugState("Going to found desert temple");
      return (Task)new GetToBlockTask(this .finalPos, false);
    } 
    return (Task)new SearchWithinBiomeTask(Biomes.DESERT);
  }
  
  protected void onStop(Task interruptTask) {}
  
  protected boolean isEqual(Task other) {
    return other instanceof adris.altoclef.tasks.movement.LocateDesertTempleTask;
  }
  
  protected String toDebugString() {
    return "Searchin' for temples";
  }
  
  public boolean isFinished() {
    return controller.getPlayer().getBlockPos().equals(this .finalPos);
  }
}
