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
  private final LinkedList<Vec3d> _posHistory = new LinkedList<>();
  private final TimerGame _shimmyTimer = new TimerGame(5.0);
  private final TimerGame _placeBlockGoToBlockTimeout = new TimerGame(5.0);

  private boolean _isProbablyStuck = false;
  private int _eatingTicks = 0;
  private boolean _interruptedEating = false;
  private boolean _startedShimmying = false;
  private BlockPos _placeBlockGoToBlock = null;

  public UnstuckChain(TaskRunner runner) {
    super(runner);
  }

  @Override
  public float getPriority() {
    if (controller == null || !controller.getTaskRunner().isActive()) {
      return Float.NEGATIVE_INFINITY;
    }

    _isProbablyStuck = false;

    // Don't run if a container is open (server side equivalent check)
    // This logic is complex and should be handled by tasks. For now, we assume no container is open.

    LivingEntity player = controller.getEntity();
    _posHistory.addFirst(player.getPos());
    if (_posHistory.size() > 500) {
      _posHistory.removeLast();
    }

    checkStuckInWater();
    checkStuckInPowderSnow();
    checkEatingGlitch();
    checkStuckOnEndPortalFrame();

    if (_isProbablyStuck) return 65.0F;

    if (_startedShimmying && !_shimmyTimer.elapsed()) {
      setTask(new SafeRandomShimmyTask());
      return 65.0F;
    }
    _startedShimmying = false;

    if (_placeBlockGoToBlockTimeout.elapsed()) {
      _placeBlockGoToBlock = null;
    }
    if (_placeBlockGoToBlock != null) {
      setTask(new GetToBlockTask(_placeBlockGoToBlock, false));
      return 65.0F;
    }

    return Float.NEGATIVE_INFINITY;
  }

  private void checkStuckInWater() {
    if (_posHistory.size() < 100) return;

    LivingEntity player = controller.getEntity();
    World world = controller.getWorld();

    if (!world.getBlockState(player.getBlockPos()).isOf(Blocks.WATER)) return;
    if (player.isOnGround() || player.getAir() < player.getMaxAir()) {
      _posHistory.clear();
      return;
    }

    Vec3d firstPos = _posHistory.get(0);
    for (int i = 1; i < 100; i++) {
      Vec3d nextPos = _posHistory.get(i);
      if (Math.abs(firstPos.getX() - nextPos.getX()) > 0.75 || Math.abs(firstPos.getZ() - nextPos.getZ()) > 0.75) {
        return;
      }
    }
    _posHistory.clear();
    setTask(new GetOutOfWaterTask());
    _isProbablyStuck = true;
  }

  private void checkStuckInPowderSnow() {
    LivingEntity player = controller.getEntity();
    if (player.inPowderSnow) {
      _isProbablyStuck = true;
      BlockPos playerPos = player.getBlockPos();
      BlockPos toBreak = null;
      if (player.getWorld().getBlockState(playerPos).isOf(Blocks.POWDER_SNOW)) {
        toBreak = playerPos;
      } else if (player.getWorld().getBlockState(playerPos.up()).isOf(Blocks.POWDER_SNOW)) {
        toBreak = playerPos.up();
      }

      if(toBreak != null) {
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
        _isProbablyStuck = true;
        controller.getBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
      }
    }
  }

  private void checkEatingGlitch() {
    FoodChain foodChain = controller.getFoodChain();
    if (_interruptedEating) {
      foodChain.shouldStop(false);
      _interruptedEating = false;
    }

    if (foodChain.isTryingToEat()) {
      _eatingTicks++;
    } else {
      _eatingTicks = 0;
    }

    if (_eatingTicks > 140) { // Over 7 seconds of eating
      Debug.logMessage("Bot is probably stuck trying to eat. Resetting action.");
      foodChain.shouldStop(true);
      _eatingTicks = 0;
      _interruptedEating = true;
      _isProbablyStuck = true;
    }
  }

  @Override
  public boolean isActive() {
    return true; // Always check for being stuck.
  }

  @Override
  protected void onTaskFinish(AltoClefController controller) {}

  @Override
  public String getName() {
    return "Unstuck Chain";
  }
}