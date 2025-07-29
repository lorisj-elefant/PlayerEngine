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
package adris.altoclef;

import adris.altoclef.chains.*;
import adris.altoclef.commands.BlockScanner;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.control.InputControls;
import adris.altoclef.control.PlayerExtraController;
import adris.altoclef.control.SlotHandler;
import adris.altoclef.player2api.AICommandBridge;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.trackers.*;
import adris.altoclef.trackers.storage.ContainerSubTracker;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import baritone.Baritone;
import baritone.api.IBaritone;
import baritone.api.entity.LivingEntityInventory;
import baritone.api.utils.IEntityContext;
import baritone.api.utils.IInteractionController;
import baritone.autoclef.AltoClefSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;

import java.util.Arrays;
import java.util.List;

public class AltoClefController {
    private final IBaritone _baritone;
    private final IEntityContext _ctx;

    private CommandExecutor _commandExecutor;
    private TaskRunner _taskRunner;
    private TrackerManager _trackerManager;
    private BotBehaviour _botBehaviour;
    private UserTaskChain _userTaskChain;

    // Chains
    private FoodChain _foodChain;
    private MobDefenseChain _mobDefenseChain;
    private MLGBucketFallChain _mlgBucketChain;

    // Trackers
    private ItemStorageTracker _storageTracker;
    private ContainerSubTracker _containerSubTracker;
    private EntityTracker _entityTracker;
    private BlockScanner _blockScanner;
    private SimpleChunkTracker _chunkTracker;
    private MiscBlockTracker _miscBlockTracker;
    private CraftingRecipeTracker _craftingRecipeTracker;
    private EntityStuckTracker _entityStuckTracker;
    private UserBlockRangeTracker _userBlockRangeTracker;

    private InputControls _inputControls;
    private SlotHandler _slotHandler;
    private PlayerExtraController _extraController;

    private Settings _settings;

    private boolean _paused = false;
    private Task _storedTask;
    private AICommandBridge aiBridge;
    private static long lastHeartbeatTime = System.nanoTime();
    public boolean isStopping = false;

    private PlayerEntity owner;

    public AltoClefController(IBaritone baritone) {
        this._baritone = baritone;
        this._ctx = baritone.getEntityContext();
        _commandExecutor = new CommandExecutor(this);
        _taskRunner = new TaskRunner(this);
        _trackerManager = new TrackerManager(this);
        _userTaskChain = new UserTaskChain(_taskRunner);
        _mobDefenseChain = new MobDefenseChain(_taskRunner);
        new PlayerInteractionFixChain(_taskRunner);
        _mlgBucketChain = new MLGBucketFallChain(_taskRunner);
        new UnstuckChain(_taskRunner);
        new PreEquipItemChain(_taskRunner);
        new WorldSurvivalChain(_taskRunner);
        _foodChain = new FoodChain(_taskRunner);
        new PlayerDefenseChain(_taskRunner);

        _storageTracker = new ItemStorageTracker(this, _trackerManager, container -> this._containerSubTracker = container);
        _entityTracker = new EntityTracker(_trackerManager);
        _blockScanner = new BlockScanner(this);
        _chunkTracker = new SimpleChunkTracker(this);
        _miscBlockTracker = new MiscBlockTracker(this);
        _craftingRecipeTracker = new CraftingRecipeTracker(_trackerManager);
        _entityStuckTracker = new EntityStuckTracker(_trackerManager);
        _userBlockRangeTracker = new UserBlockRangeTracker(_trackerManager);
        _inputControls = new InputControls(this);
        _slotHandler = new SlotHandler(this);
        aiBridge = new AICommandBridge(_commandExecutor, this);
        _extraController = new PlayerExtraController(this);
        initializeBaritoneSettings();

        _botBehaviour = new BotBehaviour(this);
        initializeCommands();

        Settings.load(newSettings -> {
            this._settings = newSettings;
            List<Item> baritoneCanPlace = Arrays.stream(this._settings.getThrowawayItems(this,true)).toList();
            (getBaritoneSettings().acceptableThrowawayItems.get()).addAll(baritoneCanPlace);

            if ((!getUserTaskChain().isActive() || getUserTaskChain().isRunningIdleTask()) && getModSettings().shouldRunIdleCommandWhenNotActive()) {
                getUserTaskChain().signalNextTaskToBeIdleTask();
                getCommandExecutor().executeWithPrefix(getModSettings().getIdleCommand());
            }

            getExtraBaritoneSettings().avoidBlockBreak(_userBlockRangeTracker::isNearUserTrackedBlock);
            getExtraBaritoneSettings().avoidBlockPlace(_entityStuckTracker::isBlockedByEntity);
        });

        Playground.IDLE_TEST_INIT_FUNCTION(this);
    }

    public void serverTick() {
        _inputControls.onTickPre();
        _storageTracker.setDirty();
        _miscBlockTracker.tick();
        _trackerManager.tick();
        _blockScanner.tick();
        _taskRunner.tick();
        _inputControls.onTickPost();
        _baritone.serverTick();

        long now = System.nanoTime();
        if (now - lastHeartbeatTime > 60_000_000_000L) {
            aiBridge.sendHeartbeat();
            lastHeartbeatTime = now;
        }

        if (aiBridge.getEnabled()) {
            aiBridge.onTick();
        }
    }

    public void stop() {
        getUserTaskChain().cancel(this);
        if (_taskRunner.getCurrentTaskChain() != null) {
            _taskRunner.getCurrentTaskChain().stop();
        }
        getTaskRunner().disable();
        getBaritone().getPathingBehavior().forceCancel();
        getBaritone().getInputOverrideHandler().clearAllKeys();
    }

