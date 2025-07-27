package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClefController;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.entity.Entity;

public class KillPlayerTask extends AbstractKillEntityTask {
  private String playerName;
  
  public KillPlayerTask(String playerName) {
    this.playerName = playerName;
  }
  
  public KillPlayerTask(String playerName, double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
    super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
    this.playerName = playerName;
  }
  
  protected Optional<Entity> getEntityTarget(AltoClefController mod) {
    for (Entity entity : controller.getWorld().iterateEntities()) {
      if (entity.isAlive() && 
        entity instanceof net.minecraft.entity.player.PlayerEntity) {
        String playerName = entity.getName().getString().toLowerCase();
        if (playerName != null && playerName.equals(this.playerName.toLowerCase()))
          return Optional.of(entity); 
      } 
    } 
    return Optional.empty();
  }
  
  protected boolean isSubEqual(AbstractDoToEntityTask other) {
    if (other instanceof adris.altoclef.tasks.entity.KillPlayerTask) {
      adris.altoclef.tasks.entity.KillPlayerTask task = (adris.altoclef.tasks.entity.KillPlayerTask)other;
      return Objects.equals(task.playerName, this.playerName);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Killing Player " + this.playerName;
  }
}
