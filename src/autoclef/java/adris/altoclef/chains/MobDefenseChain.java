// File: adris/altoclef/chains/MobDefenseChain.java
package adris.altoclef.chains;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.control.KillAura;
import adris.altoclef.tasks.construction.ProjectileProtectionWallTask;
import adris.altoclef.tasks.entity.AbstractKillEntityTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.tasks.movement.CustomBaritoneGoalTask;
import adris.altoclef.tasks.movement.DodgeProjectilesTask;
import adris.altoclef.tasks.movement.RunAwayFromCreepersTask;
import adris.altoclef.tasks.movement.RunAwayFromHostilesTask;
import adris.altoclef.tasks.speedrun.DragonBreathTracker;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.baritone.CachedProjectile;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.ProjectileHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.IBaritone;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import baritone.behavior.PathingBehavior;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MobDefenseChain extends SingleTaskChain {

  private static final double DANGER_KEEP_DISTANCE = 30.0;
  private static final double CREEPER_KEEP_DISTANCE = 10.0;
  private static final List<Class<? extends Entity>> IGNORED_MOBS = List.of(
          EnderDragonEntity.class, WitherEntity.class, EndermanEntity.class, BlazeEntity.class,
          WitherSkeletonEntity.class, HoglinEntity.class, ZoglinEntity.class, PiglinBruteEntity.class,
          VindicatorEntity.class, MagmaCubeEntity.class);

  private final KillAura _killAura = new KillAura();
  private final DragonBreathTracker _dragonBreathTracker = new DragonBreathTracker();

  private boolean _shielding = false;
  private CustomBaritoneGoalTask _runAwayTask;
  private float _prevHealth = 20.0f;
  private Entity _lockedOnEntity;
  private boolean _needsToSwitchTargets;

  public MobDefenseChain(TaskRunner runner) {
    super(runner);
  }

  public static double getCreeperSafety(Vec3d pos, CreeperEntity creeper) {
    double distance = creeper.squaredDistanceTo(pos);
    float fuse = creeper.getClientFuseTime(1);

    // Not fusing.
    if (fuse <= 0.001f) return distance;
    return distance * 0.2; // less is WORSE
  }

  @Override
  public float getPriority() {
    if (controller == null || !controller.getModSettings().isMobDefense() || controller.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
      return Float.NEGATIVE_INFINITY;
    }

    if (_needsToSwitchTargets && (controller.getEntity().getHealth() < _prevHealth || _killAura.attackedLastTick)) {
      _needsToSwitchTargets = false;
    }

    _prevHealth = controller.getEntity().getHealth();
    return getPriorityInner();
  }

  private float getPriorityInner() {
    if (controller.getFoodChain().isTryingToEat() || controller.getMLGBucketChain().isFalling(controller)) {
      stopShielding(controller);
      return Float.NEGATIVE_INFINITY;
    }

    // Handle fire
    if (isInsideFireAndOnFire(controller) != null) {
      // This is handled by WorldSurvivalChain, but we can add a higher priority escape here if needed.
      // For now, let's assume WorldSurvivalChain is enough.
    }

    // Extremely dangerous mob nearby
    Optional<Entity> universallyDangerous = getUniversallyDangerousMob(controller);
    if (universallyDangerous.isPresent() && controller.getEntity().getHealth() <= 10.0F) {
      setTask(new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true));
      return 95;
    }

    // Creeper defense
    CreeperEntity blowingUp = getClosestFusingCreeper(controller);
    if (blowingUp != null) {
      boolean canShield = hasShield(controller);//TODO && !controller.getEntity().getItemCooldownManager().isCoolingDown(Items.SHIELD);
      if (canShield && controller.getBaritone().getPathingBehavior().isSafeToCancel() && blowingUp.getClientFuseTime(1) > 10) { // 10 ticks = 0.5s
        LookHelper.lookAt(controller, blowingUp.getEyePos());
        startShielding(controller);
        setTask(null); // Shielding is an action, not a task
        return 90;
      } else {
        setTask(new RunAwayFromCreepersTask(CREEPER_KEEP_DISTANCE));
        return 90;
      }
    }

    // Projectile defense
    if (controller.getModSettings().isDodgeProjectiles() && isProjectileClose(controller)) {
      if (hasShield(controller) && controller.getBaritone().getPathingBehavior().isSafeToCancel()) {
        startShielding(controller);
        setTask(null);
        return 80;
      } else {
        setTask(new DodgeProjectilesTask(2.0, 10.0));
        return 80;
      }
    }

    // Stop shielding if threats are gone
    if (blowingUp == null && !isProjectileClose(controller)) {
      stopShielding(controller);
    }

    // General mob defense
    doForceField(controller);

    // Danger check
    if (isInDanger(controller) && !_dragonBreathTracker.isTouchingDragonBreath(controller.getEntity().getBlockPos())) {
      setTask(new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true));
      return 70;
    }

    // Annoying hostiles
    if (controller.getModSettings().shouldDealWithAnnoyingHostiles()) {
      List<LivingEntity> hostilesToDealWith = getAnnoyingHostiles(controller);
      if (!hostilesToDealWith.isEmpty()) {
        if (canDealWithHostiles(controller, hostilesToDealWith) || _needsToSwitchTargets) {
          if (!(mainTask instanceof KillEntitiesTask)) {
            _needsToSwitchTargets = true;
          }
          Entity toKill = hostilesToDealWith.get(0);
          _lockedOnEntity = toKill;
          setTask(new KillEntitiesTask(entity -> entity.equals(toKill)));
          return 60;
        } else {
          setTask(new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true));
          return 75;
        }
      }
    }

    // If we were running away, but are now safe, we can stop.
    if (_runAwayTask != null && !_runAwayTask.isFinished()) {
      setTask(_runAwayTask);
      return 50;
    }

    _lockedOnEntity = null;
    _needsToSwitchTargets = false;

    return Float.NEGATIVE_INFINITY;
  }

  private void startShielding(AltoClefController controller) {
    if (_shielding) return;
    _shielding = true;
    ((PathingBehavior)controller.getBaritone().getPathingBehavior()).requestPause();
    controller.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
    controller.getBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);
  }

  private void stopShielding(AltoClefController controller) {
    if (!_shielding) return;
    _shielding = false;
    controller.getBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);
  }

  public static boolean hasShield(AltoClefController controller) {
    return controller.getItemStorage().hasItem(Items.SHIELD) || controller.getItemStorage().hasItemInOffhand(controller, Items.SHIELD);
  }

  public static ToolItem getBestWeapon(AltoClefController controller) {
    Item best = AbstractKillEntityTask.bestWeapon(controller);
    return best instanceof ToolItem ? (ToolItem) best : null;
  }

  private BlockPos isInsideFireAndOnFire(AltoClefController controller) {
    if (!controller.getEntity().isOnFire()) return null;
    for (BlockPos pos : WorldHelper.getBlocksTouchingPlayer(controller.getEntity())) {
      if (controller.getWorld().getBlockState(pos).getBlock() instanceof net.minecraft.block.AbstractFireBlock) {
        return pos;
      }
    }
    return null;
  }

  private void doForceField(AltoClefController controller) {
    _killAura.tickStart();
    for (Entity entity : controller.getEntityTracker().getCloseEntities()) {
      if (controller.getBehaviour().shouldExcludeFromForcefield(entity)) continue;
      boolean shouldForce = false;
      if (entity instanceof MobEntity) {
        if (EntityHelper.isProbablyHostileToPlayer(controller, entity) && LookHelper.seesPlayer(entity, controller.getEntity(), 10.0)) {
          shouldForce = true;
        }
      } else if (entity instanceof FireballEntity) {
        shouldForce = true;
      }
      if (shouldForce) {
        _killAura.applyAura(entity);
      }
    }
    _killAura.tickEnd(controller);
  }

  private CreeperEntity getClosestFusingCreeper(AltoClefController controller) {
    return controller.getEntityTracker().getTrackedEntities(CreeperEntity.class).stream()
            .filter(creeper -> creeper.getFuseSpeed() > 0)
            .min(Comparator.comparingDouble(creeper -> creeper.squaredDistanceTo(controller.getEntity())))
            .orElse(null);
  }

  private boolean isProjectileClose(AltoClefController controller) {
    for (CachedProjectile projectile : controller.getEntityTracker().getProjectiles()) {
      if (projectile.position.squaredDistanceTo(controller.getEntity().getPos()) < 150.0) {
        if (projectile.projectileType == DragonFireballEntity.class) continue;

        Vec3d expectedHit = ProjectileHelper.calculateArrowClosestApproach(projectile, controller.getEntity().getPos());
        Vec3d delta = controller.getEntity().getPos().subtract(expectedHit);
        double horizontalSq = delta.x * delta.x + delta.z * delta.z;
        double vertical = Math.abs(delta.y);
        if (horizontalSq < 4.0 && vertical < 10.0) {
          return true;
        }
      }
    }
    for (SkeletonEntity skeleton : controller.getEntityTracker().getTrackedEntities(SkeletonEntity.class)) {
      if (skeleton.distanceTo(controller.getEntity()) <= 10.0 && skeleton.canSee(controller.getEntity()) && skeleton.isAttacking()) {
        return true;
      }
    }
    return false;
  }

  private Optional<Entity> getUniversallyDangerousMob(AltoClefController controller) {
    return controller.getEntityTracker().getClosestEntity(
            entity -> entity.distanceTo(controller.getEntity()) < 6 && EntityHelper.isAngryAtPlayer(controller, entity),
            WitherEntity.class, WitherSkeletonEntity.class, HoglinEntity.class, ZoglinEntity.class,
            PiglinBruteEntity.class, VindicatorEntity.class
    );
  }

  private boolean isInDanger(AltoClefController controller) {
    boolean witchNearby = controller.getEntityTracker().entityFound(WitchEntity.class);
    double safeDistance = 8.0;
    if (controller.getEntity().getHealth() <= 10.0F && witchNearby) {
      safeDistance = DANGER_KEEP_DISTANCE;
    }
    if (controller.getEntity().hasStatusEffect(StatusEffects.WITHER) || (controller.getEntity().hasStatusEffect(StatusEffects.POISON) && witchNearby)) {
      safeDistance = DANGER_KEEP_DISTANCE;
    }

    if (WorldHelper.isVulnerable(controller.getEntity())) {
      for (LivingEntity hostile : controller.getEntityTracker().getHostiles()) {
        if (hostile.isInRange(controller.getEntity(), safeDistance) &&
                !controller.getBehaviour().shouldExcludeFromForcefield(hostile) &&
                EntityHelper.isAngryAtPlayer(controller, hostile)) {
          return true;
        }
      }
    }
    return false;
  }

  private List<LivingEntity> getAnnoyingHostiles(AltoClefController controller) {
    List<LivingEntity> result = new ArrayList<>();
    for (LivingEntity hostile : controller.getEntityTracker().getHostiles()) {
      boolean isRangedOrPoisonous = hostile instanceof SkeletonEntity || hostile instanceof WitchEntity || hostile instanceof PillagerEntity || hostile instanceof PiglinEntity || hostile instanceof StrayEntity || hostile instanceof CaveSpiderEntity;
      int annoyingRange = 10;
      if (isRangedOrPoisonous) {
        annoyingRange = 20;
        if (!hasShield(controller)) {
          annoyingRange = 35;
        }
      }
      if (hostile.isInRange(controller.getEntity(), annoyingRange) && LookHelper.seesPlayer(hostile, controller.getEntity(), annoyingRange)) {
        boolean isIgnored = IGNORED_MOBS.stream().anyMatch(ignoredClass -> ignoredClass.isInstance(hostile));
        if (isIgnored) {
          if (controller.getEntity().getHealth() <= 10.0F) {
            result.add(hostile);
          }
        } else {
          result.add(hostile);
        }
      }
    }
    result.sort(Comparator.comparingDouble(entity -> entity.distanceTo(controller.getEntity())));
    return result;
  }

  private boolean canDealWithHostiles(AltoClefController controller, List<LivingEntity> hostiles) {
    ToolItem weapon = getBestWeapon(controller);
    int armor = controller.getEntity().getArmor();
    float damage = (weapon == null) ? 1.0f : weapon.getMaterial().getAttackDamage() + 1.0f;
    int shieldBonus = (hasShield(controller) && weapon != null) ? 3 : 0;
    // Simplified scoring system
    int powerScore = (int) (armor * 0.5 + damage * 2 + shieldBonus);
    int threatScore = hostiles.size();
    return powerScore >= threatScore;
  }

  @Override
  protected void onTaskFinish(AltoClefController controller) {
  }

  @Override
  public String getName() {
    return "Mob Defense";
  }

  public void setTargetEntity(Entity entity) {
    this.mainTask = new KillEntityTask(entity);
  }

  public void resetTargetEntity() {
    if (mainTask instanceof KillEntityTask) {
      this.mainTask = null;
    }
  }

  public void setForceFieldRange(double range) {
    _killAura.setRange(range);
  }

  public void resetForceField() {
    _killAura.setRange(Double.POSITIVE_INFINITY);
  }

  public boolean isShielding(){
    return _shielding;
  }
}