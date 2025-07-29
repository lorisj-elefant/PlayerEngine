package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.tasks.container.SmeltInSmokerTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.SmeltTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class CollectMeatTask extends Task {

  public static final CollectFoodTask.CookableFoodTarget[] COOKABLE_MEATS = {
          new CollectFoodTask.CookableFoodTarget("beef", CowEntity.class),
          new CollectFoodTask.CookableFoodTarget("porkchop", PigEntity.class),
          new CollectFoodTask.CookableFoodTarget("chicken", ChickenEntity.class),
          new CollectFoodTask.CookableFoodTarget("mutton", SheepEntity.class),
          new CollectFoodTask.CookableFoodTarget("rabbit", RabbitEntity.class)
  };

  private final double _unitsNeeded;
  private final TimerGame _checkNewOptionsTimer = new TimerGame(10);
  private Task _currentResourceTask = null;

  public CollectMeatTask(double unitsNeeded) {
    this._unitsNeeded = unitsNeeded;
  }

  @Override
  protected void onStart() {
    controller.getBehaviour().push();
    // Protect all cookable meats, both raw and cooked.
    for (CollectFoodTask.CookableFoodTarget meat : COOKABLE_MEATS) {
      controller.getBehaviour().addProtectedItems(meat.getRaw(), meat.getCooked());
    }
  }

  @Override
  protected Task onTick() {
    CollectFoodTask.blackListChickenJockeys(controller);

    double potentialFood = calculateFoodPotential(controller);

    // If we have enough potential food from raw meat, cook it.
    if (potentialFood >= _unitsNeeded) {
      SmeltTarget toSmelt = getBestSmeltTarget(controller);
      if (toSmelt != null) {
        setDebugState("Cooking meat");
        return new SmeltInSmokerTask(toSmelt);
      }
    }

    // Re-evaluate our strategy periodically.
    if (_checkNewOptionsTimer.elapsed()) {
      _checkNewOptionsTimer.reset();
      _currentResourceTask = null;
    }

    // If we have a cached task, run it.
    if (_currentResourceTask != null && _currentResourceTask.isActive() && !_currentResourceTask.isFinished() && !_currentResourceTask.thisOrChildAreTimedOut()) {
      return _currentResourceTask;
    }

    // Strategy: Find the best meat source.
    // 1. Pick up any dropped raw/cooked meat.
    for (CollectFoodTask.CookableFoodTarget cookable : COOKABLE_MEATS) {
      if (controller.getEntityTracker().itemDropped(cookable.getRaw(), cookable.getCooked())) {
        setDebugState("Picking up dropped meat");
        _currentResourceTask = new PickupDroppedItemTask(new ItemTarget(cookable.getRaw(), cookable.getCooked()), true);
        return _currentResourceTask;
      }
    }

    // 2. Kill animals for meat (pick the best one).
    Entity bestEntityToKill = getBestAnimalToKill(controller);
    if (bestEntityToKill != null) {
      setDebugState("Hunting " + bestEntityToKill.getType().getName().getString());
      Item rawFood = Arrays.stream(COOKABLE_MEATS)
              .filter(c -> c.mobToKill == bestEntityToKill.getClass())
              .findFirst()
              .get()
              .getRaw();
      _currentResourceTask = new KillAndLootTask(bestEntityToKill.getClass(), new ItemTarget(rawFood, 1));
      return _currentResourceTask;
    }

    // 3. If nothing is found, wander.
    setDebugState("Searching for animals...");
    return new TimeoutWanderTask();
  }

  @Override
  protected void onStop(Task interruptTask) {
    controller.getBehaviour().pop();
  }

  @Override
  public boolean isFinished() {
    // The goal is met if we have enough COOKED meat.
    // We don't care about potential food from raw meat here.
    double currentFoodScore = 0;
    for (CollectFoodTask.CookableFoodTarget meat : COOKABLE_MEATS) {
      currentFoodScore += controller.getItemStorage().getItemCount(meat.getCooked()) * meat.getCookedUnits();
    }
    return currentFoodScore >= _unitsNeeded;
  }

  @Override
  protected boolean isEqual(Task other) {
    if (other instanceof CollectMeatTask task) {
      return task._unitsNeeded == this._unitsNeeded;
    }
    return false;
  }

  @Override
  protected String toDebugString() {
    return "Collecting " + _unitsNeeded + " units of meat.";
  }

  private SmeltTarget getBestSmeltTarget(AltoClefController controller) {
    for (CollectFoodTask.CookableFoodTarget cookable : COOKABLE_MEATS) {
      int rawCount = controller.getItemStorage().getItemCount(cookable.getRaw());
      if (rawCount > 0) {
        return new SmeltTarget(new ItemTarget(cookable.getCooked(), rawCount), new ItemTarget(cookable.getRaw(), rawCount));
      }
    }
    return null;
  }

  private Entity getBestAnimalToKill(AltoClefController controller) {
    double bestScore = -1;
    Entity bestEntity = null;
    Predicate<Entity> notBaby = entity -> entity instanceof LivingEntity && !((LivingEntity) entity).isBaby();

    for (CollectFoodTask.CookableFoodTarget cookable : COOKABLE_MEATS) {
      if (!controller.getEntityTracker().entityFound(cookable.mobToKill)) continue;

      Optional<Entity> nearest = controller.getEntityTracker().getClosestEntity(controller.getEntity().getPos(), notBaby, cookable.mobToKill);

      if (nearest.isPresent()) {
        double distanceSq = nearest.get().getPos().squaredDistanceTo(controller.getEntity().getPos());
        if (distanceSq == 0) continue;
        double score = (double) cookable.getCookedUnits() / distanceSq;
        if (score > bestScore) {
          bestScore = score;
          bestEntity = nearest.get();
        }
      }
    }
    return bestEntity;
  }

  private static double calculateFoodPotential(AltoClefController controller) {
    double potentialFood = 0;
    for (ItemStack stack : controller.getItemStorage().getItemStacksPlayerInventory(true)) {
      potentialFood += getFoodPotential(stack);
    }
    return potentialFood;
  }

  public static double getFoodPotential(ItemStack food) {
    if (food == null || food.isEmpty()) return 0;
    int count = food.getCount();

    for (CollectFoodTask.CookableFoodTarget cookable : COOKABLE_MEATS) {
      if (food.getItem() == cookable.getRaw()) {
        return (double) count * cookable.getCookedUnits();
      }
      if(food.getItem() == cookable.getCooked()){
        return (double) count * cookable.getCookedUnits();
      }
    }
    return 0;
  }
}