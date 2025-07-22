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

package baritone.api.fakeplayer;

import com.mojang.logging.LogUtils;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OperatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LivingEntityInteractionManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected World world;
    protected final LivingEntity livingEntity;
    private GameMode gameMode;
    @Nullable
    private GameMode previousGameMode;
    private boolean mining;
    private int startMiningTime;
    private BlockPos miningPos;
    private int tickCounter;
    private boolean failedToMine;
    private BlockPos failedMiningPos;
    private int failedStartMiningTime;
    private int blockBreakingProgress;
    private boolean brokeBlock;

    public LivingEntityInteractionManager(LivingEntity livingEntity) {
        this.gameMode = GameMode.SURVIVAL;
        this.miningPos = BlockPos.ORIGIN;
        this.failedMiningPos = BlockPos.ORIGIN;
        this.blockBreakingProgress = -1;
        this.livingEntity = livingEntity;
        this.world = livingEntity.getWorld();
    }

//    public boolean changeGameMode(GameMode gameMode) {
//        if (gameMode == this.gameMode) {
//            return false;
//        } else {
//            this.setGameMode(gameMode, this.previousGameMode);
//            this.player.sendAbilitiesUpdate();
//            this.player.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(Action.UPDATE_GAME_MODE, this.player));
//            this.world.updateSleepingPlayers();
//            return true;
//        }
//    }
//
//    protected void setGameMode(GameMode gameMode, @Nullable GameMode previousGameMode) {
//        this.previousGameMode = previousGameMode;
//        this.gameMode = gameMode;
//        gameMode.setAbilities(this.player.getAbilities());
//    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    @Nullable
    public GameMode getPreviousGameMode() {
        return this.previousGameMode;
    }

    public boolean isSurvivalLike() {
        return this.gameMode.isSurvivalLike();
    }

    public boolean isCreative() {
        return this.gameMode.isCreative();
    }

    public void update() {
        ++this.tickCounter;
        if (this.failedToMine) {
            BlockState blockState = this.world.getBlockState(this.failedMiningPos);
            if (blockState.isAir()) {
                this.failedToMine = false;
            } else {
                float f = this.continueMining(blockState, this.failedMiningPos, this.failedStartMiningTime);
                if (f >= 1.0F) {
                    this.failedToMine = false;
                    this.tryBreakBlock(this.failedMiningPos);
                }
            }
        } else if (this.mining) {
            BlockState blockState = this.world.getBlockState(this.miningPos);
            if (blockState.isAir()) {
                this.world.setBlockBreakingInfo(this.livingEntity.getId(), this.miningPos, -1);
                this.blockBreakingProgress = -1;
                this.mining = false;
            } else {
                this.continueMining(blockState, this.miningPos, this.startMiningTime);
            }
        }

    }

    private float continueMining(BlockState state, BlockPos pos, int progress) {
        int i = this.tickCounter - progress;
        float f = calcBlockBreakingDelta(state, this.livingEntity, this.livingEntity.getWorld(), pos) * (float)(i + 1);
        int j = (int)(f * 10.0F);
        if (j != this.blockBreakingProgress) {
            this.world.setBlockBreakingInfo(this.livingEntity.getId(), pos, j);
            this.blockBreakingProgress = j;
        }

        return f;
    }

    private void method_41250(BlockPos pos, boolean bl, int i, String string) {
    }

    public void processBlockBreakingAction(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int i) {
        if (this.livingEntity.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos)) > ServerPlayNetworkHandler.MAX_INTERACTION_DISTANCE) {
            this.method_41250(pos, false, i, "too far");
        } else if (pos.getY() >= worldHeight) {
            //this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, this.world.getBlockState(pos)));
            this.method_41250(pos, false, i, "too high");
        } else {
            if (action == net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
//                if (!this.world.canPlayerModifyAt(this.player, pos)) {
//                    //this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, this.world.getBlockState(pos)));
//                    this.method_41250(pos, false, i, "may not interact");
//                    return;
//                }

                if (this.isCreative()) {
                    this.finishMining(pos, i, "creative destroy");
                    return;
                }

//                if (this.player.isBlockBreakingRestricted(this.world, pos, this.gameMode)) {
//                    //this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, this.world.getBlockState(pos)));
//                    this.method_41250(pos, false, i, "block action restricted");
//                    return;
//                }

                this.startMiningTime = this.tickCounter;
                float f = 1.0F;
                BlockState blockState = this.world.getBlockState(pos);
                if (!blockState.isAir()) {
                    //blockState.onBlockBreakStart(this.world, pos, this.player);
                    f = calcBlockBreakingDelta(blockState, this.livingEntity, this.livingEntity.getWorld(), pos);
                }

                if (!blockState.isAir() && f >= 1.0F) {
                    this.finishMining(pos, i, "insta mine");
                } else {
                    if (this.mining) {
                        //this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.miningPos, this.world.getBlockState(this.miningPos)));
                        this.method_41250(pos, false, i, "abort destroying since another started (client insta mine, server disagreed)");
                    }

                    this.mining = true;
                    this.miningPos = pos.toImmutable();
                    this.brokeBlock = true;
                    int j = (int)(f * 10.0F);
                    this.world.setBlockBreakingInfo(this.livingEntity.getId(), pos, j);
                    this.method_41250(pos, true, i, "actual start of destroying");
                    this.blockBreakingProgress = j;
                }
            } else if (action == net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
                if (pos.equals(this.miningPos)) {
                    int k = this.tickCounter - this.startMiningTime;
                    BlockState blockState = this.world.getBlockState(pos);
                    if (!blockState.isAir()) {
                        float g = calcBlockBreakingDelta(blockState, this.livingEntity, this.livingEntity.getWorld(), pos) * (float)(k + 1);
                        if (g >= 0.7F) {
                            this.mining = false;
                            this.world.setBlockBreakingInfo(this.livingEntity.getId(), pos, -1);
                            this.finishMining(pos, i, "destroyed");
                            return;
                        }

                        if (!this.failedToMine) {
                            this.mining = false;
                            this.failedToMine = true;
                            this.failedMiningPos = pos;
                            this.failedStartMiningTime = this.startMiningTime;
                        }
                    }
                }

                this.method_41250(pos, true, i, "stopped destroying");
            } else if (action == net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
                this.mining = false;
                if (!Objects.equals(this.miningPos, pos)) {
                    LOGGER.warn("Mismatch in destroy block pos: {} {}", this.miningPos, pos);
                    this.world.setBlockBreakingInfo(this.livingEntity.getId(), this.miningPos, -1);
                    this.method_41250(pos, true, i, "aborted mismatched destroying");
                }

                this.world.setBlockBreakingInfo(this.livingEntity.getId(), pos, -1);
                this.method_41250(pos, true, i, "aborted destroying");
            }

        }
    }

    public float calcBlockBreakingDelta(BlockState state, LivingEntity player, BlockView world, BlockPos pos) {
        float f = state.getHardness(world, pos);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            int i = canHarvest(state, player.getStackInHand(Hand.MAIN_HAND)) ? 30 : 100;
            return getBlockBreakingSpeed(player, state) / f / (float)i;
        }
    }

    public boolean canHarvest(BlockState state, ItemStack heldItem) {
        return !state.isToolRequired() || heldItem.isSuitableFor(state);
    }

    public float getBlockBreakingSpeed(LivingEntity entity, BlockState block) {
        float f = this.livingEntity.getStackInHand(Hand.MAIN_HAND).getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getEfficiency(entity);
            ItemStack itemStack = this.livingEntity.getStackInHand(Hand.MAIN_HAND);
            if (i > 0 && !itemStack.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }

        if (StatusEffectUtil.hasHaste(entity)) {
            f *= 1.0F + (float)(StatusEffectUtil.getHasteAmplifier(entity) + 1) * 0.2F;
        }

        if (entity.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float g;
            switch (entity.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0:
                    g = 0.3F;
                    break;
                case 1:
                    g = 0.09F;
                    break;
                case 2:
                    g = 0.0027F;
                    break;
                case 3:
                default:
                    g = 8.1E-4F;
            }

            f *= g;
        }

        if (entity.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(entity)) {
            f /= 5.0F;
        }

        if (!entity.isOnGround()) {
            f /= 5.0F;
        }

        return f;
    }

    public void finishMining(BlockPos pos, int i, String reason) {
        if (this.tryBreakBlock(pos)) {
            this.method_41250(pos, true, i, reason);
        } else {
            //this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, this.world.getBlockState(pos)));
            this.method_41250(pos, false, i, reason);
        }

    }

    public boolean tryBreakBlock(BlockPos pos) {
        BlockState blockState = this.world.getBlockState(pos);
        BlockEntity blockEntity = this.world.getBlockEntity(pos);
        Block block = blockState.getBlock();
        if (block instanceof OperatorBlock) {
            this.world.updateListeners(pos, blockState, blockState, 3);
            return false;
        } else {
            world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.create(livingEntity, blockState));
            //block.onBreak(this.world, pos, blockState, this.player);
            boolean bl = this.world.removeBlock(pos, false);
            if (bl) {
                block.onBroken(this.world, pos, blockState);
            }

            if (this.isCreative()) {
                return true;
            } else {
                ItemStack itemStack = this.livingEntity.getMainHandStack();
                ItemStack itemStack2 = itemStack.copy();
                boolean bl2 = true;//this.player.canHarvest(blockState);
                itemStack.getItem().postMine(itemStack, this.world, blockState, pos, this.livingEntity);
                if (bl && bl2) {
                    Block.dropStacks(blockState, world, pos, blockEntity, livingEntity, itemStack2);
                    //block.afterBreak(this.world, this.player, pos, blockState, blockEntity, itemStack2);
                }

                return true;
            }
        }
    }

    public ActionResult interactItem(LivingEntity player, World world, ItemStack stack, Hand hand) {
        if (this.gameMode == GameMode.SPECTATOR) {
            return ActionResult.PASS;
        } else {
            int i = stack.getCount();
            int j = stack.getDamage();
            try {
                TypedActionResult<ItemStack> typedActionResult = stack.use(world, null, hand);
                ItemStack itemStack = (ItemStack) typedActionResult.getValue();
                if (itemStack == stack && itemStack.getCount() == i && itemStack.getMaxUseTime() <= 0 && itemStack.getDamage() == j) {
                    return typedActionResult.getResult();
                } else if (typedActionResult.getResult() == ActionResult.FAIL && itemStack.getMaxUseTime() > 0 && !player.isUsingItem()) {
                    return typedActionResult.getResult();
                } else {
                    if (stack != itemStack) {
                        player.setStackInHand(hand, itemStack);
                    }

                    if (this.isCreative() && itemStack != ItemStack.EMPTY) {
                        itemStack.setCount(i);
                        if (itemStack.isDamageable() && itemStack.getDamage() != j) {
                            itemStack.setDamage(j);
                        }
                    }

                    if (itemStack.isEmpty()) {
                        player.setStackInHand(hand, ItemStack.EMPTY);
                    }

                    return typedActionResult.getResult();
                }
            }catch (Exception ignored){ return ActionResult.PASS; }
        }
    }

    public boolean shouldCancelInteraction(){
        return false;
    }

    public ActionResult interactBlock(LivingEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult) {
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (!blockState.getBlock().enabledIn(world.getEnabledFlags())) {
            return ActionResult.FAIL;
        } else {
            boolean bl = !player.getMainHandStack().isEmpty() || !player.getOffHandStack().isEmpty();
            boolean bl2 = shouldCancelInteraction() && bl;
            ItemStack itemStack = stack.copy();
            if (!bl2) {
                try {
                    ActionResult actionResult = blockState.onUse(world, null, hand, hitResult);
                    if (actionResult.isAccepted()) {
                        return actionResult;
                    }
                }catch (NullPointerException ignored){}
            }

            if (!stack.isEmpty()) {
                ItemUsageContext itemUsageContext = new ItemUsageContext(player.getWorld(), null, hand, player.getStackInHand(hand), hitResult){
                    @Override
                    public boolean shouldCancelInteraction() {
                        return shouldCancelInteraction();
                    }
                };
                ActionResult actionResult2;
                if (this.isCreative()) {
                    int i = stack.getCount();
                    actionResult2 = stack.useOnBlock(itemUsageContext);
                    stack.setCount(i);
                } else {
                    actionResult2 = stack.useOnBlock(itemUsageContext);
                }

                return actionResult2;
            } else {
                return ActionResult.PASS;
            }
        }
    }

    public void setWorld(ServerWorld world) {
        this.world = world;
    }

    public boolean isMining() {
        return mining;
    }

    public BlockPos getMiningPos() {
        return miningPos;
    }

    public int getBlockBreakingProgress() {
        return blockBreakingProgress;
    }

    public boolean hasBrokenBlock() {
        return this.brokeBlock;
    }
}
