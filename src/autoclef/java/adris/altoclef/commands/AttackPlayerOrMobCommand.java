package adris.altoclef.commands;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import adris.altoclef.AltoClefController;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.EntityDeathEvent;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AttackPlayerOrMobCommand extends Command {

    public AttackPlayerOrMobCommand() throws CommandException {
        super("attack", "Attacks a specified player or mob. Example usages: @attack zombie 5 to attack and kill 5 zombies, @attack Player to attack a player with username=Player", new Arg<>(String.class, "name"), new Arg<>(Integer.class, "count", 1, 1));
    }

    @Override
    protected void call(AltoClefController mod, ArgParser parser) throws CommandException {
        String nameToAttack = parser.get(String.class);
        int countToAttack = parser.get(Integer.class);

        mod.runUserTask(new AttackAndGetDropsTask(nameToAttack, countToAttack), this::finish);
    }

    private static class AttackAndGetDropsTask extends ResourceTask {

        private final String toKill;
    
        private final Task killTask;

        private int mobsKilledCount;

        private int mobKillTargetCount;

        private TimerGame forceCollectTimer = new TimerGame(2);

        private Subscription<EntityDeathEvent> onMobDied;

        private Predicate<Entity> shouldAttackPredicate;

        private Set<Entity> trackedDeadEntities = new HashSet<>();

        private static ItemTarget[] drops = new ItemTarget[]{
            new ItemTarget("rotten_flesh", 9999),
            new ItemTarget("bone", 9999),
            new ItemTarget("string", 9999),
            new ItemTarget("spider_eye", 9999),
            new ItemTarget("gunpowder", 9999),
            new ItemTarget("slime_ball", 9999),
            new ItemTarget("ender_pearl", 9999),
            new ItemTarget("blaze_powder", 9999),
            new ItemTarget("ghast_tear", 9999),
            new ItemTarget("magma_cream", 9999),
            new ItemTarget("ender_eye", 9999),
            new ItemTarget("speckled_melon", 9999),
            new ItemTarget("gold_nugget", 9999),
            new ItemTarget("iron_nugget", 9999),
            new ItemTarget("porkchop", 9999),
            new ItemTarget("beef", 9999),
            new ItemTarget("chicken", 9999),
            new ItemTarget("mutton", 9999),
            new ItemTarget("rabbit", 9999),
    };

        public AttackAndGetDropsTask(String toKill, int killCount) {
            super(drops);
            this.toKill = toKill;
            mobKillTargetCount = killCount;

            shouldAttackPredicate = (entity) -> {
                // Done, don't attack any mobs just collect
                if (mobsKilledCount >= mobKillTargetCount) {
                    return false;
                }

                // Attack players possibly
                if (entity instanceof PlayerEntity) {
                    String playerName = entity.getName().getString();
                    if (playerName != null && playerName.equalsIgnoreCase(toKill)) {
                        return true;
                    }
                }

                // entity type match
                String name = entity.getType().getUntranslatedName();
                return name != null && name.equals(toKill);
            };

            // Kill any entity matches our name, or if it's a player their username.
            killTask = new KillEntitiesTask(shouldAttackPredicate);// new KillEntitiesTask(shouldKill, toKill);
        }

        @Override
        protected boolean shouldAvoidPickingUp(AltoClefController mod) {
            return false;
        }

        @Override
        protected void onResourceStart(AltoClefController mod) {
            forceCollectTimer.reset();
            // TODO: Also consider if the target player entity is NOT alive, in the event we're dealing with players

            onMobDied = EventBus.subscribe(EntityDeathEvent.class, evt -> {
                Entity diedEntity = evt.entity;

                // don't double count
                if (trackedDeadEntities.contains(diedEntity)) {
                    return;
                }

                if (shouldAttackPredicate.test(diedEntity)) {
                    markEntityDead(diedEntity);
                }
            });
        }

        private void markEntityDead(Entity entity) {
            // newly dead!
            trackedDeadEntities.add(entity);
            mobsKilledCount++;
        }

        @Override
        public boolean isFinished() {
            // We've killed enough of the mobs AND our timer has gone...
            return mobsKilledCount >= mobKillTargetCount && forceCollectTimer.elapsed();
        }

        @Override
        protected Task onResourceTick(AltoClefController mod) {

            // If our target is a player, consider dead players that match to add to our counter
            for (Entity entity : mod.getWorld().iterateEntities()) {
                if (trackedDeadEntities.contains(entity)) {
                    // don't double count
                    continue;
                }

                if (shouldAttackPredicate.test(entity)) {
                    if (!entity.isAlive()) {
                        markEntityDead(entity);
                    }
                }
            }
            // Clear remaining not attack predicate entitites
            trackedDeadEntities.removeIf(entity -> entity.isAlive());

            if (mobsKilledCount < mobKillTargetCount) {
                forceCollectTimer.reset();
            }

            return killTask;
        }

        @Override
        protected void onResourceStop(AltoClefController mod, Task interruptTask) {
            EventBus.unsubscribe(onMobDied);
            trackedDeadEntities.clear();
        }
    
        @Override
        protected boolean isEqualResource(ResourceTask other) {
            if (other instanceof AttackAndGetDropsTask task) {
                return task .toKill.equals(toKill) && task .mobKillTargetCount == mobKillTargetCount;
            }
            return false;
        }
    
        @Override
        protected String toDebugStringName() {
            return "Attacking and collect items from " + toKill + " x " + mobKillTargetCount;
        }
    }
    
}