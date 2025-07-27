// File: adris/altoclef/chains/FoodChain.java
package adris.altoclef.chains;

import adris.altoclef.AltoClefController;
import adris.altoclef.Settings;
import adris.altoclef.multiversion.FoodComponentWrapper;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasks.speedrun.DragonBreathTracker;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ConfigHelper;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.Optional;

public class FoodChain extends SingleTaskChain {

  private static FoodChainConfig config;
  private static boolean hasFood;

  static {
    ConfigHelper.loadConfig("configs/food_chain_settings.json", FoodChain.FoodChainConfig::new, FoodChain.FoodChainConfig.class, newConfig -> config = newConfig);
  }

  private final DragonBreathTracker dragonBreathTracker = new DragonBreathTracker();

  private boolean isTryingToEat = false;
  private boolean requestFillup = false;
  private boolean needsToCollectFood = false;
  private Optional<Item> cachedPerfectFood = Optional.empty();
  private boolean shouldStop = false;

  public FoodChain(TaskRunner runner) {
    super(runner);
  }

  @Override
  protected void onTaskFinish(AltoClefController controller) {
    // Nothing to do.
  }

  private void startEat(AltoClefController controller, Item food) {
    // On server, we don't need to check "isBlocking". If another chain wants to shield, it will have a higher priority.
    controller.getSlotHandler().forceEquipItem(new ItemTarget(food), true);
    controller.getBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);
    controller.getExtraBaritoneSettings().setInteractionPaused(true);
    isTryingToEat = true;
    requestFillup = true;
  }

  private void stopEat(AltoClefController controller) {
    if (isTryingToEat) {
      controller.getBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);
      controller.getExtraBaritoneSettings().setInteractionPaused(false);
      isTryingToEat = false;
      requestFillup = false;

      // Re-equip shield if we have one and it's not in offhand.
      if (controller.getItemStorage().hasItem(Items.SHIELD) && !controller.getItemStorage().hasItemInOffhand(controller, Items.SHIELD)) {
        controller.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
      }
    }
  }

  public boolean isTryingToEat() {
    return isTryingToEat;
  }

  @Override
  public float getPriority() {
    if (controller == null) return Float.NEGATIVE_INFINITY;

    if (WorldHelper.isInNetherPortal(controller)) {
      stopEat(controller);
      return Float.NEGATIVE_INFINITY;
    }
    if (controller.getMobDefenseChain().isShielding()){// || controller.getMobDefenseChain().isDoingAcrobatics()) {
      stopEat(controller);
      return Float.NEGATIVE_INFINITY;
    }

    dragonBreathTracker.updateBreath(controller);
    for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer(controller.getEntity())) {
      if (dragonBreathTracker.isTouchingDragonBreath(playerIn)) {
        stopEat(controller);
        return Float.NEGATIVE_INFINITY;
      }
    }

    if (!controller.getModSettings().isAutoEat() || controller.getEntity().isInLava() || shouldStop) {
      stopEat(controller);
      return Float.NEGATIVE_INFINITY;
    }

    if (!controller.getMLGBucketChain().doneMLG() || controller.getMLGBucketChain().isFalling(controller)) {
      stopEat(controller);
      return Float.NEGATIVE_INFINITY;
    }

    Pair<Integer, Optional<Item>> calculation = calculateFood(controller);
    int foodScore = calculation.getLeft();
    cachedPerfectFood = calculation.getRight();
    hasFood = (foodScore > 0);

    if (requestFillup && controller.getBaritone().getEntityContext().hungerManager().getFoodLevel() >= 20) {
      requestFillup = false;
    }
    if (!hasFood) {
      requestFillup = false;
    }

    if (hasFood && (needsToEat() || requestFillup) && cachedPerfectFood.isPresent()) {
      startEat(controller, cachedPerfectFood.get());
    } else {
      stopEat(controller);
    }

    Settings settings = controller.getModSettings();
    if (needsToCollectFood || foodScore < settings.getMinimumFoodAllowed()) {
      needsToCollectFood = foodScore < settings.getFoodUnitsToCollect();
      if (needsToCollectFood) {
        setTask(new CollectFoodTask(settings.getFoodUnitsToCollect()));
        return 55.0F;
      }
    }

    // No task, so no priority.
    setTask(null);
    return Float.NEGATIVE_INFINITY;
  }

  @Override
  public String getName() {
    return "Food chain";
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (controller != null) {
      stopEat(controller);
    }
  }

  public boolean needsToEat() {
    if (!hasFood || shouldStop) return false;

    LivingEntity player = controller.getEntity();
    int foodLevel = controller.getBaritone().getEntityContext().hungerManager().getFoodLevel();
    float health = player.getHealth();

    if (foodLevel >= 20) return false;
    if (health <= 10.0F) return true;
    if (player.isOnFire() || player.hasStatusEffect(StatusEffects.WITHER) || health < config.alwaysEatWhenWitherOrFireAndHealthBelow) return true;

    if (foodLevel <= config.alwaysEatWhenBelowHunger) return true;
    if (health < config.alwaysEatWhenBelowHealth) return true;

    if (foodLevel < config.alwaysEatWhenBelowHungerAndPerfectFit && cachedPerfectFood.isPresent()) {
      int need = 20 - foodLevel;
      Item best = cachedPerfectFood.get();
      int fills = Optional.ofNullable(ItemVer.getFoodComponent(best)).map(FoodComponentWrapper::getHunger).orElse(-1);
      return (fills > 0 && fills <= need);
    }

    return false;
  }

  private Pair<Integer, Optional<Item>> calculateFood(AltoClefController controller) {
    Item bestFood = null;
    double bestFoodScore = Double.NEGATIVE_INFINITY;
    int foodTotal = 0;
    LivingEntity player = controller.getEntity();

    float health = player.getHealth();
    float hunger = controller.getBaritone().getEntityContext().hungerManager().getFoodLevel();
    float saturation = controller.getBaritone().getEntityContext().hungerManager().getSaturationLevel();

    for (ItemStack stack : controller.getItemStorage().getItemStacksPlayerInventory(true)) {
      if (ItemVer.isFood(stack) && !stack.isOf(Items.SPIDER_EYE)) {

        FoodComponentWrapper food = ItemVer.getFoodComponent(stack.getItem());
        if (food == null) continue;

        // Simple score: prioritize saturation gain, penalize waste.
        float hungerIfEaten = Math.min(hunger + food.getHunger(), 20.0f);
        float saturationIfEaten = Math.min(hungerIfEaten, saturation + food.getSaturationModifier());

        float gainedSaturation = saturationIfEaten - saturation;
        float gainedHunger = hungerIfEaten - hunger;
        float hungerWasted = food.getHunger() - gainedHunger;

        float score = gainedSaturation * 2 - hungerWasted;
        if(stack.isOf(Items.ROTTEN_FLESH)) score -= 100; // Heavily penalize rotten flesh

        if (score > bestFoodScore) {
          bestFoodScore = score;
          bestFood = stack.getItem();
        }
        foodTotal += food.getHunger() * stack.getCount();
      }
    }
    return new Pair<>(foodTotal, Optional.ofNullable(bestFood));
  }

  public boolean hasFood() {
    return hasFood;
  }

  public void shouldStop(boolean shouldStopInput) {
    this.shouldStop = shouldStopInput;
  }

  static class FoodChainConfig {
    public int alwaysEatWhenWitherOrFireAndHealthBelow = 6;
    public int alwaysEatWhenBelowHunger = 10;
    public int alwaysEatWhenBelowHealth = 14;
    public int alwaysEatWhenBelowHungerAndPerfectFit = 20 - 5;
  }
}