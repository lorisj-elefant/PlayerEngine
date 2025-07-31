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

    private final Integer _range;
    private final BlockPos _center;

    public FarmTask(Integer range, BlockPos center) {
        this._range = range;
        this._center = center;
    }

    public FarmTask() {
        this(null, null);
    }

    @Override
    protected void onStart() {
        IFarmProcess farmProcess = controller.getBaritone().getFarmProcess();
        if (_range != null && _center != null) {
            farmProcess.farm(_range, _center);
        } else if (_range != null) {
            farmProcess.farm(_range);
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
            return Objects.equals(task._range, _range) && Objects.equals(task._center, _center);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        if (_range != null && _center != null) {
            return "Farming in range " + _range + " around " + _center.toShortString();
        }
        return "Farming nearby";
    }
}