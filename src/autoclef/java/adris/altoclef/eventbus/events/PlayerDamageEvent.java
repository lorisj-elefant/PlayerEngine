package adris.altoclef.eventbus.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;

public class PlayerDamageEvent {
  public Entity target;
  public DamageSource source;
  
  public float damage;
  
  public PlayerDamageEvent(Entity target, DamageSource source, float damage) {
    this.target = target;
    this.source = source;
    this.damage = damage;
  }
}
