package adris.altoclef.tasksystem;

import net.minecraft.entity.LivingEntity;

/**
 * Some tasks may mess up royally if we interrupt them while mid air.
 * For instance, if we're doing some parkour and a baritone task is stopped,
 * the player will fall to whatever is below them, perhaps their death.
 */
public interface ITaskRequiresGrounded extends ITaskCanForce {
    @Override
    default boolean shouldForce(Task interruptingCandidate) {
        if (interruptingCandidate instanceof ITaskOverridesGrounded)
            return false;

        LivingEntity player = ((Task) this).controller.getPlayer();
        return !(player.isOnGround() || player.isSwimming() || player.isTouchingWater() || player.isClimbing());
    }
}