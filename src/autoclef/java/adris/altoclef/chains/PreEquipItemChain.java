package adris.altoclef.chains;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.entity.AbstractKillEntityTask;
import adris.altoclef.tasksystem.TaskChain;
import adris.altoclef.tasksystem.TaskRunner;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.movement.IMovement;
import baritone.pathing.movement.Movement;
import baritone.utils.BlockStateInterface;
import java.util.Optional;

public class PreEquipItemChain extends SingleTaskChain {
  public PreEquipItemChain(TaskRunner runner) {
    super(runner);
  }
  
  protected void onTaskFinish(AltoClefController mod) {}
  
  public float getPriority() {
    update(controller);
    return -1.0F;
  }
  
  private void update(AltoClefController mod) {
    if (mod.getFoodChain().isTryingToEat())
      return; 
    TaskChain currentChain = mod.getTaskRunner().getCurrentTaskChain();
    if (currentChain == null)
      return; 
    Optional<IPath> pathOptional = mod.getBaritone().getPathingBehavior().getPath();
    if (pathOptional.isEmpty())
      return; 
    IPath path = pathOptional.get();
    BlockStateInterface bsi = new BlockStateInterface(controller.getBaritone().getEntityContext());
    for (IMovement iMovement : path.movements()) {
      Movement movement = (Movement)iMovement;
      if (movement.toBreak(bsi).stream().anyMatch(pos -> (mod.getWorld().getBlockState(pos).getBlock().getHardness() > 0.0F)) || 
        !movement.toPlace(bsi).isEmpty())
        return; 
    } 
    if (currentChain.getTasks().stream().anyMatch(task -> task instanceof AbstractKillEntityTask))
      AbstractKillEntityTask.equipWeapon(mod); 
  }
  
  public String getName() {
    return "pre-equip item chain";
  }
  
  public boolean isActive() {
    return true;
  }
}
