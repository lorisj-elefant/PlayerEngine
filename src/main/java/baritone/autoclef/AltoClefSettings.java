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

package baritone.autoclef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class AltoClefSettings {
    private static final AltoClefSettings _instance = new AltoClefSettings();
    private final Object breakMutex = new Object();
    private final Object placeMutex = new Object();
    private final Object propertiesMutex = new Object();
    private final Object globalHeuristicMutex = new Object();
    private final HashSet<BlockPos> _blocksToAvoidBreaking = new HashSet();
    private final List<Predicate<BlockPos>> _breakAvoiders = new ArrayList();
    private final List<Predicate<BlockPos>> _placeAvoiders = new ArrayList();
    private final List<Predicate<BlockPos>> _forceCanWalkOn = new ArrayList();
    private final List<Predicate<BlockPos>> _forceAvoidWalkThrough = new ArrayList();
    private final List<BiPredicate<BlockState, ItemStack>> _forceSaveTool = new ArrayList();
    private final List<BiPredicate<BlockState, ItemStack>> _forceUseTool = new ArrayList();
    private final List<BiFunction<Double, BlockPos, Double>> _globalHeuristics = new ArrayList();
    private final HashSet<Item> _protectedItems = new HashSet();
    private boolean _allowFlowingWaterPass;
    private boolean _pauseInteractions;
    private boolean _dontPlaceBucketButStillFall;
    private boolean _allowSwimThroughLava = false;
    private boolean _treatSoulSandAsOrdinaryBlock = false;
    private boolean canWalkOnEndPortal = false;

    public AltoClefSettings() {
    }

    public static AltoClefSettings getInstance() {
        return _instance;
    }

    public void canWalkOnEndPortal(boolean canWalk) {
        this.canWalkOnEndPortal = canWalk;
    }

    public void avoidBlockBreak(BlockPos pos) {
        synchronized(this.breakMutex) {
            this._blocksToAvoidBreaking.add(pos);
        }
    }

    public void avoidBlockBreak(Predicate<BlockPos> avoider) {
        synchronized(this.breakMutex) {
            this._breakAvoiders.add(avoider);
        }
    }

    public void configurePlaceBucketButDontFall(boolean allow) {
        synchronized(this.propertiesMutex) {
            this._dontPlaceBucketButStillFall = allow;
        }
    }

    public void treatSoulSandAsOrdinaryBlock(boolean enable) {
        synchronized(this.propertiesMutex) {
            this._treatSoulSandAsOrdinaryBlock = enable;
        }
    }

    public void avoidBlockPlace(Predicate<BlockPos> avoider) {
        synchronized(this.placeMutex) {
            this._placeAvoiders.add(avoider);
        }
    }

    public boolean shouldForceSaveTool(BlockState state, ItemStack tool) {
        synchronized(this.propertiesMutex) {
            return this._forceSaveTool.stream().anyMatch((pred) -> pred.test(state, tool));
        }
    }

    public boolean shouldAvoidBreaking(int x, int y, int z) {
        return this.shouldAvoidBreaking(new BlockPos(x, y, z));
    }

    public boolean shouldAvoidBreaking(BlockPos pos) {
        synchronized(this.breakMutex) {
            return this._blocksToAvoidBreaking.contains(pos) ? true : this._breakAvoiders.stream().anyMatch((pred) -> pred.test(pos));
        }
    }

    public boolean shouldAvoidPlacingAt(BlockPos pos) {
        synchronized(this.placeMutex) {
            return this._placeAvoiders.stream().anyMatch((pred) -> pred.test(pos));
        }
    }

    public boolean shouldAvoidPlacingAt(int x, int y, int z) {
        return this.shouldAvoidPlacingAt(new BlockPos(x, y, z));
    }

    public boolean canWalkOnForce(int x, int y, int z) {
        synchronized(this.propertiesMutex) {
            return this._forceCanWalkOn.stream().anyMatch((pred) -> pred.test(new BlockPos(x, y, z)));
        }
    }

    public boolean shouldAvoidWalkThroughForce(BlockPos pos) {
        synchronized(this.propertiesMutex) {
            return this._forceAvoidWalkThrough.stream().anyMatch((pred) -> pred.test(pos));
        }
    }

    public boolean shouldAvoidWalkThroughForce(int x, int y, int z) {
        return this.shouldAvoidWalkThroughForce(new BlockPos(x, y, z));
    }

    public boolean shouldForceUseTool(BlockState state, ItemStack tool) {
        synchronized(this.propertiesMutex) {
            return this._forceUseTool.stream().anyMatch((pred) -> pred.test(state, tool));
        }
    }

    public boolean shouldNotPlaceBucketButStillFall() {
        synchronized(this.propertiesMutex) {
            return this._dontPlaceBucketButStillFall;
        }
    }

    public boolean shouldTreatSoulSandAsOrdinaryBlock() {
        synchronized(this.propertiesMutex) {
            return this._treatSoulSandAsOrdinaryBlock;
        }
    }

    public boolean isInteractionPaused() {
        synchronized(this.propertiesMutex) {
            return this._pauseInteractions;
        }
    }

    public void setInteractionPaused(boolean paused) {
        synchronized(this.propertiesMutex) {
            this._pauseInteractions = paused;
        }
    }

    public boolean isFlowingWaterPassAllowed() {
        synchronized(this.propertiesMutex) {
            return this._allowFlowingWaterPass;
        }
    }

    public boolean canSwimThroughLava() {
        synchronized(this.propertiesMutex) {
            return this._allowSwimThroughLava;
        }
    }

    public void setFlowingWaterPass(boolean pass) {
        synchronized(this.propertiesMutex) {
            this._allowFlowingWaterPass = pass;
        }
    }

    public void allowSwimThroughLava(boolean allow) {
        synchronized(this.propertiesMutex) {
            this._allowSwimThroughLava = allow;
        }
    }

    public double applyGlobalHeuristic(double prev, int x, int y, int z) {
        return prev;
    }

    public HashSet<BlockPos> getBlocksToAvoidBreaking() {
        return this._blocksToAvoidBreaking;
    }

    public List<Predicate<BlockPos>> getBreakAvoiders() {
        return this._breakAvoiders;
    }

    public List<Predicate<BlockPos>> getPlaceAvoiders() {
        return this._placeAvoiders;
    }

    public List<Predicate<BlockPos>> getForceWalkOnPredicates() {
        return this._forceCanWalkOn;
    }

    public List<Predicate<BlockPos>> getForceAvoidWalkThroughPredicates() {
        return this._forceAvoidWalkThrough;
    }

    public List<BiPredicate<BlockState, ItemStack>> getForceSaveToolPredicates() {
        return this._forceSaveTool;
    }

    public List<BiPredicate<BlockState, ItemStack>> getForceUseToolPredicates() {
        return this._forceUseTool;
    }

    public List<BiFunction<Double, BlockPos, Double>> getGlobalHeuristics() {
        return this._globalHeuristics;
    }

    public boolean isItemProtected(Item item) {
        return this._protectedItems.contains(item);
    }

    public HashSet<Item> getProtectedItems() {
        return this._protectedItems;
    }

    public void protectItem(Item item) {
        this._protectedItems.add(item);
    }

    public void stopProtectingItem(Item item) {
        this._protectedItems.remove(item);
    }

    public Object getBreakMutex() {
        return this.breakMutex;
    }

    public Object getPlaceMutex() {
        return this.placeMutex;
    }

    public Object getPropertiesMutex() {
        return this.propertiesMutex;
    }

    public Object getGlobalHeuristicMutex() {
        return this.globalHeuristicMutex;
    }

    public boolean isCanWalkOnEndPortal() {
        return this.canWalkOnEndPortal;
    }
}
