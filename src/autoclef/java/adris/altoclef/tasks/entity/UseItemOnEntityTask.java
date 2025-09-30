package adris.altoclef.tasks.entity;

import java.util.Optional;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.AbstractDoToClosestObjectTask;
import adris.altoclef.tasks.resources.CollectMilkTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.GoalRunAway;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

// Assumes you have the item in your inv already, and the entity is close
public class UseItemOnEntityTask extends Task {
    String itemName;
    String entityName;
    AltoClefController mod;
    protected final MovementProgressChecker progress = new MovementProgressChecker();
    Item item;
    Optional<Task> specialCase = Optional.empty();

    public UseItemOnEntityTask(String itemName, String entityName) {
        this.itemName = itemName;
        this.entityName = entityName;
        this.mod = this.controller;

        // will err if itemName not found
        item = ItemHelper.fromString(itemName).get();

        if (itemName.equals("shear") && entityName.equals("sheep")) {
            specialCase = Optional.of(new ShearSheepTask());
        } else if (itemName.equals("bucket") && entityName.equals("cow")) {
            specialCase = Optional.of(new CollectMilkTask(1));
        }

    }

    private boolean equipItemTask() {
        return mod.getSlotHandler().forceEquipItem(item) == true;
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
        // TODO Auto-generated method stub
        return mod.getEntityTracker().getClosestFromPredicate(pos, (p) -> p.getName().getString().equals(entityName));
    }

    @Override
    protected Task onTick() {
        Optional<Entity> closeEntity = this.getClosestToEntity(mod, mod.getPlayer().position());

        if (closeEntity.isEmpty()) {
            mod.getMobDefenseChain().resetTargetEntity();
            mod.getMobDefenseChain().resetForceField();
        } else {
            mod.getMobDefenseChain().setTargetEntity(closeEntity.get());
        }

        // will throw error if there is no entity closeby
        Entity entity = closeEntity.get();

        double playerReach = mod.getModSettings().getEntityReachRange();
        EntityHitResult result = LookHelper.raycast(mod.getPlayer(), entity, playerReach);
        double sqDist = entity.distanceToSqr(mod.getPlayer());

        double maintainDistance = playerReach - 1.0;
        boolean tooClose = sqDist < maintainDistance * maintainDistance;
        if (tooClose && !mod.getBaritone().getCustomGoalProcess().isActive()) {
            mod.getBaritone().getCustomGoalProcess()
                    .setGoalAndPath(new GoalRunAway(maintainDistance, entity.blockPosition()));
        }
        LookHelper.lookAt(mod, entity.getEyePosition());
        if (sqDist <= playerReach * playerReach) {
            mod.copiedServerPlayer.getPlayerCopy().interactOn(entity, InteractionHand.MAIN_HAND);
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
}