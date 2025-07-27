package adris.altoclef.util.helpers;

import adris.altoclef.Debug;
import adris.altoclef.util.baritone.CachedProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;

public class ProjectileHelper {
  public static final double ARROW_GRAVITY_ACCEL = 0.05000000074505806D;
  
  public static final double THROWN_ENTITY_GRAVITY_ACCEL = 0.03D;
  
  public static boolean hasGravity(ProjectileEntity entity) {
    if (entity instanceof net.minecraft.entity.projectile.ExplosiveProjectileEntity)
      return false; 
    return !entity.hasNoGravity();
  }
  
  private static Vec3d getClosestPointOnFlatLine(double shootX, double shootZ, double velX, double velZ, double playerX, double playerZ) {
    double deltaX = playerX - shootX;
    double deltaZ = playerZ - shootZ;
    double t = (velX * deltaX + velZ * deltaZ) / (velX * velX + velZ * velZ);
    double hitX = shootX + velX * t;
    double hitZ = shootZ + velZ * t;
    return new Vec3d(hitX, 0.0D, hitZ);
  }
  
  public static double getFlatDistanceSqr(double shootX, double shootZ, double velX, double velZ, double playerX, double playerZ) {
    return getClosestPointOnFlatLine(shootX, shootZ, velX, velZ, playerX, playerZ).squaredDistanceTo(playerX, 0.0D, playerZ);
  }
  
  private static double getArrowHitHeight(double gravity, double horizontalVel, double verticalVel, double initialHeight, double distanceTraveled) {
    double time = distanceTraveled / horizontalVel;
    return initialHeight - verticalVel * time - 0.5D * gravity * time * time;
  }
  
  public static Vec3d calculateArrowClosestApproach(Vec3d shootOrigin, Vec3d shootVelocity, double yGravity, Vec3d playerOrigin) {
    Vec3d flatEncounter = getClosestPointOnFlatLine(shootOrigin.x, shootOrigin.z, shootVelocity.x, shootVelocity.z, playerOrigin.x, playerOrigin.z);
    double encounterDistanceTraveled = flatEncounter.subtract(shootOrigin.x, flatEncounter.y, shootOrigin.z).length();
    double horizontalVel = Math.sqrt(shootVelocity.x * shootVelocity.x + shootVelocity.z * shootVelocity.z);
    double verticalVel = shootVelocity.y;
    double initialHeight = shootOrigin.y;
    double hitHeight = getArrowHitHeight(yGravity, horizontalVel, verticalVel, initialHeight, encounterDistanceTraveled);
    return new Vec3d(flatEncounter.x, hitHeight, flatEncounter.z);
  }
  
  public static Vec3d calculateArrowClosestApproach(CachedProjectile projectile, Vec3d pos) {
    return calculateArrowClosestApproach(projectile.position, projectile.velocity, projectile.gravity, pos);
  }
  
  public static double[] calculateAnglesForSimpleProjectileMotion(double launchHeight, double launchTargetDistance, double launchVelocity, double gravity) {
    double v = launchVelocity;
    double g = gravity;
    double x = launchTargetDistance;
    double y = -1.0D * launchHeight;
    double root = v * v * v * v - g * (g * x * x + 2.0D * y * v * v);
    if (root < 0.0D) {
      Debug.logMessage("Not enough velocity, returning 45 degrees.");
      return new double[] { 45.0D, 45.0D };
    } 
    double tanTheta0 = (v * v + Math.sqrt(root)) / g * x;
    double tanTheta1 = (v * v - Math.sqrt(root)) / g * x;
    double[] angles = { Math.toDegrees(Math.atan(tanTheta0)), Math.toDegrees(Math.atan(tanTheta1)) };
    return new double[] { Math.min(angles[0], angles[1]), Math.max(angles[0], angles[1]) };
  }
  
  public static Vec3d getThrowOrigin(Entity entity) {
    return entity.getPos().subtract(0.0D, 0.1D, 0.0D);
  }
  
  @Deprecated
  private static double getNearestTimeOfShotProjectile(Vec3d shootOrigin, Vec3d shootVelocity, double yGravity, Vec3d playerOrigin) {
    Vec3d D = playerOrigin.subtract(shootOrigin);
    Vec3d V = shootVelocity;
    double g = yGravity;
    double a = g * g / 2.0D;
    double b = -(3.0D * g * V.y) / 2.0D;
    double c = V.lengthSquared() + g * V.y;
    double d = -1.0D * V.dotProduct(D);
    double p = -b / 3.0D * a;
    double q = p * p * p + (b * c - 3.0D * a * d) / 6.0D * a * a;
    double r = c / 3.0D * a;
    double rootInner = q * q + Math.pow(r - p * p, 3.0D);
    if (rootInner < 0.0D)
      return -1.0D; 
    rootInner = Math.sqrt(rootInner);
    double outerPreCubeLeft = q + rootInner;
    double outerPreCubeRight = q - rootInner;
    return Math.pow(outerPreCubeLeft, 0.3333333333333333D) + Math.pow(outerPreCubeRight, 0.3333333333333333D) + p;
  }
}
