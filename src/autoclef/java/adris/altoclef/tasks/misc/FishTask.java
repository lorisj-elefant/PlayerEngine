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
import baritone.Baritone;
import baritone.process.FishingProcess;
import net.minecraft.item.Items;

/**
 * A task to make the bot fish automatically.
 * This is a high-level wrapper around Automatone's FishingProcess.
 * This task runs indefinitely until interrupted.
 */
public class FishTask extends Task {

    @Override
    protected void onStart() {
       ((Baritone) controller.getBaritone()).getFishingProcess().fish();
    }

    @Override
    protected Task onTick() {
        FishingProcess fishingProcess = ((Baritone) controller.getBaritone()).getFishingProcess();

        if(!controller.getSlotHandler().forceEquipItem(Items.FISHING_ROD)){
            setDebugState("Can't fish without a fishing rod");
            return null;
        }

        if (!fishingProcess.isActive()) {
            onStart();
        }

        setDebugState("Fishing with Automatone...");
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        FishingProcess fishingProcess = ((Baritone) controller.getBaritone()).getFishingProcess();
        if (fishingProcess.isActive()) {
            fishingProcess.onLostControl();
        }
    }

    @Override
    public boolean isFinished() {
       return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof FishTask;
    }

    @Override
    protected String toDebugString() {
        return "Fishing";
    }
}