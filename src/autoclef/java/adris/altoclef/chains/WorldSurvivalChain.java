package adris.altoclef.chains;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.movement.EscapeFromLavaTask;
import adris.altoclef.tasks.movement.SafeRandomShimmyTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Optional;

public class WorldSurvivalChain extends SingleTaskChain {
    private final TimerGame wasInLavaTimer = new TimerGame(1.0D);

    private final TimerGame portalStuckTimer = new TimerGame(5.0D);

    private boolean wasAvoidingDrowning;

    private BlockPos extinguishWaterPosition;

    public WorldSurvivalChain(TaskRunner runner) {
        super(runner);
    }

    protected void onTaskFinish(AltoClefController mod) {
    }

    public float getPriority() {
        if (!AltoClefController.inGame())
            return Float.NEGATIVE_INFINITY;
        AltoClefController mod = controller;
        handleDrowning(mod);
        if (isInLavaOhShit(mod) && mod.getBehaviour().shouldEscapeLava()) {
            setTask((Task) new EscapeFromLavaTask(mod));
            return 100.0F;
        }
        if (isInFire(mod)) {
            setTask((Task) new DoToClosestBlockTask(adris.altoclef.tasks.construction.PutOutFireTask::new, new Block[]{Blocks.FIRE, Blocks.SOUL_FIRE}));
            return 100.0F;
        }
        if (mod.getModSettings().shouldExtinguishSelfWithWater()) {
            if ((!(this.mainTask instanceof EscapeFromLavaTask) || !isCurrentlyRunning(mod)) && mod.getPlayer().isOnFire() && !mod.getPlayer().hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && !mod.getWorld().getDimension().ultraWarm()) {
                if (mod.getItemStorage().hasItem(new Item[]{Items.WATER_BUCKET})) {
                    BlockPos targetWaterPos = mod.getPlayer().getBlockPos();
                    if (WorldHelper.isSolidBlock(controller, targetWaterPos.down()) && WorldHelper.canPlace(controller, targetWaterPos)) {
                        Optional<Rotation> reach = LookHelper.getReach(controller, targetWaterPos.down(), Direction.UP);
                        if (reach.isPresent()) {
                            mod.getBaritone().getLookBehavior().updateTarget(reach.get(), true);
                            if (mod.getBaritone().getEntityContext().isLookingAt(targetWaterPos.down()) &&
                                    mod.getSlotHandler().forceEquipItem(Items.WATER_BUCKET)) {
                                this.extinguishWaterPosition = targetWaterPos;
                                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                                setTask(null);
                                return 90.0F;
                            }
                        }
                    }
                }
                setTask((Task) new DoToClosestBlockTask(adris.altoclef.tasks.movement.GetToBlockTask::new, new Block[]{Blocks.WATER}));
                return 90.0F;
            }
            if (mod.getItemStorage().hasItem(new Item[]{Items.BUCKET}) && this.extinguishWaterPosition != null && mod.getBlockScanner().isBlockAtPosition(this.extinguishWaterPosition, new Block[]{Blocks.WATER})) {
                setTask((Task) new InteractWithBlockTask(new ItemTarget(Items.BUCKET, 1), Direction.UP, this.extinguishWaterPosition.down(), true));
                return 60.0F;
            }
            this.extinguishWaterPosition = null;
        }
        if (isStuckInNetherPortal()) {
            mod.getExtraBaritoneSettings().setInteractionPaused(true);
        } else {
            this.portalStuckTimer.reset();
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
        }
        if (this.portalStuckTimer.elapsed()) {
            setTask((Task) new SafeRandomShimmyTask());
            return 60.0F;
        }
        return Float.NEGATIVE_INFINITY;
    }

    private void handleDrowning(AltoClefController mod) {
        boolean avoidedDrowning = false;
        if (mod.getModSettings().shouldAvoidDrowning() &&
                !mod.getBaritone().getPathingBehavior().isPathing() &&
                mod.getPlayer().isTouchingWater() && mod.getPlayer().getAir() < mod.getPlayer().getMaxAir()) {
            mod.getInputControls().hold(Input.JUMP);
            avoidedDrowning = true;
            this.wasAvoidingDrowning = true;
        }
        if (this.wasAvoidingDrowning && !avoidedDrowning) {
            this.wasAvoidingDrowning = false;
            mod.getInputControls().release(Input.JUMP);
        }
    }

    private boolean isInLavaOhShit(AltoClefController mod) {
        if (mod.getPlayer().isInLava() && !mod.getPlayer().hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            this.wasInLavaTimer.reset();
            return true;
        }
        return (mod.getPlayer().isOnFire() && !this.wasInLavaTimer.elapsed());
    }

    private boolean isInFire(AltoClefController mod) {
        if (mod.getPlayer().isOnFire() && !mod.getPlayer().hasStatusEffect(StatusEffects.FIRE_RESISTANCE))
            for (BlockPos pos : WorldHelper.getBlocksTouchingPlayer(controller.getPlayer())) {
                Block b = mod.getWorld().getBlockState(pos).getBlock();
                if (b instanceof net.minecraft.block.AbstractFireBlock)
                    return true;
            }
        return false;
    }

    private boolean isStuckInNetherPortal() {
        return (WorldHelper.isInNetherPortal(controller) &&
                !controller.getUserTaskChain().getCurrentTask().thisOrChildSatisfies(task -> task instanceof adris.altoclef.tasks.movement.EnterNetherPortalTask));
    }

    public String getName() {
        return "Misc World Survival Chain";
    }

    public boolean isActive() {
        return true;
    }

    protected void onStop() {
        super.onStop();
    }
}
