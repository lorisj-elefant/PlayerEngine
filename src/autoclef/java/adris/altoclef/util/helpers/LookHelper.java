package adris.altoclef.util.helpers;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import baritone.api.utils.IEntityContext;
import baritone.api.utils.RayTraceUtils;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public interface LookHelper {
  static Optional<Rotation> getReach(AltoClefController controller, BlockPos target, Direction side) {
    Optional<Rotation> reachableRotation;
    IEntityContext context = controller.getBaritone().getEntityContext();
    if (side == null) {
      reachableRotation = RotationUtils.reachable(context.entity(), target, context.playerController().getBlockReachDistance());
    } else {
      Vec3i sideVector = side.getVector();
      Vec3d centerOffset = new Vec3d(0.5D + sideVector.getX() * 0.5D, 0.5D + sideVector.getY() * 0.5D, 0.5D + sideVector.getZ() * 0.5D);
      Vec3d sidePoint = centerOffset.add(target.getX(), target.getY(), target.getZ());
      reachableRotation = RotationUtils.reachableOffset((Entity)context.entity(), target, sidePoint, context
          .playerController().getBlockReachDistance(), false);
      if (reachableRotation.isPresent()) {
        Vec3d cameraPos = context.entity().getCameraPosVec(1.0F);
        Vec3d vecToPlayerPos = cameraPos.subtract(sidePoint);
        double dotProduct = vecToPlayerPos.normalize().dotProduct(new Vec3d(sideVector
              .getX(), sideVector.getY(), sideVector.getZ()));
        if (dotProduct < 0.0D)
          return Optional.empty(); 
      } 
    } 
    return reachableRotation;
  }
  
  static Optional<Rotation> getReach(AltoClefController controller, BlockPos target) {
    Debug.logInternal("Target: " + String.valueOf(target));
    return getReach(controller, target, null);
  }
  
  static EntityHitResult raycast(Entity from, Entity to, double reachDistance) {
    Vec3d start = getCameraPos(from);
    Vec3d end = getCameraPos(to);
    Vec3d direction = end.subtract(start).normalize().multiply(reachDistance);
    Box box = to.getBoundingBox();
    return ProjectileUtil.raycast(from, start, start.add(direction), box, entity -> entity.equals(to), 0.0D);
  }
  
  static boolean seesPlayer(Entity entity, Entity player, double maxRange, Vec3d entityOffset, Vec3d playerOffset) {
    return (seesPlayerOffset(entity, player, maxRange, entityOffset, playerOffset) || 
      seesPlayerOffset(entity, player, maxRange, entityOffset, playerOffset.add(0.0D, -1.0D, 0.0D)));
  }
  
  static boolean seesPlayer(Entity entity, Entity player, double maxRange) {
    return seesPlayer(entity, player, maxRange, new Vec3d(0.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 0.0D));
  }
  
  static boolean cleanLineOfSight(Entity entity, Vec3d start, Vec3d end, double maxRange) {
    BlockHitResult blockHitResult = raycast(entity, start, end, maxRange);
    return (blockHitResult.getType() == HitResult.Type.MISS);
  }
  
  static boolean cleanLineOfSight(Entity entity, Vec3d end, double maxRange) {
    Vec3d start = getCameraPos(entity);
    return cleanLineOfSight(entity, start, end, maxRange);
  }
  
  static boolean cleanLineOfSight(AltoClefController controller, Vec3d end, double maxRange) {
    LivingEntity clientPlayerEntity = controller.getPlayer();
    return cleanLineOfSight(clientPlayerEntity, end, maxRange);
  }
  
  static boolean cleanLineOfSight(Entity entity, BlockPos block, double maxRange) {
    Vec3d targetPosition = WorldHelper.toVec3d(block);
    BlockHitResult hitResult = raycast(entity, getCameraPos(entity), targetPosition, maxRange);
    if (hitResult == null)
      return true; 
    switch (hitResult.getType().ordinal()) {
      default:
        throw new IncompatibleClassChangeError();
      case 1:
      
      case 2:
      
      case 3:
        break;
    } 
    return false;
  }
  
  static Vec3d toVec3d(Rotation rotation) throws NullPointerException {
    Objects.requireNonNull(rotation, "Rotation cannot be null");
    return calcLookDirectionFromRotation(rotation);
  }
  
  static Vec3d calcLookDirectionFromRotation(Rotation rotation) {
    float flatZ = MathHelper.cos(-rotation.getYaw() * 0.017453292F - 3.1415927F);
    float flatX = MathHelper.sin(-rotation.getYaw() * 0.017453292F - 3.1415927F);
    float pitchBase = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
    float pitchHeight = MathHelper.sin(-rotation.getPitch() * 0.017453292F);
    return new Vec3d((flatX * pitchBase), pitchHeight, (flatZ * pitchBase));
  }
  
  static BlockHitResult raycast(Entity entity, Vec3d start, Vec3d end, double maxRange) {
    Vec3d direction = end.subtract(start);
    if (direction.lengthSquared() > maxRange * maxRange) {
      direction = direction.normalize().multiply(maxRange);
      end = start.add(direction);
    } 
    World world = entity.getWorld();
    RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
    return world.raycast(context);
  }
  
  static BlockHitResult raycast(Entity entity, Vec3d end, double maxRange) {
    Vec3d start = getCameraPos(entity);
    return raycast(entity, start, end, maxRange);
  }
  
  static Rotation getLookRotation(Entity entity) {
    float pitch = entity.getPitch();
    float yaw = entity.getYaw();
    return new Rotation(yaw, pitch);
  }
  
  static Rotation getLookRotation(AltoClefController mod) {
    LivingEntity clientPlayerEntity = mod.getEntity();
    if (clientPlayerEntity == null)
      return new Rotation(0.0F, 0.0F); 
    return getLookRotation((Entity)clientPlayerEntity);
  }
  
  static Vec3d getCameraPos(Entity entity) {
    boolean isPlayerSneaking = (entity instanceof net.minecraft.entity.player.PlayerEntity && entity.isSneaking());
    if (isPlayerSneaking)
      return RayTraceUtils.inferSneakingEyePosition(entity); 
    return entity.getCameraPosVec(1.0F);
  }
  
  static Vec3d getCameraPos(AltoClefController mod) {
    IEntityContext playerContext = mod.getBaritone().getEntityContext();
    return playerContext.entity().getCameraPosVec(1.0F);
  }
  
  static double getLookCloseness(Entity entity, Vec3d pos) {
    Vec3d rotDirection = entity.getRotationVecClient();
    Vec3d lookStart = getCameraPos(entity);
    Vec3d deltaToPos = pos.subtract(lookStart);
    Vec3d deltaDirection = deltaToPos.normalize();
    return rotDirection.dotProduct(deltaDirection);
  }
  
  private static boolean seesPlayerOffset(Entity entity, Entity player, double maxRange, Vec3d offsetEntity, Vec3d offsetPlayer) {
    Vec3d entityCameraPos = getCameraPos(entity).add(offsetEntity);
    Vec3d playerCameraPos = getCameraPos(player).add(offsetPlayer);
    return cleanLineOfSight(entity, entityCameraPos, playerCameraPos, maxRange);
  }
  
  static void randomOrientation(AltoClefController mod) {
    float randomRotationX = (float)(Math.random() * 360.0D);
    float randomRotationY = -90.0F + (float)(Math.random() * 180.0D);
    Rotation r = new Rotation(randomRotationX, randomRotationY);
    lookAt(mod, r);
  }
  
  static boolean isLookingAt(AltoClefController mod, Rotation rotation) {
    return rotation.isReallyCloseTo(getLookRotation(mod));
  }
  
  static boolean isLookingAt(AltoClefController mod, BlockPos pos) {
    return mod.getBaritone().getEntityContext().isLookingAt(pos);
  }
  
  static boolean isLookingAt(Entity entity, Vec3d toLookAt, double angleThreshold) {
    Vec3d head = entity.getPos().add(new Vec3d(0.0D, entity.getStandingEyeHeight(), 0.0D));
    Rotation rotation = new Rotation(entity.getYaw(), entity.getPitch());
    Vec3d look = calcLookDirectionFromRotation(rotation);
    Vec3d targetLook = toLookAt.subtract(head).normalize();
    double dot = look.dotProduct(targetLook);
    double angle = Math.toDegrees(Math.acos(dot));
    return (Math.abs(angle) < angleThreshold);
  }
  
  static void lookAt(AltoClefController mod, Rotation rotation, boolean withBaritone) {
    if (withBaritone)
      mod.getBaritone().getLookBehavior().updateTarget(rotation, true); 
    mod.getPlayer().setYaw(rotation.getYaw());
    mod.getPlayer().setPitch(rotation.getPitch());
  }
  
  static void lookAt(AltoClefController mod, Rotation rotation) {
    mod.getBaritone().getLookBehavior().updateTarget(rotation, true);
    LivingEntity player = mod.getBaritone().getEntityContext().entity();
    player.setYaw(rotation.getYaw());
    player.setPitch(rotation.getPitch());
  }
  
  static void lookAt(AltoClefController mod, Vec3d toLook, boolean withBaritone) {
    if (mod == null || toLook == null)
      throw new IllegalArgumentException("mod and toLook cannot be null"); 
    Rotation targetRotation = getLookRotation(mod, toLook);
    lookAt(mod, targetRotation, withBaritone);
  }
  
  static void lookAt(AltoClefController mod, Vec3d toLook) {
    if (mod == null || toLook == null)
      throw new IllegalArgumentException("mod and toLook cannot be null"); 
    Rotation targetRotation = getLookRotation(mod, toLook);
    lookAt(mod, targetRotation, true);
  }
  
  static void lookAt(AltoClefController mod, BlockPos toLook, Direction side, boolean withBaritone) {
    double centerX = toLook.getX() + 0.5D;
    double centerY = toLook.getY() + 0.5D;
    double centerZ = toLook.getZ() + 0.5D;
    if (side != null) {
      double offsetX = side.getVector().getX() * 0.5D;
      double offsetY = side.getVector().getY() * 0.5D;
      double offsetZ = side.getVector().getZ() * 0.5D;
      centerX += offsetX;
      centerY += offsetY;
      centerZ += offsetZ;
    } 
    Vec3d target = new Vec3d(centerX, centerY, centerZ);
    lookAt(mod, target, withBaritone);
  }
  
  static void lookAt(AltoClefController mod, BlockPos toLook, Direction side) {
    double centerX = toLook.getX() + 0.5D;
    double centerY = toLook.getY() + 0.5D;
    double centerZ = toLook.getZ() + 0.5D;
    if (side != null) {
      double offsetX = side.getVector().getX() * 0.5D;
      double offsetY = side.getVector().getY() * 0.5D;
      double offsetZ = side.getVector().getZ() * 0.5D;
      centerX += offsetX;
      centerY += offsetY;
      centerZ += offsetZ;
    } 
    Vec3d target = new Vec3d(centerX, centerY, centerZ);
    lookAt(mod, target, true);
  }
  
  static void lookAt(AltoClefController mod, BlockPos toLook, boolean withBaritone) {
    lookAt(mod, toLook, null, withBaritone);
  }
  
  static void lookAt(AltoClefController mod, BlockPos toLook) {
    lookAt(mod, toLook, null, true);
  }
  
  static Rotation getLookRotation(AltoClefController mod, Vec3d toLook) {
    Vec3d playerHead = mod.getBaritone().getEntityContext().headPos();
    Rotation playerRotations = mod.getBaritone().getEntityContext().entityRotations();
    return RotationUtils.calcRotationFromVec3d(playerHead, toLook, playerRotations);
  }
  
  static Rotation getLookRotation(AltoClefController mod, BlockPos toLook) {
    Vec3d targetPosition = WorldHelper.toVec3d(toLook);
    return getLookRotation(mod, targetPosition);
  }
}
