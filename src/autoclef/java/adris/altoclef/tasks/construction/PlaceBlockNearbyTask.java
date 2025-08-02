package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.BlockPlaceEvent;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import baritone.api.entity.IInteractionManagerProvider;
import baritone.api.utils.IEntityContext;
import baritone.api.utils.input.Input;
import baritone.pathing.movement.MovementHelper;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.function.Predicate;

public class PlaceBlockNearbyTask extends Task {
    private final Block[] toPlace;

    private final MovementProgressChecker progressChecker = new MovementProgressChecker();

    private final TimeoutWanderTask wander = new TimeoutWanderTask(5.0F);

    private final TimerGame randomlookTimer = new TimerGame(0.25D);

    private final Predicate<BlockPos> canPlaceHere;

    private BlockPos justPlaced;

    private BlockPos tryPlace;

    private Subscription<BlockPlaceEvent> onBlockPlaced;

    public PlaceBlockNearbyTask(Predicate<BlockPos> canPlaceHere, Block... toPlace) {
        this.toPlace = toPlace;
        this.canPlaceHere = canPlaceHere;
    }

    public PlaceBlockNearbyTask(Block... toPlace) {
        this(blockPos -> true, toPlace);
    }

    protected void onStart() {
        this.progressChecker.reset();
        controller.getBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);
        this.onBlockPlaced = EventBus.subscribe(BlockPlaceEvent.class, evt -> {
            if (ArrayUtils.contains(this.toPlace, evt.blockState.getBlock()))
                stopPlacing();
        });
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        if (mod.getBaritone().getPathingBehavior().isPathing())
            this.progressChecker.reset();
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot(controller);
//    if (cursorStack.isEmpty())
//      StorageHelper.closeScreen();
        BlockPos current = getCurrentlyLookingBlockPlace(mod);
        if (current != null && this.canPlaceHere.test(current)) {
            setDebugState("Placing since we can...");
            if (mod.getSlotHandler().forceEquipItem(ItemHelper.blocksToItems(this.toPlace)) &&
                    place(mod, current))
                return null;
        }
        if (this.wander.isActive() && !this.wander.isFinished()) {
            setDebugState("Wandering, will try to place again later.");
            this.progressChecker.reset();
            return (Task) this.wander;
        }
        if (!this.progressChecker.check(mod)) {
            Debug.logMessage("Failed placing, wandering and trying again.");
            LookHelper.randomOrientation(controller);
            if (this.tryPlace != null) {
                mod.getBlockScanner().requestBlockUnreachable(this.tryPlace);
                this.tryPlace = null;
            }
            return (Task) this.wander;
        }
        if (this.tryPlace == null || !WorldHelper.canReach(controller, this.tryPlace))
            this.tryPlace = locateClosePlacePos(mod);
        if (this.tryPlace != null) {
            setDebugState("Trying to place at " + String.valueOf(this.tryPlace));
            this.justPlaced = this.tryPlace;
            return (Task) new PlaceBlockTask(this.tryPlace, this.toPlace);
        }
        if (this.randomlookTimer.elapsed()) {
            this.randomlookTimer.reset();
            LookHelper.randomOrientation(controller);
        }
        setDebugState("Wandering until we randomly place or find a good place spot.");
        return (Task) new TimeoutWanderTask();
    }

    protected void onStop(Task interruptTask) {
        stopPlacing();
        EventBus.unsubscribe(this.onBlockPlaced);
    }

    protected boolean isEqual(Task other) {
        if (other instanceof adris.altoclef.tasks.construction.PlaceBlockNearbyTask) {
            adris.altoclef.tasks.construction.PlaceBlockNearbyTask task = (adris.altoclef.tasks.construction.PlaceBlockNearbyTask) other;
            return Arrays.equals(task.toPlace, this.toPlace);
        }
        return false;
    }

    protected String toDebugString() {
        return "Place " + Arrays.toString(this.toPlace) + " nearby";
    }

    public boolean isFinished() {
        return (this.justPlaced != null && ArrayUtils.contains(this.toPlace, controller.getWorld().getBlockState(this.justPlaced).getBlock()));
    }

    public BlockPos getPlaced() {
        return this.justPlaced;
    }

    private BlockPos getCurrentlyLookingBlockPlace(AltoClefController mod) {
        HitResult hit = (MinecraftClient.getInstance()).crosshairTarget;
        if (hit instanceof BlockHitResult) {
            BlockHitResult bhit = (BlockHitResult) hit;
            BlockPos bpos = bhit.getBlockPos();
            IEntityContext ctx = mod.getBaritone().getEntityContext();
            if (MovementHelper.canPlaceAgainst(ctx, bpos)) {
                BlockPos placePos = bhit.getBlockPos().add(bhit.getSide().getVector());
                if (WorldHelper.isInsidePlayer(controller, placePos))
                    return null;
                if (WorldHelper.canPlace(controller, placePos))
                    return placePos;
            }
        }
        return null;
    }

    private boolean blockEquipped() {
        return StorageHelper.isEquipped(controller, ItemHelper.blocksToItems(this.toPlace));
    }

    private boolean place(AltoClefController mod, BlockPos targetPlace) {
        if (!mod.getExtraBaritoneSettings().isInteractionPaused() && blockEquipped()) {
            mod.getInputControls().hold(Input.SNEAK);
            HitResult mouseOver = (MinecraftClient.getInstance()).crosshairTarget;
            if (mouseOver == null || mouseOver.getType() != HitResult.Type.BLOCK)
                return false;
            Hand hand = Hand.MAIN_HAND;
            if (((IInteractionManagerProvider) mod.getEntity()).getInteractionManager().interactBlock(mod.getPlayer(), mod.getWorld(), mod.getPlayer().getMainHandStack(), hand, (BlockHitResult) mouseOver) == ActionResult.SUCCESS && mod
                    .getPlayer().isSneaking()) {
                mod.getPlayer().swingHand(hand);
                this.justPlaced = targetPlace;
                Debug.logMessage("PRESSED");
                return true;
            }
            return true;
        }
        return false;
    }

    private void stopPlacing() {
        controller.getInputControls().release(Input.SNEAK);
        controller.getBaritone().getBuilderProcess().onLostControl();
    }

    private BlockPos locateClosePlacePos(AltoClefController mod) {
        int range = 7;
        BlockPos best = null;
        double smallestScore = Double.POSITIVE_INFINITY;
        BlockPos start = mod.getPlayer().getBlockPos().add(-range, -range, -range);
        BlockPos end = mod.getPlayer().getBlockPos().add(range, range, range);
        for (BlockPos blockPos : WorldHelper.scanRegion(start, end)) {
            boolean solid = WorldHelper.isSolidBlock(controller, blockPos);
            boolean inside = WorldHelper.isInsidePlayer(controller, blockPos);
            if (solid && !WorldHelper.canBreak(controller, blockPos))
                continue;
            if (!this.canPlaceHere.test(blockPos))
                continue;
            if (!WorldHelper.canReach(controller, blockPos) || !WorldHelper.canPlace(controller, blockPos))
                continue;
            boolean hasBelow = WorldHelper.isSolidBlock(controller, blockPos.down());
            double distSq = BlockPosVer.getSquaredDistance(blockPos, (Position) mod.getPlayer().getPos());
            double score = distSq + (solid ? 4 : 0) + (hasBelow ? 0 : 10) + (inside ? 3 : 0);
            if (score < smallestScore) {
                best = blockPos;
                smallestScore = score;
            }
        }
        return best;
    }
}
