package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class ProjectileProtectionWallTask extends Task implements ITaskRequiresGrounded {
  private final AltoClefController mod;
  
  private final TimerGame waitForBlockPlacement = new TimerGame(2.0D);
  
  private BlockPos targetPlacePos;
  
  public ProjectileProtectionWallTask(AltoClefController mod) {
    this.mod = mod;
  }
  
  protected void onStart() {
    this.waitForBlockPlacement.forceElapse();
  }
  
  protected Task onTick() {
    if (this.targetPlacePos != null && !WorldHelper.isSolidBlock(controller, this.targetPlacePos)) {
      Optional<Slot> slot = StorageHelper.getSlotWithThrowawayBlock(this.mod, true);
      if (slot.isPresent()) {
        place(this.targetPlacePos, Hand.MAIN_HAND, ((Slot)slot.get()).getInventorySlot());
        this.targetPlacePos = null;
        setDebugState(null);
      } 
      return null;
    } 
    Optional<Entity> sentity = this.mod.getEntityTracker().getClosestEntity(e -> 
        (e instanceof SkeletonEntity && EntityHelper.isAngryAtPlayer(this.mod, e) && ((SkeletonEntity)e).getItemUseTime() > 8), new Class[] { SkeletonEntity.class });
    if (sentity.isPresent()) {
      Vec3d playerPos = this.mod.getPlayer().getPos();
      Vec3d targetPos = ((Entity)sentity.get()).getPos();
      Vec3d direction = playerPos.subtract(targetPos).normalize();
      double x = playerPos.x - 2.0D * direction.x;
      double y = playerPos.y + direction.y;
      double z = playerPos.z - 2.0D * direction.z;
      this.targetPlacePos = new BlockPos((int)x, (int)y + 1, (int)z);
      setDebugState("Placing at " + this.targetPlacePos.toString());
      this.waitForBlockPlacement.reset();
    } 
    return null;
  }
  
  protected void onStop(Task interruptTask) {}
  
  public boolean isFinished() {
    assert controller.getWorld() != null;
    Optional<Entity> entity = this.mod.getEntityTracker().getClosestEntity(e -> 
        (e instanceof SkeletonEntity && EntityHelper.isAngryAtPlayer(this.mod, e) && ((SkeletonEntity)e).getItemUseTime() > 3), new Class[] { SkeletonEntity.class });
    return ((this.targetPlacePos != null && WorldHelper.isSolidBlock(mod, this.targetPlacePos)) || entity.isEmpty());
  }
  
  protected boolean isEqual(Task other) {
    return true;
  }
  
  protected String toDebugString() {
    return "Placing blocks to block projectiles";
  }
  
  public Direction getPlaceSide(BlockPos blockPos) {
    for (Direction side : Direction.values()) {
      BlockPos neighbor = blockPos.offset(side);
      BlockState state = this.mod.getWorld().getBlockState(neighbor);
      if (!state.isAir() && !isClickable(state.getBlock()))
        if (state.getFluidState().isEmpty())
          return side;  
    } 
    return null;
  }
  
  public boolean place(BlockPos blockPos, Hand hand, int slot) {
    if (slot < 0 || slot > 8)
      return false; 
    if (!canPlace(blockPos))
      return false; 
    Vec3d hitPos = Vec3d.ofCenter((Vec3i)blockPos);
    Direction side = getPlaceSide(blockPos);
    if (side == null) {
      place(blockPos.down(), hand, slot);
      return false;
    } 
    BlockPos neighbour = blockPos.offset(side);
    hitPos = hitPos.add(side.getOffsetX() * 0.5D, side.getOffsetY() * 0.5D, side.getOffsetZ() * 0.5D);
    BlockHitResult bhr = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);
    this.mod.getPlayer().setYaw((float)getYaw(hitPos));
    this.mod.getPlayer().setPitch((float)getPitch(hitPos));
    swap(slot);
    interact(bhr, hand);
    return true;
  }
  
  public static boolean isClickable(Block block) {
    return (block instanceof net.minecraft.block.CraftingTableBlock || block instanceof net.minecraft.block.AnvilBlock || block instanceof net.minecraft.block.AbstractButtonBlock || block instanceof net.minecraft.block.AbstractPressurePlateBlock || block instanceof net.minecraft.block.BlockWithEntity || block instanceof net.minecraft.block.BedBlock || block instanceof net.minecraft.block.FenceGateBlock || block instanceof net.minecraft.block.DoorBlock || block instanceof net.minecraft.block.NoteBlock || block instanceof net.minecraft.block.TrapdoorBlock);
  }
  
  public void interact(BlockHitResult blockHitResult, Hand hand) {
    boolean wasSneaking = (this.mod.getPlayer()).isSneaking();
    (this.mod.getPlayer()).setSneaking(false);
    ActionResult result = this.mod.getBaritone().getEntityContext().playerController().processRightClickBlock(this.mod.getPlayer(), this.mod.getWorld(), hand, blockHitResult);
    if (result.shouldSwingHand())
      this.mod.getPlayer().swingHand(hand); 
    (this.mod.getPlayer()).setSneaking(wasSneaking);
  }
  
  public boolean canPlace(BlockPos blockPos, boolean checkEntities) {
    if (blockPos == null)
      return false; 
    if (!World.isValid(blockPos) || !controller.getWorld().isInBuildLimit(blockPos))
      return false; 
    if (!this.mod.getWorld().getBlockState(blockPos).materialReplaceable())
      return false; 
    return (!checkEntities || this.mod.getWorld().canPlace(Blocks.OBSIDIAN.getDefaultState(), blockPos, ShapeContext.absent()));
  }
  
  public boolean canPlace(BlockPos blockPos) {
    return canPlace(blockPos, true);
  }
  
  public boolean swap(int slot) {
    if (slot == (mod.getBaritone().getEntityContext().inventory()).selectedSlot)
      return true; 
    if (slot < 0 || slot > 8)
      return false; 
    (mod.getBaritone().getEntityContext().inventory()).selectedSlot = slot;
    return true;
  }
  
  public double getYaw(Vec3d pos) {
    return (this.mod.getPlayer().getYaw() + MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(pos.getZ() - this.mod.getPlayer().getZ(), pos.getX() - this.mod.getPlayer().getX())) - 90.0F - this.mod.getPlayer().getYaw()));
  }
  
  public double getPitch(Vec3d pos) {
    double diffX = pos.getX() - this.mod.getPlayer().getX();
    double diffY = pos.getY() - this.mod.getPlayer().getY() + this.mod.getPlayer().getEyeHeight(this.mod.getPlayer().getPose());
    double diffZ = pos.getZ() - this.mod.getPlayer().getZ();
    double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
    return (this.mod.getPlayer().getPitch() + MathHelper.wrapDegrees((float)-Math.toDegrees(Math.atan2(diffY, diffXZ)) - this.mod.getPlayer().getPitch()));
  }
}
