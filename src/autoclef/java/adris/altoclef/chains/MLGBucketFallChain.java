package adris.altoclef.chains;

import adris.altoclef.AltoClefController;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.MLGBucketTask;
import adris.altoclef.tasksystem.ITaskOverridesGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.RaycastContext;

import java.util.Optional;

public class MLGBucketFallChain extends SingleTaskChain implements ITaskOverridesGrounded {
    private final TimerGame tryCollectWaterTimer = new TimerGame(4.0D);

    private final TimerGame pickupRepeatTimer = new TimerGame(0.25D);

    private MLGBucketTask lastMLG = null;

    private boolean wasPickingUp = false;

    private boolean doingChorusFruit = false;

    public MLGBucketFallChain(TaskRunner runner) {
        super(runner);
    }

    protected void onTaskFinish(AltoClefController mod) {
    }

    public float getPriority() {
        if (!AltoClefController.inGame())
            return Float.NEGATIVE_INFINITY;
        AltoClefController mod = controller;
        if (isFalling(mod)) {
            this.tryCollectWaterTimer.reset();
            setTask((Task) new MLGBucketTask());
            this.lastMLG = (MLGBucketTask) this.mainTask;
            return 100.0F;
        }
        if (!this.tryCollectWaterTimer.elapsed())
            if (mod.getItemStorage().hasItem(new Item[]{Items.BUCKET}) && !mod.getItemStorage().hasItem(new Item[]{Items.WATER_BUCKET}) && this.lastMLG != null) {
                boolean isPlacedWater;
                BlockPos placed = this.lastMLG.getWaterPlacedPos();
                try {
                    isPlacedWater = (mod.getWorld().getBlockState(placed).getBlock() == Blocks.WATER);
                } catch (Exception e) {
                    isPlacedWater = false;
                }
                if (placed != null && placed.isCenterWithinDistance((Position) mod.getPlayer().getPos(), 5.5D) && isPlacedWater) {
                    BlockPos toInteract = placed;
                    mod.getBehaviour().push();
                    mod.getBehaviour().setRayTracingFluidHandling(RaycastContext.FluidHandling.SOURCE_ONLY);
                    Optional<Rotation> reach = LookHelper.getReach(controller, toInteract, Direction.UP);
                    if (reach.isPresent()) {
                        mod.getBaritone().getLookBehavior().updateTarget(reach.get(), true);
                        if (mod.getBaritone().getEntityContext().isLookingAt(toInteract) &&
                                mod.getSlotHandler().forceEquipItem(Items.BUCKET))
                            if (this.pickupRepeatTimer.elapsed()) {
                                this.pickupRepeatTimer.reset();
                                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                                this.wasPickingUp = true;
                            } else if (this.wasPickingUp) {
                                this.wasPickingUp = false;
                            }
                    } else {
                        setTask((Task) TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1));
                    }
                    mod.getBehaviour().pop();
                    return 60.0F;
                }
            }
        if (this.wasPickingUp) {
            this.wasPickingUp = false;
            this.lastMLG = null;
        }
        if (mod.getPlayer().hasStatusEffect(StatusEffects.LEVITATION) && ((StatusEffectInstance) mod
                .getPlayer().getActiveStatusEffects().get(StatusEffects.LEVITATION)).getDuration() <= 70 && mod
                .getItemStorage().hasItemInventoryOnly(new Item[]{Items.CHORUS_FRUIT}) && !mod.getItemStorage().hasItemInventoryOnly(new Item[]{Items.WATER_BUCKET})) {
            this.doingChorusFruit = true;
            mod.getSlotHandler().forceEquipItem(Items.CHORUS_FRUIT);
            mod.getInputControls().hold(Input.CLICK_RIGHT);
            mod.getExtraBaritoneSettings().setInteractionPaused(true);
        } else if (this.doingChorusFruit) {
            this.doingChorusFruit = false;
            mod.getInputControls().release(Input.CLICK_RIGHT);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
        }
        this.lastMLG = null;
        return Float.NEGATIVE_INFINITY;
    }

    public String getName() {
        return "MLG Water Bucket Fall Chain";
    }

    public boolean isActive() {
        return true;
    }

    public boolean doneMLG() {
        return (this.lastMLG == null);
    }

    public boolean isChorusFruiting() {
        return this.doingChorusFruit;
    }

    public boolean isFalling(AltoClefController mod) {
        if (!mod.getModSettings().shouldAutoMLGBucket())
            return false;
        if (mod.getPlayer().isSwimming() || mod.getPlayer().isTouchingWater() || mod.getPlayer().isOnGround() || mod.getPlayer().isClimbing())
            return false;
        double ySpeed = (mod.getPlayer().getVelocity()).y;
        return (ySpeed < -0.7D);
    }
}
