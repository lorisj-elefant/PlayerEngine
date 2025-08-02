package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasks.container.SmeltInSmokerTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.SmeltTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static adris.altoclef.tasks.resources.CollectMeatTask.getFoodPotential;

public class CollectFoodTask extends Task {

  public static final CookableFoodTarget[] COOKABLE_FOODS = {
          new CookableFoodTarget("beef", CowEntity.class),
          new CookableFoodTarget("porkchop", PigEntity.class),
          new CookableFoodTarget("chicken", ChickenEntity.class),
          new CookableFoodTarget("mutton", SheepEntity.class),
          new CookableFoodTarget("rabbit", RabbitEntity.class)
  };
  public static final Item[] ITEMS_TO_PICK_UP = {
          Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE, Items.GOLDEN_CARROT, Items.BREAD, Items.BAKED_POTATO
  };
  public static final CropTarget[] CROPS = {
          new CropTarget(Items.WHEAT, Blocks.WHEAT),
          new CropTarget(Items.CARROT, Blocks.CARROTS)
  };

  private final double unitsNeeded;
  private final TimerGame checkNewOptionsTimer = new TimerGame(10);
  private Task currentResourceTask = null;

  public CollectFoodTask(double unitsNeeded) {
    this .unitsNeeded = unitsNeeded;
  }

  @Override
  protected void onStart() {
    controller.getBehaviour().push();
    controller.getBehaviour().addProtectedItems(ITEMS_TO_PICK_UP);
    controller.getBehaviour().addProtectedItems(Items.HAY_BLOCK, Items.SWEET_BERRIES);
  }

  @Override
  protected Task onTick() {
    // Blacklist chicken jockeys and pillager hay bales
    blackListChickenJockeys(controller);
    blacklistPillagerHayBales(controller);

    // First priority: if we have raw food, cook it.
    SmeltTarget toSmelt = getBestSmeltTarget(controller);
    if (toSmelt != null) {
      setDebugState("Smelting food");
      return new SmeltInSmokerTask(toSmelt);
    }

    // Re-evaluate our strategy every so often.
    if (checkNewOptionsTimer.elapsed()) {
      checkNewOptionsTimer.reset();
      currentResourceTask = null;
    }

    // If we have a cached task, run it.
    if (currentResourceTask != null && currentResourceTask.isActive() && !currentResourceTask.isFinished() && !currentResourceTask.thisOrChildAreTimedOut()) {
      return currentResourceTask;
    }

    // If we have enough materials to meet our food goal, process them.
    double potentialFood = StorageHelper.calculateInventoryFoodScore(controller);
    if (potentialFood >= unitsNeeded) {
      if (controller.getItemStorage().getItemCount(Items.HAY_BLOCK) >= 1) {
        setDebugState("Crafting wheat from hay");
        currentResourceTask = new CraftInInventoryTask(new RecipeTarget(Items.WHEAT, 9, CraftingRecipe.newShapedRecipe("wheat", new ItemTarget[]{new ItemTarget(Items.HAY_BLOCK, 1)}, 9)));
        return currentResourceTask;
      }
      if (controller.getItemStorage().getItemCount(Items.WHEAT) >= 3) {
        setDebugState("Crafting bread");
        currentResourceTask = new CraftInTableTask(new RecipeTarget(Items.BREAD, 1, CraftingRecipe.newShapedRecipe("bread", new ItemTarget[]{new ItemTarget(Items.WHEAT, 3)}, 1)));
        return currentResourceTask;
      }
    }

    // Strategy: Find the best food source based on a priority list.
    // 1. Pick up high-value dropped food
    for (Item item : ITEMS_TO_PICK_UP) {
      if (controller.getEntityTracker().itemDropped(item)) {
        setDebugState("Picking up high-value food: " + item.getName().getString());
        currentResourceTask = new PickupDroppedItemTask(new ItemTarget(item), true);
        return currentResourceTask;
      }
    }

    // 2. Pick up any dropped raw/cooked meat
    for (CookableFoodTarget cookable : COOKABLE_FOODS) {
      if (controller.getEntityTracker().itemDropped(cookable.getRaw(), cookable.getCooked())) {
        setDebugState("Picking up dropped meat");
        currentResourceTask = new PickupDroppedItemTask(new ItemTarget(cookable.getRaw(), cookable.getCooked()), true);
        return currentResourceTask;
      }
    }

    // 3. Collect Hay Bales if nearby
    if (controller.getBlockScanner().anyFound(Blocks.HAY_BLOCK)) {
      setDebugState("Collecting hay bales");
      currentResourceTask = new MineAndCollectTask(new ItemTarget(Items.HAY_BLOCK, 9999), new Block[]{Blocks.HAY_BLOCK}, MiningRequirement.HAND);
      return currentResourceTask;
    }

    // 4. Harvest mature crops
    for (CropTarget crop : CROPS) {
      if (controller.getBlockScanner().anyFound(pos -> isCropMature(controller, pos, crop.cropBlock), crop.cropBlock)) {
        setDebugState("Harvesting " + crop.cropItem.getName().getString());
        currentResourceTask = new CollectCropTask(new ItemTarget(crop.cropItem, 9999), new Block[]{crop.cropBlock}, crop.cropItem);
        return currentResourceTask;
      }
    }

    // 5. Kill animals for meat (pick the best one based on distance and food value)
    Entity bestEntityToKill = getBestAnimalToKill(controller);
    if (bestEntityToKill != null) {
      setDebugState("Killing " + bestEntityToKill.getType().getName().getString());
      Item rawFood = Arrays.stream(COOKABLE_FOODS).filter(c -> c.mobToKill == bestEntityToKill.getClass()).findFirst().get().getRaw();
      currentResourceTask = new KillAndLootTask(bestEntityToKill.getClass(), new ItemTarget(rawFood, 1));
      return currentResourceTask;
    }

    // 6. As a last resort, gather sweet berries
    if(controller.getBlockScanner().anyFound(Blocks.SWEET_BERRY_BUSH)) {
      setDebugState("Collecting sweet berries");
      currentResourceTask = new MineAndCollectTask(new ItemTarget(Items.SWEET_BERRIES, 9999), new Block[]{Blocks.SWEET_BERRY_BUSH}, MiningRequirement.HAND);
      return currentResourceTask;
    }

    // 7. If nothing is found, wander around.
    setDebugState("Searching for food source...");
    return new TimeoutWanderTask();
  }

