package adris.altoclef.chains;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.GetOutOfWaterTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.SafeRandomShimmyTask;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.LinkedList;

public class UnstuckChain extends SingleTaskChain {
    private final LinkedList<Vec3d> posHistory = new LinkedList<>();
    private final TimerGame shimmyTimer = new TimerGame(5.0);
    private final TimerGame placeBlockGoToBlockTimeout = new TimerGame(5.0);

    private boolean isProbablyStuck = false;
    private int eatingTicks = 0;
    private boolean interruptedEating = false;
    private boolean startedShimmying = false;
    private BlockPos placeBlockGoToBlock = null;

    public UnstuckChain(TaskRunner runner) {
        super(runner);
    }

    @Override
    public float getPriority() {
        if (controller == null || !controller.getTaskRunner().isActive()) {
            return Float.NEGATIVE_INFINITY;
        }

        isProbablyStuck = false;

        // Don't run if a container is open (server side equivalent check)
        // This logic is complex and should be handled by tasks. For now, we assume no container is open.

        LivingEntity player = controller.getEntity();
        posHistory.addFirst(player.getPos());
        if (posHistory.size() > 500) {
            posHistory.removeLast();
        }

        checkStuckInWater();
        checkStuckInPowderSnow();
        checkEatingGlitch();
        checkStuckOnEndPortalFrame();

        if (isProbablyStuck) return 65.0F;

        if (startedShimmying && !shimmyTimer.elapsed()) {
            setTask(new SafeRandomShimmyTask());
            return 65.0F;
        }
        startedShimmying = false;

        if (placeBlockGoToBlockTimeout.elapsed()) {
            placeBlockGoToBlock = null;
        }
        if (placeBlockGoToBlock != null) {
            setTask(new GetToBlockTask(placeBlockGoToBlock, false));
            return 65.0F;
        }

        return Float.NEGATIVE_INFINITY;
    }

    private void checkStuckInWater() {
        if (posHistory.size() < 100) return;

        LivingEntity player = controller.getEntity();
        World world = controller.getWorld();

        if (!world.getBlockState(player.getBlockPos()).isOf(Blocks.WATER)) return;
        if (player.isOnGround() || player.getAir() < player.getMaxAir()) {
            posHistory.clear();
            return;
        }

        Vec3d firstPos = posHistory.get(0);
        for (int i = 1; i < 100; i++) {
            Vec3d nextPos = posHistory.get(i);
            if (Math.abs(firstPos.getX() - nextPos.getX()) > 0.75 || Math.abs(firstPos.getZ() - nextPos.getZ()) > 0.75) {
                return;
            }
        }
        posHistory.clear();
        setTask(new GetOutOfWaterTask());
        isProbablyStuck = true;
    }

    private void checkStuckInPowderSnow() {
        LivingEntity player = controller.getEntity();
        if (player.inPowderSnow) {
            isProbablyStuck = true;
            BlockPos playerPos = player.getBlockPos();
            BlockPos toBreak = null;
            if (player.getWorld().getBlockState(playerPos).isOf(Blocks.POWDER_SNOW)) {
                toBreak = playerPos;
            } else if (player.getWorld().getBlockState(playerPos.up()).isOf(Blocks.POWDER_SNOW)) {
                toBreak = playerPos.up();
            }

            if (toBreak != null) {
                setTask(new DestroyBlockTask(toBreak));
            } else {
                setTask(new SafeRandomShimmyTask());
            }
        }
    }

    private void checkStuckOnEndPortalFrame() {
        BlockState standingOn = controller.getWorld().getBlockState(controller.getEntity().getSteppingPosition());
        if (standingOn.isOf(Blocks.END_PORTAL_FRAME) && !standingOn.get(EndPortalFrameBlock.EYE)) {
            if (!controller.getFoodChain().isTryingToEat()) {
                isProbablyStuck = true;
                controller.getBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
            }
        }
    }

    private void checkEatingGlitch() {
        FoodChain foodChain = controller.getFoodChain();
        if (interruptedEating) {
            foodChain.shouldStop(false);
            interruptedEating = false;
        }

        if (foodChain.isTryingToEat()) {
            eatingTicks++;
        } else {
            eatingTicks = 0;
        }

        if (eatingTicks > 140) { // Over 7 seconds of eating
            Debug.logMessage("Bot is probably stuck trying to eat. Resetting action.");
            foodChain.shouldStop(true);
            eatingTicks = 0;
            interruptedEating = true;
            isProbablyStuck = true;
        }
    }

    @Override
    public boolean isActive() {
        return true; // Always check for being stuck.
    }

    @Override
    protected void onTaskFinish(AltoClefController controller) {
    }

    @Override
    public String getName() {
        return "Unstuck Chain";
    }
}