    private void initializeBaritoneSettings() {
        getExtraBaritoneSettings().canWalkOnEndPortal(false);
        getExtraBaritoneSettings().avoidBlockPlace(_entityStuckTracker::isBlockedByEntity);
        getExtraBaritoneSettings().avoidBlockBreak(_userBlockRangeTracker::isNearUserTrackedBlock);

        getBaritoneSettings().freeLook.set(false);
        getBaritoneSettings().overshootTraverse.set(true);
        getBaritoneSettings().allowOvershootDiagonalDescend.set(true);
        getBaritoneSettings().allowInventory.set(true);
        getBaritoneSettings().allowParkour.set(false);
        getBaritoneSettings().allowParkourAscend.set(false);
        getBaritoneSettings().allowParkourPlace.set(false);
        getBaritoneSettings().allowDiagonalDescend.set(false);
        getBaritoneSettings().allowDiagonalAscend.set(false);
        getBaritoneSettings().fadePath.set(true);
        getBaritoneSettings().mineScanDroppedItems.set(false);
        getBaritoneSettings().mineDropLoiterDurationMSThanksLouca.set(0L);
        getExtraBaritoneSettings().configurePlaceBucketButDontFall(true);
        getBaritoneSettings().randomLooking.set(0.0D);
        getBaritoneSettings().randomLooking113.set(0.0D);
        getBaritoneSettings().failureTimeoutMS.reset();
        getBaritoneSettings().planAheadFailureTimeoutMS.reset();
        getBaritoneSettings().movementTimeoutTicks.reset();
    }

    private void initializeCommands() {
        try {
            AltoClefCommands.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runUserTask(Task task, Runnable onFinish) {
        _userTaskChain.runTask(this, task, onFinish);
    }

    public void runUserTask(Task task) {
        runUserTask(task, () -> {});
    }

    public void cancelUserTask() {
        _userTaskChain.cancel(this);
    }


    public CommandExecutor getCommandExecutor() {
        return _commandExecutor;
    }

    public LivingEntity getEntity() {
        return _ctx.entity();
    }

    public ServerWorld getWorld() {
        return _ctx.world();
    }

    public IInteractionController getInteractionManager() {
        return _ctx.playerController();
    }

    public IBaritone getBaritone() {
        return _baritone;
    }

    public baritone.api.Settings getBaritoneSettings() {
        return _baritone.settings();
    }

    public AltoClefSettings getExtraBaritoneSettings() {
        return ((Baritone)_baritone).getExtraBaritoneSettings();
    }

    public TaskRunner getTaskRunner() {
        return _taskRunner;
    }

    public UserTaskChain getUserTaskChain() {
        return _userTaskChain;
    }

    public BotBehaviour getBehaviour() {
        return _botBehaviour;
    }

    public boolean isPaused() {
        return _paused;
    }

    public void setPaused(boolean pausing) {
        this._paused = pausing;
    }

    public Task getStoredTask() {
        return _storedTask;
    }

    public void setStoredTask(Task currentTask) {
        this._storedTask = currentTask;
    }

    public ItemStorageTracker getItemStorage() {
        return _storageTracker;
    }

    public EntityTracker getEntityTracker() {
        return _entityTracker;
    }

    public CraftingRecipeTracker getCraftingRecipeTracker() {
        return _craftingRecipeTracker;
    }

    public BlockScanner getBlockScanner() {
        return _blockScanner;
    }

    public SimpleChunkTracker getChunkTracker() {
        return _chunkTracker;
    }

    public MiscBlockTracker getMiscBlockTracker() {
        return _miscBlockTracker;
    }

    public Settings getModSettings() {
        return _settings;
    }

    public FoodChain getFoodChain() {
        return _foodChain;
    }

    public MobDefenseChain getMobDefenseChain() {
        return _mobDefenseChain;
    }

    public MLGBucketFallChain getMLGBucketChain() {
        return _mlgBucketChain;
    }

    public void log(String message) {
        Debug.logMessage(message);
    }

    public void logWarning(String message) {
        Debug.logWarning(message);
    }

    public static boolean inGame(){
        return true;
    }

    public LivingEntity getPlayer(){
        return _ctx.entity();
    }

    public InputControls getInputControls() {
        return _inputControls;
    }

    public SlotHandler getSlotHandler() {
        return _slotHandler;
    }

    public LivingEntityInventory getInventory(){
        return getBaritone().getEntityContext().inventory();
    }

    public PlayerExtraController getControllerExtras(){
        return _extraController;
    }

    public AICommandBridge getAiBridge() {
        return this.aiBridge;
    }

    public void setChatClefEnabled(boolean enabled) {
        getAiBridge().setEnabled(enabled);
        if (!enabled) {
            getUserTaskChain().cancel(this);
            getTaskRunner().disable();
        }
    }

    public void logCharacterMessage(String message, adris.altoclef.player2api.Character character, boolean isPublic) {
        int maxLength = 256;
        int start = 0;
        while (start < message.length()) {
            int end = Math.min(start + maxLength, message.length());
            String chunk = message.substring(start, end);
            if (chunk.length() > 0 && !chunk.isBlank()) {
                Debug.logCharacterMessage(chunk, character, isPublic);
            }
            start = end;
        }
    }

    public PlayerEntity getOwner() {
        return owner;
    }

    public void setOwner(PlayerEntity owner) {
        this.owner = owner;
    }
}