  @Override
  protected void onStop(Task interruptTask) {
    controller.getBehaviour().pop();
  }

  @Override
  public boolean isFinished() {
    return StorageHelper.calculateInventoryFoodScore(controller) >= unitsNeeded;
  }

  @Override
  protected boolean isEqual(Task other) {
    if (other instanceof CollectFoodTask task) {
      return task .unitsNeeded == this .unitsNeeded;
    }
    return false;
  }

  @Override
  protected String toDebugString() {
    return "Collecting " + unitsNeeded + " food units.";
  }

  private SmeltTarget getBestSmeltTarget(AltoClefController controller) {
    for (CookableFoodTarget cookable : COOKABLE_FOODS) {
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
    for (CookableFoodTarget cookable : COOKABLE_FOODS) {
      if (!controller.getEntityTracker().entityFound(cookable.mobToKill)) continue;

      Optional<Entity> nearest = controller.getEntityTracker().getClosestEntity(controller.getEntity().getPos(), notBaby, cookable.mobToKill);
      if (nearest.isPresent()) {
        double distanceSq = nearest.get().getPos().squaredDistanceTo(controller.getEntity().getPos());
        if (distanceSq == 0) continue; // Avoid division by zero
        double score = (double) cookable.getCookedUnits() / distanceSq;
        if (score > bestScore) {
          bestScore = score;
          bestEntity = nearest.get();
        }
      }
    }
    return bestEntity;
  }

  public static void blackListChickenJockeys(AltoClefController controller) {
    for (ChickenEntity chicken : controller.getEntityTracker().getTrackedEntities(ChickenEntity.class)) {
      if (chicken.hasPassengers()) {
        controller.getEntityTracker().requestEntityUnreachable(chicken);
      }
    }
  }

  private static void blacklistPillagerHayBales(AltoClefController controller) {
    for (BlockPos pos : controller.getBlockScanner().getKnownLocations(Blocks.HAY_BLOCK)) {
      if (controller.getWorld().getBlockState(pos.up()).isOf(Blocks.CARVED_PUMPKIN)) {
        controller.getBlockScanner().requestBlockUnreachable(pos, 0);
      }
    }
  }

  private static boolean isCropMature(AltoClefController controller, BlockPos pos, Block block) {
    if (!controller.getChunkTracker().isChunkLoaded(pos)) return false;
    if (controller.getWorld().getBlockState(pos).getBlock() instanceof CropBlock crop) {
      return crop.isMature(controller.getWorld().getBlockState(pos));
    }
    return true; // Not a crop block, so it's "mature" by default (e.g. pumpkin)
  }

  public static double calculateFoodPotential(AltoClefController mod) {
    double potentialFood = 0;
    for (ItemStack food : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
      potentialFood += getFoodPotential(food);
    }
    int potentialBread = (int) (mod.getItemStorage().getItemCount(Items.WHEAT) / 3) + mod.getItemStorage().getItemCount(Items.HAY_BLOCK) * 3;
    potentialFood += Objects.requireNonNull(ItemVer.getFoodComponent( Items.BREAD)).getHunger() * potentialBread;
    return potentialFood;
  }

  public static class CookableFoodTarget {
    public final String rawFood;
    public final String cookedFood;
    public final Class<? extends Entity> mobToKill;

    public CookableFoodTarget(String rawFood, Class<? extends Entity> mobToKill) {
      this(rawFood, "cooked_" + rawFood, mobToKill);
    }

    public CookableFoodTarget(String rawFood, String cookedFood, Class<? extends Entity> mobToKill) {
      this.rawFood = rawFood;
      this.cookedFood = cookedFood;
      this.mobToKill = mobToKill;
    }

    public Item getRaw() { return TaskCatalogue.getItemMatches(rawFood)[0]; }
    public Item getCooked() { return TaskCatalogue.getItemMatches(cookedFood)[0]; }
    public int getCookedUnits() {
      return ItemVer.getFoodComponent(getCooked()).getHunger();
    }
  }

  public static class CropTarget {
    public final Item cropItem;
    public final Block cropBlock;

    public CropTarget(Item cropItem, Block cropBlock) {
      this.cropItem = cropItem;
      this.cropBlock = cropBlock;
    }
  }
}