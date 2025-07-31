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

package baritone.process;

import baritone.Baritone;
import baritone.api.entity.LivingEntityInventory;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import baritone.entity.CustomFishingBobberEntity;
import baritone.pathing.movement.MovementHelper;
import baritone.utils.BaritoneProcessHelper;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.StreamSupport;

public final class FishingProcess extends BaritoneProcessHelper implements IBaritoneProcess {

    private enum State {
        IDLE,
        WALKING_TO_WATER,
        PREPARING_TO_CAST,
        CASTING,
        WAITING_FOR_BITE,
        REELING_IN,
        WAITING_FOR_ITEMS,
        RECAST_DELAY;
    }

    private boolean active = false;
    private State currentState = State.IDLE;

    private BlockPos fishingSpot = null;
    private CustomFishingBobberEntity bobber = null;
    private int timeoutTicks = 0;

    public FishingProcess(Baritone baritone) {
        super(baritone);
    }

    public void fish() {
        this.active = true;
        this.currentState = State.IDLE;
        this.fishingSpot = null;
        this.bobber = null;
        this.timeoutTicks = 0;
        logDirect("Fishing process started.");
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void onLostControl() {
        active = false;
        bobber = null;
    }

    @Override
    public String displayName0() {
        return "Fishing: " + currentState.toString();
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (findFishingRodSlot() == -1) {
            //logDirect("No fishing rod in hotbar!");
            onLostControl();
            return null;
        }

        if (calcFailed) {
            //logDirect("Failed to path to water, stopping.");
            onLostControl();
            return null;
        }

        switch (currentState) {
            case IDLE:
                return handleIdleState();

            case WALKING_TO_WATER:
                return handleWalkingState();

            case PREPARING_TO_CAST:
                handlePreparingToCastState();
                break;

            case CASTING:
                handleCastingState();
                break;

            case WAITING_FOR_BITE:
                handleWaitingForBiteState();
                break;

            case REELING_IN:
                handleReelingInState();
                break;

            case WAITING_FOR_ITEMS:
            case RECAST_DELAY:
                handleDelayStates();
                break;
        }

        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    private PathingCommand handleIdleState() {
        Optional<BlockPos> spot = findWaterSpot();
        if (spot.isPresent()) {
            fishingSpot = spot.get().up();
            //logDirect("Found a fishing spot. Walking to " + fishingSpot);
            currentState = State.WALKING_TO_WATER;
            return new PathingCommand(new GoalBlock(fishingSpot), PathingCommandType.SET_GOAL_AND_PATH);
        } else {
            //logDirect("No suitable water spot found nearby.");
            onLostControl();
            return null;
        }
    }

    private PathingCommand handleWalkingState() {
        Goal goal = new GoalBlock(fishingSpot);
        if (goal.isInGoal(ctx.feetPos())) {
            //logDirect("Arrived at fishing spot.");
            currentState = State.PREPARING_TO_CAST;
            timeoutTicks = 0;
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }
        return new PathingCommand(goal, PathingCommandType.SET_GOAL_AND_PATH);
    }

    private void handlePreparingToCastState() {
        BlockPos waterBlock = findAdjacentWater(fishingSpot.down());
        if (waterBlock == null) {
            onLostControl();
            return;
        }

        Rotation rotation = RotationUtils.calcRotationFromVec3d(ctx.headPos(), Vec3d.ofCenter(waterBlock));
        baritone.getLookBehavior().updateTarget(rotation, true);

        if (ctx.entityRotations().isReallyCloseTo(rotation)) {
            currentState = State.CASTING;
        }
    }

    private void handleCastingState() {
        equipFishingRod();
        useFishingRod(ctx.world(), ctx.entity(), Hand.MAIN_HAND);
        //logDirect("Casting line...");
        currentState = State.WAITING_FOR_BITE;
        timeoutTicks = 0;
    }

    private void handleWaitingForBiteState() {
        if (this.bobber == null || !this.bobber.isAlive() || timeoutTicks<60) {
            this.bobber = findOurBobber();
            if (this.bobber == null || timeoutTicks<60) {
                timeoutTicks++;
                if (timeoutTicks > 70) {
                    //logDirect("Bobber not found, recasting.");
                    currentState = State.RECAST_DELAY;
                }
                return;
            }
        }


        if (this.bobber.getVelocity().y < -0.04) {
            //logDirect("Fish on the hook!");
            currentState = State.REELING_IN;
            return;
        }

        timeoutTicks++;
        if (timeoutTicks > 1200) {
            //logDirect("No bite for a long time, recasting.");
            currentState = State.RECAST_DELAY;
        }
    }

    private void handleReelingInState() {
        equipFishingRod();
        useFishingRod(ctx.world(), ctx.entity(), Hand.MAIN_HAND);
        //logDirect("Reeling in!");
        this.bobber = null;
        currentState = State.WAITING_FOR_ITEMS;
        timeoutTicks = 20;
    }

    private void handleDelayStates() {
        timeoutTicks--;
        if (timeoutTicks <= 0) {
            if (currentState == State.WAITING_FOR_ITEMS) {
                currentState = State.RECAST_DELAY;
                timeoutTicks = 10;
            } else {
                currentState = State.PREPARING_TO_CAST;
            }
        }
    }


    private Optional<BlockPos> findWaterSpot() {
        BlockPos center = ctx.feetPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-8, -3, -8), center.add(8, 3, 8))) {
            if (ctx.world().getBlockState(pos).isOf(Blocks.WATER)) {
                if (ctx.world().getBlockState(pos.up()).isAir() && ctx.world().getBlockState(pos.up(2)).isAir()) {
                    for (Direction dir : Direction.Type.HORIZONTAL) {
                        BlockPos standPos = pos.offset(dir);
                        if (MovementHelper.canWalkOn(ctx, standPos) && ctx.world().getBlockState(standPos.up()).isAir() && ctx.world().getBlockState(standPos.up(2)).isAir()) {
                            return Optional.of(standPos.toImmutable());
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private BlockPos findAdjacentWater(BlockPos position) {
        for (Direction dir : Direction.Type.HORIZONTAL) {
            if (ctx.world().getBlockState(position.offset(dir)).isOf(Blocks.WATER)) {
                return position.offset(dir);
            }
        }
        return null;
    }

    private int findFishingRodSlot() {
        LivingEntityInventory inv = ctx.inventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).getItem() instanceof FishingRodItem) {
                return i;
            }
        }
        return -1;
    }

    private void equipFishingRod() {
        int slot = findFishingRodSlot();
        if (slot != -1) {
            ctx.inventory().selectedSlot = slot;
        }
    }

    @Nullable
    private CustomFishingBobberEntity findOurBobber() {
        return (CustomFishingBobberEntity) StreamSupport.stream(ctx.world().iterateEntities().spliterator(), false)
                .filter(e -> e instanceof CustomFishingBobberEntity)
                .filter(e -> ((CustomFishingBobberEntity) e).getPlayerOwner() == ctx.entity())
                .findFirst()
                .orElse(null);
    }

    public TypedActionResult<ItemStack> useFishingRod(World world, LivingEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        CustomFishingBobberEntity bobber = findOurBobber();
        if (bobber != null) {
            if (!world.isClient) {
                int i = bobber.use(itemStack);
                itemStack.damage(i, user, (p) -> p.sendToolBreakStatus(hand));
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            user.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!world.isClient) {
                int i = EnchantmentHelper.getLure(itemStack);
                int j = EnchantmentHelper.getLuckOfTheSea(itemStack);
                world.spawnEntity(new CustomFishingBobberEntity(user, world, j, i));
            }

            user.emitGameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }
}