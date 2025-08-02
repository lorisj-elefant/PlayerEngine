package adris.altoclef.tasks;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.SafeRandomShimmyTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.baritone.GoalAnd;
import adris.altoclef.util.baritone.GoalBlockSide;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalTwoBlocks;
import baritone.api.process.ICustomGoalProcess;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.Objects;
import java.util.Optional;

public class InteractWithBlockTask extends Task {
    private final MovementProgressChecker moveChecker = new MovementProgressChecker();

    private final MovementProgressChecker stuckCheck = new MovementProgressChecker();

    private final ItemTarget toUse;

    private final Direction direction;

    private final BlockPos target;

    private final boolean walkInto;

    private final Vec3i interactOffset;

    private final Input interactInput;

    private final boolean shiftClick;

    private final TimerGame clickTimer = new TimerGame(5.0D);

    private final TimeoutWanderTask wanderTask = new TimeoutWanderTask(5.0F, true);

    Block[] annoyingBlocks = new Block[]{
            Blocks.VINE, Blocks.NETHER_SPROUTS, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WEEPING_VINES_PLANT, Blocks.LADDER, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM,
            Blocks.SMALL_DRIPLEAF, Blocks.TALL_GRASS, Blocks.GRASS, Blocks.SWEET_BERRY_BUSH};

    private Task unstuckTask = null;

    private ClickResponse cachedClickStatus = ClickResponse.CANT_REACH;

    private int waitingForClickTicks = 0;

    public InteractWithBlockTask(ItemTarget toUse, Direction direction, BlockPos target, Input interactInput, boolean walkInto, Vec3i interactOffset, boolean shiftClick) {
        this.toUse = toUse;
        this.direction = direction;
        this.target = target;
        this.interactInput = interactInput;
        this.walkInto = walkInto;
        this.interactOffset = interactOffset;
        this.shiftClick = shiftClick;
    }

    public InteractWithBlockTask(ItemTarget toUse, Direction direction, BlockPos target, Input interactInput, boolean walkInto, boolean shiftClick) {
        this(toUse, direction, target, interactInput, walkInto, Vec3i.ZERO, shiftClick);
    }

    public InteractWithBlockTask(ItemTarget toUse, Direction direction, BlockPos target, boolean walkInto) {
        this(toUse, direction, target, Input.CLICK_RIGHT, walkInto, true);
    }

    public InteractWithBlockTask(ItemTarget toUse, BlockPos target, boolean walkInto, Vec3i interactOffset) {
        this(toUse, null, target, Input.CLICK_RIGHT, walkInto, interactOffset, true);
    }

    public InteractWithBlockTask(ItemTarget toUse, BlockPos target, boolean walkInto) {
        this(toUse, target, walkInto, Vec3i.ZERO);
    }

    public InteractWithBlockTask(ItemTarget toUse, BlockPos target) {
        this(toUse, target, false);
    }

    public InteractWithBlockTask(Item toUse, Direction direction, BlockPos target, Input interactInput, boolean walkInto, Vec3i interactOffset, boolean shiftClick) {
        this(new ItemTarget(toUse, 1), direction, target, interactInput, walkInto, interactOffset, shiftClick);
    }

    public InteractWithBlockTask(Item toUse, Direction direction, BlockPos target, Input interactInput, boolean walkInto, boolean shiftClick) {
        this(new ItemTarget(toUse, 1), direction, target, interactInput, walkInto, shiftClick);
    }

    public InteractWithBlockTask(Item toUse, Direction direction, BlockPos target, boolean walkInto) {
        this(new ItemTarget(toUse, 1), direction, target, walkInto);
    }

    public InteractWithBlockTask(Item toUse, Direction direction, BlockPos target) {
        this(new ItemTarget(toUse, 1), direction, target, Input.CLICK_RIGHT, false, false);
    }

    public InteractWithBlockTask(Item toUse, BlockPos target, boolean walkInto, Vec3i interactOffset) {
        this(new ItemTarget(toUse, 1), target, walkInto, interactOffset);
    }

    public InteractWithBlockTask(Item toUse, Direction direction, BlockPos target, Vec3i interactOffset) {
        this(new ItemTarget(toUse, 1), direction, target, Input.CLICK_RIGHT, false, interactOffset, false);
    }

    public InteractWithBlockTask(Item toUse, BlockPos target, Vec3i interactOffset) {
        this(new ItemTarget(toUse, 1), null, target, Input.CLICK_RIGHT, false, interactOffset, false);
    }

    public InteractWithBlockTask(Item toUse, BlockPos target, boolean walkInto) {
        this(new ItemTarget(toUse, 1), target, walkInto);
    }

    public InteractWithBlockTask(Item toUse, BlockPos target) {
        this(new ItemTarget(toUse, 1), target);
    }

    public InteractWithBlockTask(BlockPos target, boolean shiftClick) {
        this(ItemTarget.EMPTY, null, target, Input.CLICK_RIGHT, false, shiftClick);
    }

    public InteractWithBlockTask(BlockPos target) {
        this(ItemTarget.EMPTY, null, target, Input.CLICK_RIGHT, false, false);
    }

