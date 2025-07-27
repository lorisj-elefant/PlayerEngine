package adris.altoclef.tasks.entity;

import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;

public class KillEntitiesTask extends DoToClosestEntityTask {
  public KillEntitiesTask(Predicate<Entity> shouldKill) {
    super(adris.altoclef.tasks.entity.KillEntityTask::new, e -> (e.isAlive() && shouldKill.test(e)), (Class[])null);
  }
  
  public KillEntitiesTask(Predicate<Entity> shouldKill, Class<?>... entities) {
    super(adris.altoclef.tasks.entity.KillEntityTask::new, e -> (e.isAlive() && shouldKill.test(e)), entities);
    assert entities != null;
  }
  
  public KillEntitiesTask(Class<?>... entities) {
    super(adris.altoclef.tasks.entity.KillEntityTask::new, entities);
    assert entities != null;
  }
}
