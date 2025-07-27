package adris.altoclef.trackers;

import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.WorldHelper;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class EntityStuckTracker extends Tracker {
  final float MOB_RANGE = 25.0F;
  
  private final Set<BlockPos> _blockedSpots = new HashSet<>();
  
  public EntityStuckTracker(TrackerManager manager) {
    super(manager);
  }
  
  public boolean isBlockedByEntity(BlockPos pos) {
    ensureUpdated();
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      return this._blockedSpots.contains(pos);
    } 
  }
  
  protected synchronized void updateState() {
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      this._blockedSpots.clear();
      LivingEntity clientPlayerEntity = mod.getEntity();
      for (Entity entity : mod.getWorld().iterateEntities()) {
        if (entity == null || !entity.isAlive())
          continue; 
        if (entity.equals(clientPlayerEntity))
          continue; 
        if (!clientPlayerEntity.isInRange(entity, 25.0D))
          continue; 
        Box b = entity.getBoundingBox();
        for (BlockPos p : WorldHelper.getBlocksTouchingBox(b))
          this._blockedSpots.add(p); 
      } 
    } 
  }
  
  protected void reset() {
    this._blockedSpots.clear();
  }
}
