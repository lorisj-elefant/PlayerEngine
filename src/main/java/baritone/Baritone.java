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

package baritone;

import baritone.api.IBaritone;
import baritone.api.Settings;
import baritone.api.cache.IWorldProvider;
import baritone.api.event.listener.IEventBus;
import baritone.api.process.IBaritoneProcess;
import baritone.api.utils.IEntityContext;
import baritone.autoclef.AltoClefSettings;
import baritone.behavior.Behavior;
import baritone.behavior.InventoryBehavior;
import baritone.behavior.LookBehavior;
import baritone.behavior.MemoryBehavior;
import baritone.behavior.PathingBehavior;
import baritone.cache.WorldProvider;
import baritone.command.defaults.DefaultCommands;
import baritone.command.manager.BaritoneCommandManager;
import baritone.event.GameEventHandler;
import baritone.process.BackfillProcess;
import baritone.process.BuilderProcess;
import baritone.process.CustomGoalProcess;
import baritone.process.ExploreProcess;
import baritone.process.FarmProcess;
import baritone.process.FishingProcess;
import baritone.process.FollowProcess;
import baritone.process.GetToBlockProcess;
import baritone.process.MineProcess;
import baritone.utils.BlockStateInterface;
import baritone.utils.InputOverrideHandler;
import baritone.utils.PathingControlManager;
import baritone.utils.player.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * @author Brady
 * @since 7/31/2018
 */
public class Baritone implements IBaritone {

    private final Settings settings;
    private final GameEventHandler gameEventHandler;

    private final PathingBehavior pathingBehavior;
    private final LookBehavior lookBehavior;
    private final MemoryBehavior memoryBehavior;
    private final InventoryBehavior inventoryBehavior;
    private final InputOverrideHandler inputOverrideHandler;

    private final FollowProcess followProcess;
    private final MineProcess mineProcess;
    private final GetToBlockProcess getToBlockProcess;
    private final CustomGoalProcess customGoalProcess;
    private final BuilderProcess builderProcess;
    private final ExploreProcess exploreProcess;
    private final BackfillProcess backfillProcess;
    private final FarmProcess farmProcess;
    private final FishingProcess fishingProcess;
    private final IBaritoneProcess execControlProcess;

    private final PathingControlManager pathingControlManager;
    private final BaritoneCommandManager commandManager;

    private final IEntityContext playerContext;

    public BlockStateInterface bsi;
    public AltoClefSettings altoClefSettings;

    public Baritone(LivingEntity player) {
        this.altoClefSettings = new AltoClefSettings();
        this.settings = new Settings();
        this.gameEventHandler = new GameEventHandler(this);

        // Define this before behaviors try and get it, or else it will be null and the builds will fail!
        this.playerContext = new EntityContext(player);

        {
            // the Behavior constructor calls baritone.registerBehavior(this) so this populates the behaviors arraylist
            pathingBehavior = new PathingBehavior(this);
            lookBehavior = new LookBehavior(this);
            memoryBehavior = new MemoryBehavior(this);
            inventoryBehavior = new InventoryBehavior(this);
            inputOverrideHandler = new InputOverrideHandler(this);
        }

        this.pathingControlManager = new PathingControlManager(this);
        {
            this.pathingControlManager.registerProcess(followProcess = new FollowProcess(this));
            this.pathingControlManager.registerProcess(mineProcess = new MineProcess(this));
            this.pathingControlManager.registerProcess(customGoalProcess = new CustomGoalProcess(this)); // very high iq
            this.pathingControlManager.registerProcess(getToBlockProcess = new GetToBlockProcess(this));
            this.pathingControlManager.registerProcess(builderProcess = new BuilderProcess(this));
            this.pathingControlManager.registerProcess(exploreProcess = new ExploreProcess(this));
            this.pathingControlManager.registerProcess(backfillProcess = new BackfillProcess(this));
            this.pathingControlManager.registerProcess(farmProcess = new FarmProcess(this));
            this.pathingControlManager.registerProcess(fishingProcess = new FishingProcess(this));
        }

        this.commandManager = new BaritoneCommandManager(this);
        this.execControlProcess = DefaultCommands.controlCommands.registerProcess(this);
    }

    @Override
    public PathingControlManager getPathingControlManager() {
        return this.pathingControlManager;
    }

    public void registerBehavior(Behavior behavior) {
        this.gameEventHandler.registerEventListener(behavior);
    }

    @Override
    public InputOverrideHandler getInputOverrideHandler() {
        return this.inputOverrideHandler;
    }

    @Override
    public CustomGoalProcess getCustomGoalProcess() {
        return this.customGoalProcess;
    }

    @Override
    public GetToBlockProcess getGetToBlockProcess() {
        return this.getToBlockProcess;
    }

    @Override
    public IEntityContext getEntityContext() {
        return this.playerContext;
    }

    public MemoryBehavior getMemoryBehavior() {
        return this.memoryBehavior;
    }

    @Override
    public FollowProcess getFollowProcess() {
        return this.followProcess;
    }

    @Override
    public BuilderProcess getBuilderProcess() {
        return this.builderProcess;
    }

    public InventoryBehavior getInventoryBehavior() {
        return this.inventoryBehavior;
    }

    @Override
    public LookBehavior getLookBehavior() {
        return this.lookBehavior;
    }

    public ExploreProcess getExploreProcess() {
        return this.exploreProcess;
    }

    @Override
    public MineProcess getMineProcess() {
        return this.mineProcess;
    }

    public FarmProcess getFarmProcess() {
        return this.farmProcess;
    }

    @Override
    public PathingBehavior getPathingBehavior() {
        return this.pathingBehavior;
    }

    @Override
    public WorldProvider getWorldProvider() {
        return (WorldProvider) IWorldProvider.KEY.get(this.getEntityContext().world());
    }

    @Override
    public IEventBus getGameEventHandler() {
        return this.gameEventHandler;
    }

    @Override
    public BaritoneCommandManager getCommandManager() {
        return this.commandManager;
    }

    public IBaritoneProcess getExecControlProcess() {
        return execControlProcess;
    }

    @Override
    public boolean isActive() {
        return this.pathingControlManager.isActive();
    }

    public Settings settings() {
        return this.settings;
    }

    public AltoClefSettings getExtraBaritoneSettings() {
        return altoClefSettings;
    }

    @Override
    public void logDebug(String message) {
        Automatone.LOGGER.debug(message);
    }

    @Override
    public void serverTick() {
        this.getGameEventHandler().onTickServer();
    }

    public FishingProcess getFishingProcess() {
        return this.fishingProcess;
    }
}
