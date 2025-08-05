package adris.altoclef.chains;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.control.KillAura;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.tasks.construction.ProjectileProtectionWallTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.CustomBaritoneGoalTask;
import adris.altoclef.tasks.movement.DodgeProjectilesTask;
import adris.altoclef.tasks.movement.RunAwayFromCreepersTask;
import adris.altoclef.tasks.movement.RunAwayFromHostilesTask;
import adris.altoclef.tasks.speedrun.DragonBreathTracker;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.baritone.CachedProjectile;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.ProjectileHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import baritone.api.IBaritone;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import baritone.behavior.PathingBehavior;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.mob.warden.WardenEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;

public class MobDefenseChain extends SingleTaskChain {
    private static final double DANGER_KEEP_DISTANCE = 30.0D;

    private static final double CREEPER_KEEP_DISTANCE = 10.0D;

    private static final double ARROW_KEEP_DISTANCE_HORIZONTAL = 2.0D;

    private static final double ARROW_KEEP_DISTANCE_VERTICAL = 10.0D;

    private static final double SAFE_KEEP_DISTANCE = 8.0D;

    private static final List<Class<? extends Entity>> ignoredMobs = List.of(WardenEntity.class, WitherEntity.class, EndermanEntity.class, BlazeEntity.class, WitherSkeletonEntity.class, HoglinEntity.class, ZoglinEntity.class, PiglinBruteEntity.class, VindicatorEntity.class, MagmaCubeEntity.class);

    private static boolean shielding = false;

    private final DragonBreathTracker dragonBreathTracker = new DragonBreathTracker();

    private final KillAura killAura = new KillAura();

    private Entity targetEntity;

    private boolean doingFunkyStuff = false;

    private boolean wasPuttingOutFire = false;

    private CustomBaritoneGoalTask runAwayTask;

    private float prevHealth = 20.0F;

    private boolean needsChangeOnAttack = false;

    private Entity lockedOnEntity = null;

    private float cachedLastPriority;

    public MobDefenseChain(TaskRunner runner) {
        super(runner);
    }

    public static double getCreeperSafety(Vec3d pos, CreeperEntity creeper) {
        double distance = creeper.squaredDistanceTo(pos);
        float fuse = creeper.getClientFuseTime(1.0F);
        if (fuse <= 0.001F)
            return distance;
        return distance * 0.2D;
    }