    private static BlockPos[] generateSides(BlockPos pos) {
        return new BlockPos[]{pos
                .add(1, 0, 0), pos
                .add(-1, 0, 0), pos
                .add(0, 0, 1), pos
                .add(0, 0, -1), pos
                .add(1, 0, -1), pos
                .add(1, 0, 1), pos
                .add(-1, 0, -1), pos
                .add(-1, 0, 1)};
    }

    private static Goal createGoalForInteract(BlockPos target, int reachDistance, Direction interactSide, Vec3i interactOffset, boolean walkInto) {
        boolean sideMatters = (interactSide != null);
        if (sideMatters) {
            Vec3i offs = interactSide.getVector();
            if (offs.getY() == -1)
                offs = offs.down();
            target = target.add(offs);
        }
        if (walkInto)
            return (Goal) new GoalTwoBlocks(target);
        if (sideMatters) {
            GoalBlockSide goalBlockSide = new GoalBlockSide(target, interactSide, 1.0D);
            return (Goal) new GoalAnd(new Goal[]{(Goal) goalBlockSide, (Goal) new GoalNear(target.add(interactOffset), reachDistance)});
        }
        return (Goal) new GoalTwoBlocks(target.up());
    }

    private boolean isAnnoying(AltoClefController mod, BlockPos pos) {
        if (this.annoyingBlocks != null) {
            Block[] arrayOfBlock = this.annoyingBlocks;
            int i = arrayOfBlock.length;
            byte b = 0;
            if (b < i) {
                Block AnnoyingBlocks = arrayOfBlock[b];
                return (mod.getWorld().getBlockState(pos).getBlock() == AnnoyingBlocks || mod
                        .getWorld().getBlockState(pos).getBlock() instanceof net.minecraft.block.DoorBlock || mod
                        .getWorld().getBlockState(pos).getBlock() instanceof net.minecraft.block.FenceBlock || mod
                        .getWorld().getBlockState(pos).getBlock() instanceof net.minecraft.block.FenceGateBlock || mod
                        .getWorld().getBlockState(pos).getBlock() instanceof net.minecraft.block.FlowerBlock);
            }
        }
        return false;
    }

    private BlockPos stuckInBlock(AltoClefController mod) {
        BlockPos p = mod.getPlayer().getBlockPos();
        if (isAnnoying(mod, p))
            return p;
        if (isAnnoying(mod, p.up()))
            return p.up();
        BlockPos[] toCheck = generateSides(p);
        for (BlockPos check : toCheck) {
            if (isAnnoying(mod, check))
                return check;
        }
        BlockPos[] toCheckHigh = generateSides(p.up());
        for (BlockPos check : toCheckHigh) {
            if (isAnnoying(mod, check))
                return check;
        }
        return null;
    }

    private Task getFenceUnstuckTask() {
        return (Task) new SafeRandomShimmyTask();
    }

