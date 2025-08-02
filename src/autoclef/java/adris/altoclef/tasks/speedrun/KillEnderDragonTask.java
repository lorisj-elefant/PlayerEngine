package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.mixins.LivingEntityMixin;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.entity.AbstractKillEntityTask;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.resources.CollectBlockByOneTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.utils.Rotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class KillEnderDragonTask extends Task {
  private static final String[] DIAMOND_ARMORS = new String[] { "diamond_chestplate", "diamond_leggings", "diamond_helmet", "diamond_boots" };
  
  private final TimerGame lookDownTimer = new TimerGame(0.5D);
  
  private final Task collectBuildMaterialsTask = (Task)new CollectBlockByOneTask.CollectEndStoneTask(100);
  
  private final PunkEnderDragonTask punkTask = new PunkEnderDragonTask();
  
  private BlockPos exitPortalTop;
  
  private static Task getPickupTaskIfAny(AltoClefController mod, Item... itemsToPickup) {
    for (Item check : itemsToPickup) {
      if (mod.getEntityTracker().itemDropped(new Item[] { check }))
        return (Task)new PickupDroppedItemTask(new ItemTarget(check), true); 
    } 
    return null;
  }
  
  protected void onStart() {
    AltoClefController mod = controller;
    mod.getBehaviour().push();
    mod.getBehaviour().addForceFieldExclusion(entity -> (entity instanceof net.minecraft.entity.mob.EndermanEntity || entity instanceof EnderDragonEntity || entity instanceof net.minecraft.entity.boss.dragon.EnderDragonPart));
    mod.getBehaviour().setPreferredStairs(true);
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
    if (this.exitPortalTop == null)
      this.exitPortalTop = locateExitPortalTop(mod); 
    List<Item> toPickUp = new ArrayList<>(Arrays.asList(new Item[] { Items.DIAMOND_SWORD, Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET }));
    if (StorageHelper.calculateInventoryFoodScore(mod) < 10)
      toPickUp.addAll(Arrays.asList(new Item[] { Items.BREAD, Items.COOKED_BEEF, Items.COOKED_CHICKEN, Items.COOKED_MUTTON, Items.COOKED_RABBIT, Items.COOKED_PORKCHOP })); 
    Task pickupDrops = getPickupTaskIfAny(mod, (Item[])toPickUp.toArray(x$0 -> new Item[x$0]));
    if (pickupDrops != null) {
      setDebugState("Picking up drops in end.");
      return pickupDrops;
    } 
    for (Item armor : ItemHelper.DIAMOND_ARMORS) {
      try {
        if (mod.getItemStorage().hasItem(new Item[] { armor }) && !StorageHelper.isArmorEquipped(mod, new Item[] { armor })) {
          setDebugState("Equipping " + String.valueOf(armor));
          return (Task)new EquipArmorTask(new Item[] { armor });
        } 
      } catch (NullPointerException e) {
        Debug.logError("NullpointerException that Should never happen.");
        e.printStackTrace();
      } 
    } 
    if (!isRailingOnDragon() && this.lookDownTimer.elapsed() && !mod.getControllerExtras().isBreakingBlock() && 
      mod.getPlayer().isOnGround()) {
      this.lookDownTimer.reset();
      mod.getBaritone().getLookBehavior().updateTarget(new Rotation(0.0F, 90.0F), true);
    } 
    if (mod.getBlockScanner().anyFound(new Block[] { Blocks.END_PORTAL })) {
      setDebugState("Entering portal to beat the game.");
      return (Task)new DoToClosestBlockTask(blockPos -> new GetToBlockTask(blockPos.up(), false), new Block[] { Blocks.END_PORTAL });
    } 
    int MINIMUM_BUILDING_BLOCKS = 1;
    if ((mod.getEntityTracker().entityFound(new Class[] { EndCrystalEntity.class }) && mod.getItemStorage().getItemCount(new Item[] { Items.DIRT, Items.COBBLESTONE, Items.NETHERRACK, Items.END_STONE }) < MINIMUM_BUILDING_BLOCKS) || (this.collectBuildMaterialsTask.isActive() && !this.collectBuildMaterialsTask.isFinished())) {
      if (StorageHelper.miningRequirementMetInventory(controller, MiningRequirement.WOOD)) {
        mod.getBehaviour().addProtectedItems(new Item[] { Items.END_STONE });
        setDebugState("Collecting building blocks to pillar to crystals");
        return this.collectBuildMaterialsTask;
      } 
    } else {
      mod.getBehaviour().removeProtectedItems(new Item[] { Items.END_STONE });
    } 
    if (mod.getEntityTracker().entityFound(new Class[] { EndCrystalEntity.class })) {
      setDebugState("Kamakazeeing crystals");
      return (Task)new DoToClosestEntityTask(toDestroy -> {
            if (toDestroy.isInRange((Entity)mod.getPlayer(), 7.0D))
              mod.getControllerExtras().attack(toDestroy); 
            return (Task)new GetToBlockTask(toDestroy.getBlockPos().add(1, 0, 0), false);
          },new Class[] { EndCrystalEntity.class });
    } 
    if (mod.getEntityTracker().entityFound(new Class[] { EnderDragonEntity.class })) {
      setDebugState("Punking dragon");
      return (Task)this.punkTask;
    } 
    setDebugState("Couldn't find ender dragon... This can be very good or bad news.");
    return null;
  }
  
  protected void onStop(Task interruptTask) {
    controller.getBehaviour().pop();
  }
  
  protected boolean isEqual(Task other) {
    return other instanceof adris.altoclef.tasks.speedrun.KillEnderDragonTask;
  }
  
  protected String toDebugString() {
    return "Killing Ender Dragon";
  }
  
  private boolean isRailingOnDragon() {
    return (this.punkTask.getMode() == Mode.RAILING);
  }
  
  private BlockPos locateExitPortalTop(AltoClefController mod) {
    if (!mod.getChunkTracker().isChunkLoaded(new BlockPos(0, 64, 0)))
      return null; 
    int height = WorldHelper.getGroundHeight(mod, 0, 0, new Block[] { Blocks.BEDROCK });
    if (height != -1)
      return new BlockPos(0, height, 0); 
    return null;
  }

  private enum Mode {
    WAITING_FOR_PERCH,
    RAILING
  }

  private class PunkEnderDragonTask extends Task {

    private final HashMap<BlockPos, Double> breathCostMap = new HashMap<>();
    private final TimerGame hitHoldTimer = new TimerGame(0.1);
    private final TimerGame hitResetTimer = new TimerGame(0.4);
    private final TimerGame randomWanderChangeTimeout = new TimerGame(20);
    private Mode mode = Mode.WAITING_FOR_PERCH;

    private BlockPos randomWanderPos;
    private boolean wasHitting;
    private boolean wasReleased;

    private PunkEnderDragonTask() {
    }

    public Mode getMode() {
      return mode;
    }

    private void hit(AltoClefController mod) {
      mod.getExtraBaritoneSettings().setInteractionPaused(true);
      if (!wasHitting) {
        wasHitting = true;
        wasReleased = false;
        hitHoldTimer.reset();
        hitResetTimer.reset();
        Debug.logInternal("HIT");
        mod.getInputControls().tryPress(Input.CLICK_LEFT);
        //mod.getPlayer().swingHand(Hand.MAIN_HAND);
      }
      if (hitHoldTimer.elapsed()) {
        if (!wasReleased) {
          Debug.logInternal("    up");
          //mod.getControllerExtras().mouseClickOverride(0, false);
          wasReleased = true;
        }
      }
      if (wasHitting && hitResetTimer.elapsed() && getAttackCooldownProgress(mod.getPlayer(), 0) > 0.99) {
        wasHitting = false;
        // Code duplication maybe?
        //mod.getControllerExtras().mouseClickOverride(0, false);
        mod.getExtraBaritoneSettings().setInteractionPaused(false);
        hitResetTimer.reset();
      }
    }

    public float getAttackCooldownProgressPerTick(LivingEntity entity) {
      return (float)((double)1.0F / entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * (double)20.0F);
    }

    public float getAttackCooldownProgress(LivingEntity entity, float baseTime) {
      return MathHelper.clamp(((float)((LivingEntityMixin)entity).getLastAttackedTicks() + baseTime) / this.getAttackCooldownProgressPerTick(entity), 0.0F, 1.0F);
    }

    private void stopHitting(AltoClefController mod) {
      if (wasHitting) {
        if (!wasReleased) {
          //mod.getControllerExtras().mouseClickOverride(0, false);
          mod.getExtraBaritoneSettings().setInteractionPaused(false);
          wasReleased = true;
        }
        wasHitting = false;
      }
    }


    @Override
    protected void onStart() {
      controller.getBaritone().getCustomGoalProcess().onLostControl();
    }

    @Override
    protected Task onTick() {
      AltoClefController mod = controller;

      if (!mod.getEntityTracker().entityFound(EnderDragonEntity.class)) {
        setDebugState("No dragon found.");
        return null;
      }
      List<EnderDragonEntity> dragons = mod.getEntityTracker().getTrackedEntities(EnderDragonEntity.class);
      if (!dragons.isEmpty()) {
        for (EnderDragonEntity dragon : dragons) {
          Phase dragonPhase = dragon.getPhaseManager().getCurrent();
          //Debug.logInternal("PHASE: " + dragonPhase);
          boolean perchingOrGettingReady = dragonPhase.getType() == PhaseType.LANDING || dragonPhase.isSittingOrHovering();
          switch (mode) {
            case RAILING -> {
              if (!perchingOrGettingReady) {
                Debug.logMessage("Dragon no longer perching.");
                mod.getBaritone().getCustomGoalProcess().onLostControl();
                mode = Mode.WAITING_FOR_PERCH;
                break;
              }
              //DamageSource.DRAGON_BREATH
              Entity head = dragon.head;
              // Go for the head
              if (head.isInRange(mod.getPlayer(), 7.5) && dragon.ticksSinceDeath <= 1) {
                // Equip weapon
                AbstractKillEntityTask.equipWeapon(mod);
                // Look torwards da dragon
                Vec3d targetLookPos = head.getPos().add(0, 3, 0);
                Rotation targetRotation = RotationUtils.calcRotationFromVec3d(mod.getBaritone().getEntityContext().headPos(), targetLookPos, mod.getBaritone().getEntityContext().entityRotations());
                mod.getBaritone().getLookBehavior().updateTarget(targetRotation, true);
                // Also look towards da dragon
//                OptionsVer.setAutoJump(false);
                mod.getBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
                hit(mod);
              } else {
                stopHitting(mod);
              }
              if (!mod.getBaritone().getCustomGoalProcess().isActive()) {
                // Set goal to closest block within the pillar that's by the head.
                if (exitPortalTop != null) {
                  int bottomYDelta = -3;
                  BlockPos closest = null;
                  double closestDist = Double.POSITIVE_INFINITY;
                  for (int dx = -2; dx <= 2; ++dx) {
                    for (int dz = -2; dz <= 2; ++dz) {
                      // We have sort of a rounded circle here.
                      if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                      BlockPos toCheck = exitPortalTop.add(dx,bottomYDelta,dz);
                      double distSq = BlockPosVer.getSquaredDistance(toCheck,head.getPos());
                      if (distSq < closestDist) {
                        closest = toCheck;
                        closestDist = distSq;
                      }
                    }
                  }
                  if (closest != null) {
                    mod.getBaritone().getCustomGoalProcess().setGoalAndPath(
                            new GoalGetToBlock(closest)
                    );
                  }
                }
              }
              setDebugState("Railing on dragon");
            }
            case WAITING_FOR_PERCH -> {
              stopHitting(mod);
              if (perchingOrGettingReady) {
                // We're perching!!
                mod.getBaritone().getCustomGoalProcess().onLostControl();
                Debug.logMessage("Dragon perching detected. Dabar duosiu į snuki.");
                mode = Mode.RAILING;
                break;
              }
              // Run around aimlessly, dodging dragon fire
              if (randomWanderPos != null && WorldHelper.inRangeXZ(mod.getPlayer(), randomWanderPos, 2)) {
                randomWanderPos = null;
              }
              if (randomWanderPos != null && randomWanderChangeTimeout.elapsed()) {
                randomWanderPos = null;
                Debug.logMessage("Reset wander pos after timeout, oof");
              }
              if (randomWanderPos == null) {
                randomWanderPos = getRandomWanderPos(mod);
                randomWanderChangeTimeout.reset();
                mod.getBaritone().getCustomGoalProcess().onLostControl();
              }
              if (!mod.getBaritone().getCustomGoalProcess().isActive()) {
                mod.getBaritone().getCustomGoalProcess().setGoalAndPath(
                        new GoalGetToBlock(randomWanderPos)
                );
              }
              setDebugState("Waiting for perch");
            }
          }
        }
      }
      return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
      AltoClefController mod = controller;

      mod.getBaritone().getCustomGoalProcess().onLostControl();
      mod.getBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
      //mod.getControllerExtras().mouseClickOverride(0, false);
      mod.getExtraBaritoneSettings().setInteractionPaused(false);
    }

    @Override
    protected boolean isEqual(Task other) {
      return other instanceof PunkEnderDragonTask;
    }

    @Override
    protected String toDebugString() {
      return "Punking the dragon";
    }

    private BlockPos getRandomWanderPos(AltoClefController mod) {
      double RADIUS_RANGE = 45;
      double MIN_RADIUS = 7;
      BlockPos pos = null;
      int allowed = 5000;

      while (pos == null) {
        if (allowed-- < 0) {
          Debug.logWarning("Failed to find random solid ground in end, this may lead to problems.");
          return null;
        }
        double radius = MIN_RADIUS + (RADIUS_RANGE - MIN_RADIUS) * Math.random();
        double angle = Math.PI * 2 * Math.random();
        int x = (int) (radius * Math.cos(angle)),
                z = (int) (radius * Math.sin(angle));
        int y = WorldHelper.getGroundHeight(mod, x, z);
        if (y == -1) continue;
        BlockPos check = new BlockPos(x, y, z);
        if (mod.getWorld().getBlockState(check).getBlock() == Blocks.END_STONE) {
          // We found a spot!
          pos = check.up();
        }
      }
      return pos;
    }
  }
}
