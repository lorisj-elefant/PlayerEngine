package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class OneCycleTask extends Task {
  TimerGame placeBedTimer = new TimerGame(0.6D);
  
  TimerGame waiTimer = new TimerGame(0.3D);
  
  double prevDist = 100.0D;
  
  protected Task onTick() {
    AltoClefController mod = controller;
    mod.getFoodChain().shouldStop(true);
    mod.getSlotHandler().forceEquipItemToOffhand(Items.AIR);
    if (mod.getInputControls().isHeldDown(Input.SNEAK))
      mod.getInputControls().release(Input.SNEAK); 
    List<EnderDragonEntity> dragons = mod.getEntityTracker().getTrackedEntities(EnderDragonEntity.class);
    if (dragons.size() != 1)
      mod.log("No dragon? :("); 
    for (EnderDragonEntity dragon : dragons) {
      BlockPos endPortalTop = KillEnderDragonWithBedsTask.locateExitPortalTop(mod).up();
      BlockPos obsidian = null;
      Direction dir = null;
      for (Direction direction : new Direction[] { Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH }) {
        if (mod.getWorld().getBlockState(endPortalTop.offset(direction)).getBlock().equals(Blocks.OBSIDIAN)) {
          obsidian = endPortalTop.offset(direction);
          dir = direction.getOpposite();
          break;
        } 
      } 
      if (dir == null) {
        mod.log("no obisidan? :(");
        return null;
      } 
      Direction offsetDir = (dir.getAxis() == Direction.Axis.X) ? Direction.SOUTH : Direction.WEST;
      BlockPos targetBlock = endPortalTop.down(3).offset(offsetDir, 3).offset(dir);
      double d = distanceIgnoreY(WorldHelper.toVec3d(targetBlock), mod.getPlayer().getPos());
      if (d > 0.7D) {
        mod.log("" + d);
        return (Task)new GetToBlockTask(targetBlock);
      } 
      LookHelper.lookAt(mod, obsidian, dir);
      BlockPos bedHead = WorldHelper.getBedHead(mod, endPortalTop);
      BlockPos bedTargetPosition = endPortalTop.up();
      mod.getSlotHandler().forceEquipItem(ItemHelper.BED);
      if (bedHead == null) {
        if (this.placeBedTimer.elapsed() && Math.abs(dragon.getY() - endPortalTop.getY()) < 10.0D) {
          mod.getInputControls().tryPress(Input.CLICK_RIGHT);
          this.waiTimer.reset();
        } 
        continue;
      } 
      if (!this.waiTimer.elapsed())
        return null; 
      Vec3d dragonHeadPos = dragon.head.getBoundingBox().getCenter();
      Vec3d bedHeadPos = WorldHelper.toVec3d(bedHead);
      double dist = dragonHeadPos.distanceTo(bedHeadPos);
      double distXZ = distanceIgnoreY(dragonHeadPos, bedHeadPos);
      EnderDragonPart body = dragon.getBodyParts()[2];
      double destroyDistance = Math.abs(body.getBoundingBox().getMin(Direction.Axis.Y) - bedHeadPos.getY());
      boolean tooClose = (destroyDistance < 1.1D);
      boolean skip = (destroyDistance > 3.0D && dist > 4.5D && distXZ > 2.5D);
      mod.log("" + destroyDistance + " : " + destroyDistance + " : " + dist);
      if (dist < 1.5D || (this.prevDist < distXZ && destroyDistance < 4.0D && this.prevDist < 2.9D) || (destroyDistance < 2.0D && dist < 4.0D) || (destroyDistance < 1.7D && dist < 4.5D) || tooClose || (destroyDistance < 2.4D && distXZ < 3.7D) || (destroyDistance < 3.5D && distXZ < 2.4D))
        if (!skip) {
          mod.getInputControls().tryPress(Input.CLICK_RIGHT);
          this.placeBedTimer.reset();
        }  
      this.prevDist = distXZ;
      double d1 = dragonHeadPos.getY() - bedHead.getY();
    } 
    return null;
  }
  
  public double distanceIgnoreY(Vec3d vec, Vec3d vec1) {
    double d = vec.x - vec1.x;
    double f = vec.z - vec1.z;
    return Math.sqrt(d * d + f * f);
  }
  
  protected void onStop(Task interruptTask) {}
  
  public boolean isFinished() {
    return controller.getEntityTracker().getTrackedEntities(EnderDragonEntity.class).isEmpty();
  }
  
  protected void onStart() {}
  
  protected boolean isEqual(Task other) {
    return false;
  }
  
  protected String toDebugString() {
    return "One cycling bby";
  }
}
