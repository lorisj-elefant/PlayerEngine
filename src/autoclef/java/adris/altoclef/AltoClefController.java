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

import adris.altoclef.chains.FoodChain;
import adris.altoclef.chains.MLGBucketFallChain;
import adris.altoclef.chains.MobDefenseChain;
import adris.altoclef.chains.PlayerDefenseChain;
import adris.altoclef.chains.PlayerInteractionFixChain;
import adris.altoclef.chains.PreEquipItemChain;
import adris.altoclef.chains.UnstuckChain;
import adris.altoclef.chains.UserTaskChain;
import adris.altoclef.chains.WorldSurvivalChain;
import adris.altoclef.commands.BlockScanner;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.control.InputControls;
import adris.altoclef.control.PlayerExtraController;
import adris.altoclef.control.SlotHandler;
import adris.altoclef.player2api.Character;
import adris.altoclef.player2api.EventQueueManager;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.trackers.CraftingRecipeTracker;
import adris.altoclef.trackers.EntityStuckTracker;
import adris.altoclef.trackers.EntityTracker;
import adris.altoclef.trackers.MiscBlockTracker;
import adris.altoclef.trackers.SimpleChunkTracker;
import adris.altoclef.trackers.TrackerManager;
import adris.altoclef.trackers.UserBlockRangeTracker;
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
    private final IBaritone baritone;
    private final IEntityContext ctx;

    private CommandExecutor commandExecutor;
    private TaskRunner taskRunner;
    private TrackerManager trackerManager;
    private BotBehaviour botBehaviour;
    private UserTaskChain userTaskChain;

    // Chains
    private FoodChain foodChain;
    private MobDefenseChain mobDefenseChain;
    private MLGBucketFallChain mlgBucketChain;

    // Trackers
    private ItemStorageTracker storageTracker;
    private ContainerSubTracker containerSubTracker;
    private EntityTracker entityTracker;
    private BlockScanner blockScanner;
    private SimpleChunkTracker chunkTracker;
    private MiscBlockTracker miscBlockTracker;
    private CraftingRecipeTracker craftingRecipeTracker;
    private EntityStuckTracker entityStuckTracker;
    private UserBlockRangeTracker userBlockRangeTracker;

    private InputControls inputControls;
    private SlotHandler slotHandler;
    private PlayerExtraController extraController;

    private Settings settings;

    private boolean paused = false;
    private Task storedTask;
    private static long lastHeartbeatTime = System.nanoTime();
    private Character character;
    public boolean isStopping = false;

    private PlayerEntity owner;

    public AltoClefController(IBaritone baritone, Character character) {
        this.baritone = baritone;
        this.character = character;
        EventQueueManager.createEventQueueData(this, character);

        this.ctx = baritone.getEntityContext();
        commandExecutor = new CommandExecutor(this);
        taskRunner = new TaskRunner(this);
        trackerManager = new TrackerManager(this);
        userTaskChain = new UserTaskChain(taskRunner);
        mobDefenseChain = new MobDefenseChain(taskRunner);
        new PlayerInteractionFixChain(taskRunner);
        mlgBucketChain = new MLGBucketFallChain(taskRunner);
        new UnstuckChain(taskRunner);
        new PreEquipItemChain(taskRunner);
        new WorldSurvivalChain(taskRunner);
        foodChain = new FoodChain(taskRunner);
        new PlayerDefenseChain(taskRunner);

        storageTracker = new ItemStorageTracker(this, trackerManager,
                container -> this.containerSubTracker = container);
        entityTracker = new EntityTracker(trackerManager);
        blockScanner = new BlockScanner(this);
        chunkTracker = new SimpleChunkTracker(this);
        miscBlockTracker = new MiscBlockTracker(this);
        craftingRecipeTracker = new CraftingRecipeTracker(trackerManager);
        entityStuckTracker = new EntityStuckTracker(trackerManager);
        userBlockRangeTracker = new UserBlockRangeTracker(trackerManager);
        inputControls = new InputControls(this);
        slotHandler = new SlotHandler(this);

        extraController = new PlayerExtraController(this);
        initializeBaritoneSettings();

        botBehaviour = new BotBehaviour(this);
        initializeCommands();

        Settings.load(newSettings -> {
            this.settings = newSettings;
            List<Item> baritoneCanPlace = Arrays.stream(this.settings.getThrowawayItems(this, true)).toList();
            (getBaritoneSettings().acceptableThrowawayItems.get()).addAll(baritoneCanPlace);

            if ((!getUserTaskChain().isActive() || getUserTaskChain().isRunningIdleTask())
                    && getModSettings().shouldRunIdleCommandWhenNotActive()) {
                getUserTaskChain().signalNextTaskToBeIdleTask();
                getCommandExecutor().executeWithPrefix(getModSettings().getIdleCommand());
            }

            getExtraBaritoneSettings().avoidBlockBreak(userBlockRangeTracker::isNearUserTrackedBlock);
            getExtraBaritoneSettings().avoidBlockPlace(entityStuckTracker::isBlockedByEntity);
        });

        Playground.IDLE_TEST_INIT_FUNCTION(this);
    }

    public void serverTick() {
        inputControls.onTickPre();
        storageTracker.setDirty();
        miscBlockTracker.tick();
        trackerManager.tick();
        blockScanner.tick();
        taskRunner.tick();
        inputControls.onTickPost();
        baritone.serverTick();
        EventQueueManager.injectOnTick();

        long now = System.nanoTime();
        if (now - lastHeartbeatTime > 60_000_000_000L) {
            EventQueueManager.sendHeartbeat();
            lastHeartbeatTime = now;
        }
    }

    public void stop() {
        getUserTaskChain().cancel(this);
        if (taskRunner.getCurrentTaskChain() != null) {
            taskRunner.getCurrentTaskChain().stop();
        }
        getTaskRunner().disable();
        getBaritone().getPathingBehavior().forceCancel();
        getBaritone().getInputOverrideHandler().clearAllKeys();
    }

    private void initializeBaritoneSettings() {
        getExtraBaritoneSettings().canWalkOnEndPortal(false);
        getExtraBaritoneSettings().avoidBlockPlace(entityStuckTracker::isBlockedByEntity);
        getExtraBaritoneSettings().avoidBlockBreak(userBlockRangeTracker::isNearUserTrackedBlock);

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
        userTaskChain.runTask(this, task, onFinish);
    }

    public void runUserTask(Task task) {
        runUserTask(task, () -> {
        });
    }

    public void cancelUserTask() {
        userTaskChain.cancel(this);
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public LivingEntity getEntity() {
        return ctx.entity();
    }

    public ServerWorld getWorld() {
        return ctx.world();
    }

    public IInteractionController getInteractionManager() {
        return ctx.playerController();
    }

    public IBaritone getBaritone() {
        return baritone;
    }

    public baritone.api.Settings getBaritoneSettings() {
        return baritone.settings();
    }

    public AltoClefSettings getExtraBaritoneSettings() {
        return ((Baritone) baritone).getExtraBaritoneSettings();
    }

    public TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public UserTaskChain getUserTaskChain() {
        return userTaskChain;
    }

    public BotBehaviour getBehaviour() {
        return botBehaviour;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean pausing) {
        this.paused = pausing;
    }

    public Task getStoredTask() {
        return storedTask;
    }

    public void setStoredTask(Task currentTask) {
        this.storedTask = currentTask;
    }

    public ItemStorageTracker getItemStorage() {
        return storageTracker;
    }

    public EntityTracker getEntityTracker() {
        return entityTracker;
    }

    public CraftingRecipeTracker getCraftingRecipeTracker() {
        return craftingRecipeTracker;
    }

    public BlockScanner getBlockScanner() {
        return blockScanner;
    }

    public SimpleChunkTracker getChunkTracker() {
        return chunkTracker;
    }

    public MiscBlockTracker getMiscBlockTracker() {
        return miscBlockTracker;
    }

    public Settings getModSettings() {
        return settings;
    }

    public FoodChain getFoodChain() {
        return foodChain;
    }

    public MobDefenseChain getMobDefenseChain() {
        return mobDefenseChain;
    }

    public MLGBucketFallChain getMLGBucketChain() {
        return mlgBucketChain;
    }

    public void log(String message) {
        Debug.logMessage(message);
    }

    public void logWarning(String message) {
        Debug.logWarning(message);
    }

    public static boolean inGame() {
        return true;
    }

    public LivingEntity getPlayer() {
        return ctx.entity();
    }

    public InputControls getInputControls() {
        return inputControls;
    }

    public SlotHandler getSlotHandler() {
        return slotHandler;
    }

    public LivingEntityInventory getInventory() {
        return getBaritone().getEntityContext().inventory();
    }

    public PlayerExtraController getControllerExtras() {
        return extraController;
    }

    public void setChatClefEnabled(boolean enabled) {
        if (enabled) {
            EventQueueManager.enable();
        } else {
            EventQueueManager.disable();
        }

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