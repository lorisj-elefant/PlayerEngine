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

package baritone.utils.player;

import baritone.api.fakeplayer.FakeServerPlayerEntity;
import baritone.api.fakeplayer.LivingEntityInteractionManager;
import baritone.api.utils.IPlayerController;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;


/**
 * Implementation of {@link IPlayerController} that chains to the primary player controller's methods
 *
 * @author Brady
 * @since 12/14/2018
 */
public class ServerPlayerController implements IPlayerController {
    private final ServerPlayerEntity player;
    private int sequence;

    public ServerPlayerController(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public boolean hasBrokenBlock() {
        return getInteractionManager().hasBrokenBlock();
    }

    @Override
    public boolean onPlayerDamageBlock(BlockPos pos, Direction side) {
        LivingEntityInteractionManager interactionManager = getInteractionManager();
        if (interactionManager.isMining()) {
            int progress = interactionManager.getBlockBreakingProgress();
            if (progress >= 10) {
                getInteractionManager().processBlockBreakingAction(interactionManager.getMiningPos(), PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, side, this.player.getWorld().getTopY(), sequence++);
            }
            return true;
        }
        return false;
    }

    @Override
    public void resetBlockRemoving() {
        LivingEntityInteractionManager interactionManager = getInteractionManager();
        if (interactionManager.isMining()) {
            getInteractionManager().processBlockBreakingAction(interactionManager.getMiningPos(), PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, Direction.UP, this.player.getWorld().getTopY(), sequence++);
        }
    }

    @Override
    public GameMode getGameType() {
        return GameMode.SURVIVAL;
    }

    @Override
    public ActionResult processRightClickBlock(PlayerEntity player, World world, Hand hand, BlockHitResult result) {
        return getInteractionManager().interactBlock(this.player, this.player.getWorld(), this.player.getStackInHand(hand), hand, result);
    }

    @Override
    public ActionResult processRightClick(PlayerEntity player, World world, Hand hand) {
        return getInteractionManager().interactItem(this.player, this.player.getWorld(), this.player.getStackInHand(hand), hand);
    }

    @Override
    public boolean clickBlock(BlockPos loc, Direction face) {
        BlockState state = this.player.getWorld().getBlockState(loc);
        if (state.isAir()) return false;

        getInteractionManager().processBlockBreakingAction(loc, PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, face, this.player.getWorld().getTopY(), sequence++);
        // Success = starting the mining process or insta-mining
        return (getInteractionManager()).isMining() || this.player.getWorld().isAir(loc);
    }

    public LivingEntityInteractionManager getInteractionManager(){
        if(player instanceof FakeServerPlayerEntity fakeServerPlayer)
            return fakeServerPlayer.manager;
        return null;
    }

    @Override
    public void setHittingBlock(boolean hittingBlock) {
        // NO-OP
    }

    @Override
    public double getBlockReachDistance() {
        return ReachEntityAttributes.getReachDistance(this.player, this.getGameType().isCreative() ? 5.0 : 4.5);
    }
}
