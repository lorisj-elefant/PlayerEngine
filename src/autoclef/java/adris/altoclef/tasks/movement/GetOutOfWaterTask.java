package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.pathing.goals.Goal;
import baritone.api.utils.input.Input;
import baritone.pathing.movement.MovementHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class GetOutOfWaterTask extends CustomBaritoneGoalTask {
    private boolean startedShimmying = false;

    private final TimerGame shimmyTaskTimer = new TimerGame(5.0D);

    protected void onStart() {
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        if (mod.getPlayer().getAir() < mod.getPlayer().getMaxAir() || mod.getPlayer().isSubmergedInWater())
            return super.onTick();
        boolean hasBlockBelow = false;
        for (int i = 0; i < 3; i++) {
            if (mod.getWorld().getBlockState(mod.getPlayer().getSteppingPosition().down(i)).getBlock() != Blocks.WATER)
                hasBlockBelow = true;
        }
        boolean hasAirAbove = mod.getWorld().getBlockState(mod.getPlayer().getBlockPos().up(2)).getBlock().equals(Blocks.AIR);
        if (hasAirAbove && hasBlockBelow && StorageHelper.getNumberOfThrowawayBlocks(mod) > 0) {
            mod.getInputControls().tryPress(Input.JUMP);
            if (mod.getPlayer().isOnGround()) {
                if (!this.startedShimmying) {
                    this.startedShimmying = true;
                    this.shimmyTaskTimer.reset();
                }
                return (Task) new SafeRandomShimmyTask();
            }
            mod.getSlotHandler().forceEquipItem((Item[]) ((List) (mod.getBaritoneSettings()).acceptableThrowawayItems.get()).toArray(new Item[0]));
            LookHelper.lookAt(mod, mod.getPlayer().getSteppingPosition().down());
            mod.getInputControls().tryPress(Input.CLICK_RIGHT);
        }
        return super.onTick();
    }

    protected void onStop(Task interruptTask) {
    }

    protected Goal newGoal(AltoClefController mod) {
        return (Goal) new EscapeFromWaterGoal(mod);
    }

    protected boolean isEqual(Task other) {
        return false;
    }

    protected String toDebugString() {
        return "";
    }

    public boolean isFinished() {
        return (!controller.getPlayer().isTouchingWater() && controller.getPlayer().isOnGround());
    }

    private class EscapeFromWaterGoal implements Goal {
        private AltoClefController mod;

        private EscapeFromWaterGoal(AltoClefController mod) {
            this.mod = mod;
        }

        private boolean isWater(int x, int y, int z) {
            if (mod.getWorld() == null) return false;
            return MovementHelper.isWater(mod.getWorld().getBlockState(new BlockPos(x, y, z)));
        }

        private boolean isWaterAdjacent(int x, int y, int z) {
            return isWater(x + 1, y, z) || isWater(x - 1, y, z) || isWater(x, y, z + 1) || isWater(x, y, z - 1)
                    || isWater(x + 1, y, z - 1) || isWater(x + 1, y, z + 1) || isWater(x - 1, y, z - 1)
                    || isWater(x - 1, y, z + 1);
        }

        @Override
        public boolean isInGoal(int x, int y, int z) {
            return !isWater(x, y, z) && !isWaterAdjacent(x, y, z);
        }

        @Override
        public double heuristic(int x, int y, int z) {
            if (isWater(x, y, z)) {
                return 1;
            } else if (isWaterAdjacent(x, y, z)) {
                return 0.5f;
            }

            return 0;
        }
    }
}
