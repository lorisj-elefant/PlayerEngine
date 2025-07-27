package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;

public class ShearSheepTask extends AbstractDoToEntityTask {
  public ShearSheepTask() {
    super(0.0D, -1.0D, -1.0D);
  }
  
  protected boolean isSubEqual(AbstractDoToEntityTask other) {
    return other instanceof adris.altoclef.tasks.entity.ShearSheepTask;
  }
  
  protected Task onEntityInteract(AltoClefController mod, Entity entity) {
    if (!mod.getItemStorage().hasItem(new Item[] { Items.SHEARS })) {
      Debug.logWarning("Failed to shear sheep because you have no shears.");
      return null;
    } 
    if (mod.getSlotHandler().forceEquipItem(Items.SHEARS)){
      ((SheepEntity)entity).sheared(SoundCategory.PLAYERS);
      mod.getPlayer().getMainHandStack().damage(1, mod.getPlayer(), (e)->{});
    }
    return null;
  }
  
  protected Optional<Entity> getEntityTarget(AltoClefController mod) {
    return mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), entity -> {
          if (entity instanceof SheepEntity) {
            SheepEntity sheep = (SheepEntity)entity;
            return (sheep.isShearable() && !sheep.isSheared());
          } 
          return false;
        },new Class[] { SheepEntity.class });
  }
  
  protected String toDebugString() {
    return "Shearing Sheep";
  }
}
