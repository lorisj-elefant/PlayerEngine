/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package adris.altoclef.tasks.misc;

import adris.altoclef.tasksystem.Task;
import baritone.api.process.IFarmProcess;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

/**
 * A task to make the bot farm in a specified area.
 * This is a high-level wrapper around Automatone's FarmProcess.
 * This task runs indefinitely until interrupted.
 */
public class FarmTask extends Task {

    private final Integer range;
    private final BlockPos center;

    public FarmTask(Integer range, BlockPos center) {
        this.range = range;
        this.center = center;
    }

    public FarmTask() {
        this(null, null);
    }

    @Override
    protected void onStart() {
        IFarmProcess farmProcess = controller.getBaritone().getFarmProcess();
        if (range != null && center != null) {
            farmProcess.farm(range, center);
        } else if (range != null) {
            farmProcess.farm(range);
        } else {
            farmProcess.farm();
        }
    }

    @Override
    protected Task onTick() {
        IFarmProcess farmProcess = controller.getBaritone().getFarmProcess();

        if (!farmProcess.isActive()) {
            onStart();
        }

        setDebugState("Farming with Automatone...");
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        IFarmProcess farmProcess = controller.getBaritone().getFarmProcess();
        if (farmProcess.isActive()) {
            farmProcess.onLostControl();
        }
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof FarmTask task) {
            return Objects.equals(task.range, range) && Objects.equals(task.center, center);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        if (range != null && center != null) {
            return "Farming in range " + range + " around " + center.toShortString();
        }
        return "Farming nearby";
    }
}