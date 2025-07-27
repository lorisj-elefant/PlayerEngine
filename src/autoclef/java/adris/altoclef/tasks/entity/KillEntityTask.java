package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClefController;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.Entity;

public class KillEntityTask extends AbstractKillEntityTask {
  private final Entity target;
  
  public KillEntityTask(Entity entity) {
    this.target = entity;
  }
  
  public KillEntityTask(Entity entity, double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
    super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
    this.target = entity;
  }
  
  protected Optional<Entity> getEntityTarget(AltoClefController mod) {
    return Optional.of(this.target);
  }
  
  protected boolean isSubEqual(AbstractDoToEntityTask other) {
    if (other instanceof adris.altoclef.tasks.entity.KillEntityTask) {
      adris.altoclef.tasks.entity.KillEntityTask task = (adris.altoclef.tasks.entity.KillEntityTask)other;
      return Objects.equals(task.target, this.target);
    } 
    return false;
  }
  
  protected String toDebugString() {
    return "Killing " + this.target.getType().getTranslationKey();
  }
}