    protected void onStart() {
        controller.getBaritone().getPathingBehavior().forceCancel();
        this.moveChecker.reset();
        this.stuckCheck.reset();
        this.wanderTask.resetWander();
        this.clickTimer.reset();
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        if (mod.getBaritone().getPathingBehavior().isPathing())
            this.moveChecker.reset();
        if (WorldHelper.isInNetherPortal(controller)) {
            if (!mod.getBaritone().getPathingBehavior().isPathing()) {
                setDebugState("Getting out from nether portal");
                mod.getInputControls().hold(Input.SNEAK);
                mod.getInputControls().hold(Input.MOVE_FORWARD);
                return null;
            }
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.MOVE_BACK);
            mod.getInputControls().release(Input.MOVE_FORWARD);
        } else if (mod.getBaritone().getPathingBehavior().isPathing()) {
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.MOVE_BACK);
            mod.getInputControls().release(Input.MOVE_FORWARD);
        }
        if (this.unstuckTask != null && this.unstuckTask.isActive() && !this.unstuckTask.isFinished() && stuckInBlock(mod) != null) {
            setDebugState("Getting unstuck from block.");
            this.stuckCheck.reset();
            mod.getBaritone().getCustomGoalProcess().onLostControl();
            mod.getBaritone().getExploreProcess().onLostControl();
            return this.unstuckTask;
        }
        if (!this.moveChecker.check(mod) || !this.stuckCheck.check(mod)) {
            BlockPos blockStuck = stuckInBlock(mod);
            if (blockStuck != null) {
                this.unstuckTask = getFenceUnstuckTask();
                return this.unstuckTask;
            }
            this.stuckCheck.reset();
        }
        this.cachedClickStatus = ClickResponse.CANT_REACH;
        if (!ItemTarget.nullOrEmpty(this.toUse) && !StorageHelper.itemTargetsMet(mod, new ItemTarget[]{this.toUse})) {
            this.moveChecker.reset();
            this.clickTimer.reset();
            return (Task) TaskCatalogue.getItemTask(this.toUse);
        }
        if (this.wanderTask.isActive() && !this.wanderTask.isFinished()) {
            this.moveChecker.reset();
            this.clickTimer.reset();
            return (Task) this.wanderTask;
        }
        if (!this.moveChecker.check(mod)) {
            Debug.logMessage("Failed, blacklisting and wandering.");
            mod.getBlockScanner().requestBlockUnreachable(this.target);
            return (Task) this.wanderTask;
        }
        int reachDistance = 0;
        Goal moveGoal = createGoalForInteract(this.target, reachDistance, this.direction, this.interactOffset, this.walkInto);
        ICustomGoalProcess customGoalProcess = mod.getBaritone().getCustomGoalProcess();
        this.cachedClickStatus = rightClick(mod);
        switch (Objects.requireNonNull(this.cachedClickStatus).ordinal()) {
            case 0:
                setDebugState("Getting to our goal");
                if (!customGoalProcess.isActive())
                    customGoalProcess.setGoalAndPath(moveGoal);
                this.clickTimer.reset();
                break;
            case 1:
                setDebugState("Waiting for click");
                if (customGoalProcess.isActive())
                    customGoalProcess.onLostControl();
                this.clickTimer.reset();
                this.waitingForClickTicks++;
                if (this.waitingForClickTicks % 25 == 0 && this.shiftClick) {
                    mod.getInputControls().hold(Input.SNEAK);
                    mod.log("trying to press shift");
                }
                if (this.waitingForClickTicks > 200) {
                    mod.log("trying to wander");
                    this.waitingForClickTicks = 0;
                    return (Task) this.wanderTask;
                }
                break;
            case 2:
                setDebugState("Clicking.");
                if (customGoalProcess.isActive())
                    customGoalProcess.onLostControl();
                if (this.clickTimer.elapsed()) {
                    this.clickTimer.reset();
                    return (Task) this.wanderTask;
                }
                break;
        }
        return null;
    }

    protected void onStop(Task interruptTask) {
        AltoClefController mod = controller;
        mod.getBaritone().getPathingBehavior().forceCancel();
        mod.getInputControls().release(Input.SNEAK);
    }

    public boolean isFinished() {
        return false;
    }

    protected boolean isEqual(Task other) {
        if (other instanceof adris.altoclef.tasks.InteractWithBlockTask) {
            adris.altoclef.tasks.InteractWithBlockTask task = (adris.altoclef.tasks.InteractWithBlockTask) other;
            if (((task.direction == null) ? true : false) != ((this.direction == null) ? true : false))
                return false;
            if (task.direction != null && !task.direction.equals(this.direction))
                return false;
            if (((task.toUse == null) ? true : false) != ((this.toUse == null) ? true : false))
                return false;
            if (task.toUse != null && !task.toUse.equals(this.toUse))
                return false;
            if (!task.target.equals(this.target))
                return false;
            if (!task.interactInput.equals(this.interactInput))
                return false;
            return (task.walkInto == this.walkInto);
        }
        return false;
    }

    protected String toDebugString() {
        return "Interact using " + String.valueOf(this.toUse) + " at " + String.valueOf(this.target) + " dir " + String.valueOf(this.direction);
    }

    public ClickResponse getClickStatus() {
        return this.cachedClickStatus;
    }

    private ClickResponse rightClick(AltoClefController mod) {
        if (mod.getExtraBaritoneSettings().isInteractionPaused() || mod.getFoodChain().needsToEat() || mod
                .getPlayer().isBlocking())
            return ClickResponse.WAIT_FOR_CLICK;
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot(controller);
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            if (moveTo.isPresent()) {
                mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                return ClickResponse.WAIT_FOR_CLICK;
            }
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                return ClickResponse.WAIT_FOR_CLICK;
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            if (garbage.isPresent()) {
                mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                return ClickResponse.WAIT_FOR_CLICK;
            }
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            return ClickResponse.WAIT_FOR_CLICK;
        }
        Optional<Rotation> reachable = getCurrentReach();
        if (reachable.isPresent()) {
            if (LookHelper.isLookingAt(mod, this.target)) {
                if (this.toUse != null) {
                    mod.getSlotHandler().forceEquipItem(this.toUse, false);
                } else {
                    mod.getSlotHandler().forceDeequipRightClickableItem();
                }
                mod.getInputControls().tryPress(this.interactInput);
                if (mod.getInputControls().isHeldDown(this.interactInput)) {
                    if (this.shiftClick)
                        mod.getInputControls().hold(Input.SNEAK);
                    return ClickResponse.CLICK_ATTEMPTED;
                }
            } else {
                LookHelper.lookAt(controller, reachable.get());
            }
            return ClickResponse.WAIT_FOR_CLICK;
        }
        if (this.shiftClick)
            mod.getInputControls().release(Input.SNEAK);
        return ClickResponse.CANT_REACH;
    }

    public Optional<Rotation> getCurrentReach() {
        return LookHelper.getReach(controller, this.target, this.direction);
    }

    public enum ClickResponse {
        CANT_REACH,
        WAIT_FOR_CLICK,
        CLICK_ATTEMPTED
    }
}