    private static void startShielding(AltoClefController mod) {
        shielding = true;
        ((PathingBehavior) mod.getBaritone().getPathingBehavior()).requestPause();
        mod.getExtraBaritoneSettings().setInteractionPaused(true);
        if (!mod.getPlayer().isBlocking()) {
            ItemStack handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot(mod.getInventory()));
            if (ItemVer.isFood(handItem)) {
                List<ItemStack> spaceSlots = mod.getItemStorage().getItemStacksPlayerInventory(false);
                for (ItemStack spaceSlot : spaceSlots) {
                    if (spaceSlot.isEmpty()) {
                        mod.getSlotHandler().clickSlot(PlayerSlot.getEquipSlot(mod.getInventory()), 0, SlotActionType.QUICK_MOVE);
                        return;
                    }
                }
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                garbage.ifPresent(slot -> mod.getSlotHandler().forceEquipItem(StorageHelper.getItemStackInSlot(slot).getItem()));
            }
        }
        mod.getInputControls().hold(Input.SNEAK);
        mod.getInputControls().hold(Input.CLICK_RIGHT);
    }

    private static int getDangerousnessScore(List<LivingEntity> toDealWithList) {
        int numberOfProblematicEntities = toDealWithList.size();
        for (LivingEntity toDealWith : toDealWithList) {
            if (toDealWith instanceof EndermanEntity || toDealWith instanceof net.minecraft.entity.mob.SlimeEntity || toDealWith instanceof BlazeEntity) {
                numberOfProblematicEntities++;
                continue;
            }
            if (toDealWith instanceof net.minecraft.entity.mob.DrownedEntity && toDealWith.getItemsEquipped() == Items.TRIDENT)
                numberOfProblematicEntities += 5;
        }
        return numberOfProblematicEntities;
    }

    public float getPriority() {
        this.cachedLastPriority = getPriorityInner();
        if (getCurrentTask() == null)
            this.cachedLastPriority = 0.0F;
        this.prevHealth = controller.getPlayer().getHealth();
        return this.cachedLastPriority;
    }

    private void stopShielding(AltoClefController mod) {
        if (shielding) {
            ItemStack cursor = StorageHelper.getItemStackInCursorSlot(controller);
            if (ItemVer.isFood(cursor)) {
                Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false).or(() -> StorageHelper.getGarbageSlot(mod));
                if (toMoveTo.isPresent()) {
                    Slot garbageSlot = toMoveTo.get();
                    mod.getSlotHandler().clickSlot(garbageSlot, 0, SlotActionType.PICKUP);
                }
            }
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.CLICK_RIGHT);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
            shielding = false;
        }
    }

    public boolean isShielding() {
        return (shielding || this.killAura.isShielding());
    }

    private boolean escapeDragonBreath(AltoClefController mod) {
        this.dragonBreathTracker.updateBreath(mod);
        for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer(mod.getPlayer())) {
            if (this.dragonBreathTracker.isTouchingDragonBreath(playerIn))
                return true;
        }
        return false;
    }

    private float getPriorityInner() {
        if (!AltoClefController.inGame())
            return Float.NEGATIVE_INFINITY;
        AltoClefController mod = controller;
        if (!mod.getModSettings().isMobDefense())
            return Float.NEGATIVE_INFINITY;
        if (mod.getWorld().getDifficulty() == Difficulty.PEACEFUL)
            return Float.NEGATIVE_INFINITY;
        if (this.needsChangeOnAttack && (mod.getPlayer().getHealth() < this.prevHealth || this.killAura.attackedLastTick))
            this.needsChangeOnAttack = false;
        BlockPos fireBlock = isInsideFireAndOnFire(mod);
        if (fireBlock != null) {
            putOutFire(mod, fireBlock);
            this.wasPuttingOutFire = true;
        } else {
            mod.getBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, false);
            this.wasPuttingOutFire = false;
        }
        Optional<Entity> universallyDangerous = getUniversallyDangerousMob(mod);
        if (universallyDangerous.isPresent() && mod.getPlayer().getHealth() <= 10.0F) {
            this.runAwayTask = (CustomBaritoneGoalTask) new RunAwayFromHostilesTask(30.0D, true);
            this.runAwayTask.controller = this.controller;
            setTask((Task) this.runAwayTask);
            return 70.0F;
        }
        this.doingFunkyStuff = false;
        Slot offhandSlot = PlayerSlot.getOffhandSlot(mod.getInventory());
        Item offhandItem = StorageHelper.getItemStackInSlot(offhandSlot).getItem();
        CreeperEntity blowingUp = getClosestFusingCreeper(mod);
        if (blowingUp != null && blowingUp.distanceTo(mod.getEntity())<=16)
            if ((!mod.getFoodChain().needsToEat() || mod.getPlayer().getHealth() < 9.0F) &&
                    hasShield(mod) &&
                    !mod.getEntityTracker().entityFound(new Class[]{PotionEntity.class}) && //!mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandItem) &&
                    mod.getBaritone().getPathingBehavior().isSafeToCancel() && blowingUp
                    .getClientFuseTime(blowingUp.getFuseSpeed()) > 0.5D) {
                LookHelper.lookAt(mod, blowingUp.getEyePos());
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(offhandSlot);
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    startShielding(mod);
                }
            } else {
                this.doingFunkyStuff = true;
                this.runAwayTask = (CustomBaritoneGoalTask) new RunAwayFromCreepersTask(10.0D);
                this.runAwayTask.controller = this.controller;
                setTask((Task) this.runAwayTask);
                return 50.0F + blowingUp.getClientFuseTime(1.0F) * 50.0F;
            }
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            if (mod.getModSettings().isDodgeProjectiles() &&
                    hasShield(mod) &&
                    //!mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandItem) &&
                    mod.getBaritone().getPathingBehavior().isSafeToCancel() &&
                    !mod.getEntityTracker().entityFound(new Class[]{PotionEntity.class}) && isProjectileClose(mod)) {
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(new Slot(mod.getInventory().offHand, 0));
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    startShielding(mod);
                }
                return 60.0F;
            }
            if (blowingUp == null && !isProjectileClose(mod))
                stopShielding(mod);
        }
        if (mod.getFoodChain().needsToEat() || mod.getMLGBucketChain().isFalling(mod) ||
                !mod.getMLGBucketChain().doneMLG() || mod.getMLGBucketChain().isChorusFruiting()) {
            this.killAura.stopShielding(mod);
            stopShielding(mod);
            return Float.NEGATIVE_INFINITY;
        }
        doForceField(mod);
        if (mod.getPlayer().getHealth() <= 10.0F && !hasShield(mod)) {
            if (StorageHelper.getNumberOfThrowawayBlocks(mod) > 0 && !mod.getFoodChain().needsToEat() && mod
                    .getModSettings().isDodgeProjectiles() && isProjectileClose(mod)) {
                this.doingFunkyStuff = true;
                setTask((Task) new ProjectileProtectionWallTask(mod));
                return 65.0F;
            }
            if (isProjectileClose(mod)) {
                this.runAwayTask = (CustomBaritoneGoalTask) new DodgeProjectilesTask(2.0D, 10.0D);
                this.runAwayTask.controller = this.controller;
                setTask((Task) this.runAwayTask);
                return 65.0F;
            }
        }
        if (isInDanger(mod) && !escapeDragonBreath(mod) && !mod.getFoodChain().isShouldStop() && (
                this.targetEntity == null || WorldHelper.isSurroundedByHostiles(mod))) {
            this.runAwayTask = (CustomBaritoneGoalTask) new RunAwayFromHostilesTask(30.0D, true);
            this.runAwayTask.controller = this.controller;
            setTask((Task) this.runAwayTask);
            return 70.0F;
        }
        if (mod.getModSettings().shouldDealWithAnnoyingHostiles()) {
            List<LivingEntity> hostiles = mod.getEntityTracker().getHostiles();
            List<LivingEntity> toDealWithList = new ArrayList<>();
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                for (LivingEntity hostile : hostiles) {
                    if (hostile == mod.getEntity()) continue;
                    boolean isRangedOrPoisonous = (hostile instanceof SkeletonEntity || hostile instanceof WitchEntity || hostile instanceof net.minecraft.entity.mob.PillagerEntity || hostile instanceof net.minecraft.entity.mob.PiglinEntity || hostile instanceof net.minecraft.entity.mob.StrayEntity || hostile instanceof net.minecraft.entity.mob.CaveSpiderEntity);
                    int annoyingRange = 10;
                    if (isRangedOrPoisonous) {
                        annoyingRange = 20;
                        if (!hasShield(mod))
                            annoyingRange = 35;
                    }
                    if (hostile.isInRange((Entity) mod.getPlayer(), annoyingRange) && LookHelper.seesPlayer((Entity) hostile, (Entity) mod.getPlayer(), annoyingRange)) {
                        boolean isIgnored = false;
                        for (Class<? extends Entity> ignored : ignoredMobs) {
                            if (ignored.isInstance(hostile)) {
                                isIgnored = true;
                                break;
                            }
                        }
                        if (isIgnored) {
                            if (mod.getPlayer().getHealth() <= 10.0F)
                                toDealWithList.add(hostile);
                            continue;
                        }
                        toDealWithList.add(hostile);
                    }
                }
            }
            toDealWithList.sort(Comparator.comparingDouble(entity -> mod.getPlayer().distanceTo((Entity) entity)));
            if (!toDealWithList.isEmpty()) {
                ToolItem bestWeapon = getBestWeapon(mod);
                int armor = mod.getPlayer().getArmor();
                float damage = (bestWeapon == null) ? 0.0F : (bestWeapon.getMaterial().getAttackDamage() + 1.0F);
                int shield = (hasShield(mod) && bestWeapon != null) ? 3 : 0;
                int canDealWith = (int) Math.ceil(armor * 3.6D / 20.0D + damage * 0.8D + shield);
                if (canDealWith >= getDangerousnessScore(toDealWithList) || this.needsChangeOnAttack) {
                    if (!(this.mainTask instanceof KillEntitiesTask))
                        this.needsChangeOnAttack = true;
                    this.runAwayTask = null;
                    Entity toKill = (Entity) toDealWithList.get(0);
                    this.lockedOnEntity = toKill;
                    setTask((Task) new KillEntitiesTask(new Class[]{toKill.getClass()}));
                    return 65.0F;
                }
                this.runAwayTask = (CustomBaritoneGoalTask) new RunAwayFromHostilesTask(30.0D, true);
                this.runAwayTask.controller = this.controller;
                setTask((Task) this.runAwayTask);
                return 80.0F;
            }
        }
        if (this.runAwayTask != null && !this.runAwayTask.isFinished()) {
            setTask((Task) this.runAwayTask);
            return this.cachedLastPriority;
        }
        this.runAwayTask = null;
        if (this.needsChangeOnAttack && this.lockedOnEntity != null && this.lockedOnEntity.isAlive()) {
            setTask((Task) new KillEntitiesTask(new Class[]{this.lockedOnEntity.getClass()}));
            return 65.0F;
        }
        this.needsChangeOnAttack = false;
        this.lockedOnEntity = null;
        return 0.0F;
    }

    private static boolean hasShield(AltoClefController mod) {
        return (mod.getItemStorage().hasItem(new Item[]{Items.SHIELD}) || mod.getItemStorage().hasItemInOffhand(mod, Items.SHIELD));
    }

    public static ToolItem getBestWeapon(AltoClefController mod) {
        Item[] WEAPONS = {
                Items.NETHERITE_SWORD, Items.NETHERITE_AXE, Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.IRON_SWORD, Items.IRON_AXE, Items.GOLDEN_SWORD, Items.GOLDEN_AXE, Items.STONE_SWORD, Items.STONE_AXE,
                Items.WOODEN_SWORD, Items.WOODEN_AXE};
        ToolItem bestSword = null;
        for (Item item : WEAPONS) {
            if (mod.getItemStorage().hasItem(new Item[]{item})) {
                bestSword = (ToolItem) item;
                break;
            }
        }
        return bestSword;
    }

    private BlockPos isInsideFireAndOnFire(AltoClefController mod) {
        boolean onFire = mod.getPlayer().isOnFire();
        if (!onFire)
            return null;
        BlockPos p = mod.getPlayer().getBlockPos();
        BlockPos[] toCheck = {p, p.add(1, 0, 0), p.add(1, 0, -1), p.add(0, 0, -1), p.add(-1, 0, -1), p.add(-1, 0, 0), p.add(-1, 0, 1), p.add(0, 0, 1), p.add(1, 0, 1)};
        for (BlockPos check : toCheck) {
            Block b = mod.getWorld().getBlockState(check).getBlock();
            if (b instanceof net.minecraft.block.AbstractFireBlock)
                return check;
        }
        return null;
    }

    private void putOutFire(AltoClefController mod, BlockPos pos) {
        Optional<Rotation> reach = LookHelper.getReach(mod, pos);
        if (reach.isPresent()) {
            IBaritone b = mod.getBaritone();
            if (LookHelper.isLookingAt(mod, pos)) {
                ((PathingBehavior) b.getPathingBehavior()).requestPause();
                b.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
                return;
            }
            LookHelper.lookAt(controller, reach.get());
        }
    }

    private void doForceField(AltoClefController mod) {
        this.killAura.tickStart();
        List<Entity> entities = mod.getEntityTracker().getCloseEntities();
        try {
            for (Entity entity : entities) {
                if (entity == mod.getPlayer()) continue;
                boolean shouldForce = false;
                if (mod.getBehaviour().shouldExcludeFromForcefield(entity))
                    continue;
                if (entity instanceof net.minecraft.entity.mob.MobEntity) {
                    if (EntityHelper.isProbablyHostileToPlayer(mod, entity) &&
                            LookHelper.seesPlayer(entity, (Entity) mod.getPlayer(), 10.0D))
                        shouldForce = true;
                } else if (entity instanceof FireballEntity) {
                    shouldForce = true;
                }
                if (shouldForce)
                    this.killAura.applyAura(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.killAura.tickEnd(mod);
    }

    private CreeperEntity getClosestFusingCreeper(AltoClefController mod) {
        double worstSafety = Double.POSITIVE_INFINITY;
        CreeperEntity target = null;
        try {
            List<CreeperEntity> creepers = mod.getEntityTracker().getTrackedEntities(CreeperEntity.class);
            for (CreeperEntity creeper : creepers) {
                if (creeper == null ||
                        creeper.getClientFuseTime(1.0F) < 0.04D)
                    continue;
                double safety = getCreeperSafety(mod.getPlayer().getPos(), creeper);
                if (safety < worstSafety)
                    target = creeper;
            }
        } catch (ConcurrentModificationException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            Debug.logWarning("Weird Exception caught and ignored while scanning for creepers: " + e.getMessage());
            return target;
        }
        return target;
    }

    private boolean isProjectileClose(AltoClefController mod) {
        List<CachedProjectile> projectiles = mod.getEntityTracker().getProjectiles();
        try {
            for (CachedProjectile projectile : projectiles) {
                if (projectile.position.squaredDistanceTo(mod.getPlayer().getPos()) < 150.0D) {
                    boolean isGhastBall = (projectile.projectileType == FireballEntity.class);
                    if (isGhastBall) {
                        Optional<Entity> ghastBall = mod.getEntityTracker().getClosestEntity(new Class[]{FireballEntity.class});
                        Optional<Entity> ghast = mod.getEntityTracker().getClosestEntity(new Class[]{GhastEntity.class});
                        if (ghastBall.isPresent() && ghast.isPresent() && this.runAwayTask == null && mod
                                .getBaritone().getPathingBehavior().isSafeToCancel()) {
                            ((PathingBehavior) mod.getBaritone().getPathingBehavior()).requestPause();
                            LookHelper.lookAt(mod, ((Entity) ghast.get()).getEyePos());
                        }
                        return false;
                    }
                    if (projectile.projectileType == DragonFireballEntity.class)
                        continue;
                    if (projectile.projectileType == ArrowEntity.class || projectile.projectileType == SpectralArrowEntity.class || projectile.projectileType == SmallFireballEntity.class) {
                        LivingEntity clientPlayerEntity = mod.getPlayer();
                        if (clientPlayerEntity.squaredDistanceTo(projectile.position) < clientPlayerEntity.squaredDistanceTo(projectile.position.add(projectile.velocity)))
                            continue;
                    }
                    Vec3d expectedHit = ProjectileHelper.calculateArrowClosestApproach(projectile, mod.getPlayer().getPos());
                    Vec3d delta = mod.getPlayer().getPos().subtract(expectedHit);
                    double horizontalDistanceSq = delta.x * delta.x + delta.z * delta.z;
                    double verticalDistance = Math.abs(delta.y);
                    if (horizontalDistanceSq < 4.0D && verticalDistance < 10.0D) {
                        if (mod.getBaritone().getPathingBehavior().isSafeToCancel() &&
                                hasShield(mod)) {
                            ((PathingBehavior) mod.getBaritone().getPathingBehavior()).requestPause();
                            LookHelper.lookAt(mod, projectile.position.add(0.0D, 0.3D, 0.0D));
                        }
                        return true;
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
            Debug.logWarning(e.getMessage());
        }
        for (SkeletonEntity skeleton : mod.getEntityTracker().getTrackedEntities(SkeletonEntity.class)) {
            if (skeleton.distanceTo((Entity) mod.getPlayer()) > 10.0F || !skeleton.canSee((Entity) mod.getPlayer()))
                continue;
            if (skeleton.getItemUseTime() > 15)
                return true;
        }
        return false;
    }

    private Optional<Entity> getUniversallyDangerousMob(AltoClefController mod) {
        Class<?>[] dangerousMobs = new Class[]{WardenEntity.class, WitherEntity.class, WitherSkeletonEntity.class, HoglinEntity.class, ZoglinEntity.class, PiglinBruteEntity.class, VindicatorEntity.class};
        double range = 6.0D;
        for (Class<?> dangerous : dangerousMobs) {
            Optional<Entity> entity = mod.getEntityTracker().getClosestEntity(new Class[]{dangerous});
            if (entity.isPresent() && (
                    (Entity) entity.get()).squaredDistanceTo((Entity) mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, entity.get()))
                return entity;
        }
        return Optional.empty();
    }

    private boolean isInDanger(AltoClefController mod) {
        boolean witchNearby = mod.getEntityTracker().entityFound(new Class[]{WitchEntity.class});
        double safeKeepDistance = 8.0D;
        float health = mod.getPlayer().getHealth();
        if (health <= 10.0F && witchNearby)
            safeKeepDistance = 30.0D;
        if (mod.getPlayer().hasStatusEffect(StatusEffects.WITHER) || (mod
                .getPlayer().hasStatusEffect(StatusEffects.POISON) && witchNearby))
            safeKeepDistance = 30.0D;
        if (WorldHelper.isVulnerable(mod.getPlayer()))
            try {
                LivingEntity player = mod.getPlayer();
                List<LivingEntity> hostiles = mod.getEntityTracker().getHostiles();
                synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                    for (Entity entity : hostiles) {
                        if (entity.isInRange((Entity) player, safeKeepDistance) &&
                                !mod.getBehaviour().shouldExcludeFromForcefield(entity) &&
                                EntityHelper.isAngryAtPlayer(mod, entity) && entity != mod.getPlayer())
                            return true;
                    }
                }
            } catch (Exception e) {
                Debug.logWarning("Weird multithread exception. Will fix later. " + e.getMessage());
            }
        return false;
    }

    public void setTargetEntity(Entity entity) {
        this.targetEntity = entity;
    }

    public void resetTargetEntity() {
        this.targetEntity = null;
    }

    public void setForceFieldRange(double range) {
        this.killAura.setRange(range);
    }

    public void resetForceField() {
        this.killAura.setRange(Double.POSITIVE_INFINITY);
    }

    public boolean isDoingAcrobatics() {
        return this.doingFunkyStuff;
    }

    public boolean isPuttingOutFire() {
        return this.wasPuttingOutFire;
    }

    public boolean isActive() {
        return true;
    }

    protected void onTaskFinish(AltoClefController mod) {
    }

    public String getName() {
        return "Mob Defense";
    }
}