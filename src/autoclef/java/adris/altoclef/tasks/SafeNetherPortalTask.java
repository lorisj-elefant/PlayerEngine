package adris.altoclef.tasks;

import adris.altoclef.AltoClefController;
import adris.altoclef.control.InputControls;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SafeNetherPortalTask extends Task {
  private final TimerGame wait = new TimerGame(1.0D);
  
  private boolean keyReset = false;
  
  private boolean finished = false;
  
  private List<BlockPos> positions = null;
  
  private List<Direction> directions = null;
  
  private Direction.Axis axis = null;
  
  protected void onStart() {
    controller.getBaritone().getInputOverrideHandler().clearAllKeys();
    this.wait.reset();
  }
  
  protected Task onTick() {
    if (!this.wait.elapsed())
      return null; 
    AltoClefController mod = controller;
    if (!this.keyReset) {
      this.keyReset = true;
      mod.getBaritone().getInputOverrideHandler().clearAllKeys();
    } 
    if (mod.getPlayer().getNetherPortalCooldown() < 10) {
      if (this.positions != null && this.directions != null) {
        BlockPos pos1 = mod.getPlayer().getSteppingPosition().offset(this.axis, 1);
        BlockPos pos2 = mod.getPlayer().getSteppingPosition().offset(this.axis, -1);
        if (mod.getWorld().getBlockState(pos1).isAir() || mod.getWorld().getBlockState(pos1).getBlock().equals(Blocks.SOUL_SAND)) {
          boolean passed = false;
          for (Direction dir : Direction.values()) {
            if (mod.getWorld().getBlockState(pos1.up().offset(dir)).getBlock().equals(Blocks.NETHER_PORTAL)) {
              passed = true;
              break;
            } 
          } 
          if (passed)
            return (Task)new ReplaceSafeBlock(pos1); 
        } 
        if (mod.getWorld().getBlockState(pos2).isAir() || mod.getWorld().getBlockState(pos2).getBlock().equals(Blocks.SOUL_SAND)) {
          boolean passed = false;
          for (Direction dir : Direction.values()) {
            if (mod.getWorld().getBlockState(pos2.up().offset(dir)).getBlock().equals(Blocks.NETHER_PORTAL)) {
              passed = true;
              break;
            } 
          } 
          if (passed)
            return (Task)new ReplaceSafeBlock(pos2); 
        } 
      } 
      this.finished = true;
      setDebugState("We are not in a portal");
      return null;
    } 
    BlockState state = mod.getWorld().getBlockState(mod.getPlayer().getBlockPos());
    if (this.positions == null || this.directions == null) {
      if (state.getBlock().equals(Blocks.NETHER_PORTAL)) {
        this.axis = (Direction.Axis)state.get((Property)Properties.HORIZONTAL_AXIS);
        this.positions = new ArrayList<>();
        this.positions.add(mod.getPlayer().getBlockPos());
        for (Direction dir : Direction.values()) {
          if (!dir.getAxis().isVertical()) {
            BlockPos pos = mod.getPlayer().getBlockPos().offset(dir);
            if (mod.getWorld().getBlockState(pos).getBlock().equals(Blocks.NETHER_PORTAL))
              this.positions.add(pos); 
          } 
        } 
        this.directions = List.of(Direction.WEST, Direction.EAST);
        if (this.axis == Direction.Axis.X)
          this.directions = List.of(Direction.NORTH, Direction.SOUTH); 
      } else {
        this.finished = true;
        setDebugState("We are not standing inside a nether portal block");
      } 
    } else {
      for (BlockPos pos : this.positions) {
        for (Direction dir : this.directions) {
          BlockPos newPos = pos.down().offset(dir);
          if (mod.getWorld().getBlockState(newPos).isAir() || mod.getWorld().getBlockState(newPos).getBlock().equals(Blocks.SOUL_SAND)) {
            setDebugState("Changing block...");
            return (Task)new ReplaceSafeBlock(newPos);
          } 
        } 
      } 
      this.finished = true;
      setDebugState("Portal is safe");
      return null;
    } 
    return null;
  }
  
  protected void onStop(Task interruptTask) {
    InputControls controls = controller.getInputControls();
    controls.release(Input.MOVE_FORWARD);
    controls.release(Input.SNEAK);
    controls.release(Input.CLICK_LEFT);
    controller.getBaritone().getInputOverrideHandler().clearAllKeys();
  }
  
  protected boolean isEqual(Task other) {
    return other instanceof adris.altoclef.tasks.SafeNetherPortalTask;
  }
  
  protected String toDebugString() {
    return "Making nether portal safe";
  }
  
  public boolean isFinished() {
    return this.finished;
  }

  private static class ReplaceSafeBlock extends Task {

    private final BlockPos pos;
    private boolean finished = false;

    public ReplaceSafeBlock(BlockPos pos) {
      this.pos = pos;
    }


    @Override
    protected void onStart() {
      controller.getBaritone().getInputOverrideHandler().clearAllKeys();
    }

    @Override
    protected Task onTick() {
      AltoClefController mod = controller;

      if (mod.getWorld().getBlockState(pos).isAir()) {
        setDebugState("Placing block...");
        return new PlaceStructureBlockTask(pos);
      }

      if (controller.getWorld().getBlockState(pos).getBlock().equals(Blocks.SOUL_SAND)) {
        LookHelper.lookAt(mod, pos);

        HitResult result = mod.getPlayer().raycast(3, 0, true);
        if (result instanceof BlockHitResult blockHitResult && mod.getWorld().getBlockState(blockHitResult.getBlockPos()).getBlock().equals(Blocks.NETHER_PORTAL)) {
          setDebugState("Getting closer to target...");
          mod.getInputControls().hold(Input.MOVE_FORWARD);
          mod.getInputControls().hold(Input.SNEAK);
        } else {
          setDebugState("Breaking block");
          mod.getInputControls().release(Input.MOVE_FORWARD);
          mod.getInputControls().release(Input.SNEAK);
          mod.getInputControls().hold(Input.CLICK_LEFT);
        }
        return null;
      }

      this.finished = true;
      return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
      InputControls controls = controller.getInputControls();

      controls.release(Input.MOVE_FORWARD);
      controls.release(Input.SNEAK);
      controls.release(Input.CLICK_LEFT);
      controller.getBaritone().getInputOverrideHandler().clearAllKeys();
    }

    @Override
    public boolean isFinished() {
      return finished;
    }

    @Override
    protected boolean isEqual(Task other) {
      return other instanceof ReplaceSafeBlock same && same.pos.equals(this.pos);
    }

    @Override
    protected String toDebugString() {
      return "Making sure " + pos + " is safe";
    }
  }

}
