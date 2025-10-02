package adris.altoclef.tasks.entity;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.AbstractDoToClosestObjectTask;
import adris.altoclef.tasks.resources.CollectMilkTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalRunAway;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

// Assumes you have the item in your inv already, and the entity is close
public class UseItemOnEntityTask extends Task {
    public static final Logger LOGGER = LogManager.getLogger();

    String itemName;
    String entityName;
    protected final MovementProgressChecker progress = new MovementProgressChecker();
    Item item;
    Optional<Task> specialCase = Optional.empty();
    boolean hasInteracted = false;

    public UseItemOnEntityTask(String itemName, String entityName) {
        this.itemName = itemName;
        this.entityName = entityName;
        if (itemName.equals("hand")) {
            return;
        }
        // will err if itemName not found
        item = ItemHelper.fromString(itemName).get();

        // if (itemName.equals("shear") && entityName.equals("sheep")) {
        // specialCase = Optional.of(new ShearSheepTask());
        // } else if (itemName.equals("bucket") && entityName.equals("cow")) {
        // specialCase = Optional.of(new CollectMilkTask(1));
        // }

    }

    private void equipItem() {
        AltoClefController mod = this.controller;
        if (itemName.equals("hand")) {
            mod.getSlotHandler().forceDeequip(stack -> !stack.isEmpty());
            return;
        }
        mod.getSlotHandler().forceEquipItem(item);
    }

    @Override
    protected boolean isEqual(Task var1) {
        // TODO Auto-generated method stub
        if (!(var1 instanceof UseItemOnEntityTask)) {
            return false;
        }
        UseItemOnEntityTask rhs = (UseItemOnEntityTask) var1;
        return rhs.itemName.equals(this.itemName) && rhs.entityName.equals(this.entityName);
    }

    @Override
    protected void onStart() {
        this.progress.reset();
    }

    protected Optional<Entity> getClosestToEntity(AltoClefController mod, Vec3 pos) {
        return EntityHelper.stringToEntityClass(entityName)
                .flatMap((entClass) -> mod.getEntityTracker().getClosestEntity(pos, entClass));
    }

    @Override
    protected Task onTick() {
        if (hasInteracted) {
            return null;
        }

        AltoClefController mod = this.controller;
        Optional<Entity> closeEntity = this.getClosestToEntity(mod, mod.getPlayer().position());

        if (closeEntity.isEmpty()) {
            mod.getMobDefenseChain().resetTargetEntity();
            mod.getMobDefenseChain().resetForceField();
            return null;
        } else {
            mod.getMobDefenseChain().setTargetEntity(closeEntity.get());
        }

        // will throw error if there is no entity closeby
        Entity entity = closeEntity.get();

        double playerReach = mod.getModSettings().getEntityReachRange();
        // EntityHitResult result = LookHelper.raycast(mod.getPlayer(), entity,
        // playerReach);
        double sqDist = entity.distanceToSqr(mod.getPlayer());

        // double maintainDistance = playerReach - 0.5;
        // boolean tooClose = sqDist < maintainDistance * maintainDistance;
        // if (tooClose && !mod.getBaritone().getCustomGoalProcess().isActive()) {
        // mod.getBaritone().getCustomGoalProcess()
        // .setGoalAndPath(new GoalRunAway(maintainDistance, entity.blockPosition()));
        // }
        mod.getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(entity.getOnPos()));
        LookHelper.lookAt(mod, entity.getEyePosition());
        equipItem();
        if (sqDist <= playerReach * playerReach) {
            mod.copiedServerPlayer.getPlayerCopy().interactOn(entity, InteractionHand.MAIN_HAND);
            mod.copiedServerPlayer.updateOriginalItem(mod); // update item
            hasInteracted = true;
        }

        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClefController mod = this.controller;
        mod.getMobDefenseChain().setTargetEntity(null);
        mod.getMobDefenseChain().resetForceField();
    }

    @Override
    protected String toDebugString() {
        return String.format("UseItemOnEntity(%s on %s)", itemName, entityName);
    }

    @Override
    public boolean isFinished() {
        // TODO Auto-generated method stub
        return hasInteracted;
    }
}