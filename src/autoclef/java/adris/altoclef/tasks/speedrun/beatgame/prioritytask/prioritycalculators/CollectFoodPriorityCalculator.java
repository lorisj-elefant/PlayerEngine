package adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators;

import adris.altoclef.AltoClefController;
import adris.altoclef.multiversion.item.ItemVer;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.Slot;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;

public class CollectFoodPriorityCalculator extends ItemPriorityCalculator {
  private final AltoClefController mod;
  
  private final double foodUnits;
  
  public CollectFoodPriorityCalculator(AltoClefController mod, double foodUnits) {
    super(2147483647, 2147483647);
    this.mod = mod;
    this.foodUnits = foodUnits;
  }
  
  public double calculatePriority(int count) {
    double distance = getDistance(this.mod);
    double multiplier = 1.0D;
    double foodPotential = CollectFoodTask.calculateFoodPotential(this.mod);
    if (Double.isInfinite(distance) && foodPotential < this.foodUnits)
      return 0.1D; 
    Optional<BlockPos> hay = this.mod.getBlockScanner().getNearestBlock(new Block[] { Blocks.HAY_BLOCK });
    if ((hay.isPresent() && WorldHelper.inRangeXZ(hay.get(), this.mod.getPlayer().getBlockPos(), 75.0D)) || this.mod.getEntityTracker().itemDropped(new Item[] { Items.HAY_BLOCK }))
      multiplier = 50.0D; 
    if (foodPotential > this.foodUnits) {
      if (foodPotential > this.foodUnits + 20.0D)
        return Double.NEGATIVE_INFINITY; 
      if (distance > 10.0D && hay.isEmpty())
        return Double.NEGATIVE_INFINITY; 
      return 17.0D / distance * 30.0D / count / 2.0D * multiplier;
    } 
    if (foodPotential < 10.0D)
      multiplier = Math.max(11.0D / foodPotential, 22.0D); 
    return 33.0D / distance * 37.0D * multiplier;
  }
  
  private double getDistance(AltoClefController mod) {
    LivingEntity clientPlayerEntity = mod.getPlayer();
    for (Item item : CollectFoodTask.ITEMS_TO_PICK_UP) {
      double dist = pickupTaskOrNull(mod, item);
      if (dist != Double.NEGATIVE_INFINITY)
        return dist; 
    } 
    for (CollectFoodTask.CookableFoodTarget cookable : CollectFoodTask.COOKABLE_FOODS) {
      double dist = pickupTaskOrNull(mod, cookable.getRaw(), 20.0D);
      if (dist == Double.NEGATIVE_INFINITY)
        dist = pickupTaskOrNull(mod, cookable.getCooked(), 40.0D); 
      if (dist != Double.NEGATIVE_INFINITY)
        return dist; 
    } 
    double hayTaskBlock = pickupBlockTaskOrNull(mod, Blocks.HAY_BLOCK, Items.HAY_BLOCK, 300.0D);
    if (hayTaskBlock != Double.NEGATIVE_INFINITY)
      return hayTaskBlock; 
    for (CollectFoodTask.CropTarget target : CollectFoodTask.CROPS) {
      double t = pickupBlockTaskOrNull(mod, target.cropBlock, target.cropItem, blockPos -> {
            BlockState s = mod.getWorld().getBlockState(blockPos);
            Block b = s.getBlock();
            if (b instanceof CropBlock) {
              boolean isWheat = (!(b instanceof net.minecraft.block.PotatoesBlock) && !(b instanceof net.minecraft.block.CarrotsBlock) && !(b instanceof net.minecraft.block.BeetrootsBlock));
              if (isWheat) {
                if (!mod.getChunkTracker().isChunkLoaded(blockPos))
                  return false; 
                CropBlock crop = (CropBlock)b;
                return crop.isMature(s);
              } 
            } 
            return WorldHelper.canBreak(mod, blockPos);
          },96.0D);
      if (t != Double.NEGATIVE_INFINITY)
        return t; 
    } 
    double bestScore = 0.0D;
    Entity bestEntity = null;
    Predicate<Entity> notBaby = entity -> {
        if (entity instanceof LivingEntity) {
          LivingEntity livingEntity = (LivingEntity)entity;
          if (!livingEntity.isBaby());
        } 
        return false;
      };
    for (CollectFoodTask.CookableFoodTarget cookable : CollectFoodTask.COOKABLE_FOODS) {
      if (mod.getEntityTracker().entityFound(new Class[] { cookable.mobToKill })) {
        Optional<Entity> nearest = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), notBaby, new Class[] { cookable.mobToKill });
        if (!nearest.isEmpty()) {
          int hungerPerformance = cookable.getCookedUnits();
          double sqDistance = ((Entity)nearest.get()).squaredDistanceTo((Entity)mod.getPlayer());
          double score = 100.0D * hungerPerformance / sqDistance;
//          if (cookable.isFish())
//            score = 0.0D;
          if (score > bestScore) {
            bestScore = score;
            bestEntity = nearest.get();
          } 
        } 
      } 
    } 
    if (bestEntity != null)
      return bestEntity.distanceTo((Entity)clientPlayerEntity); 
    double berryPickup = pickupBlockTaskOrNull(mod, Blocks.SWEET_BERRY_BUSH, Items.SWEET_BERRIES, 96.0D);
    if (berryPickup != Double.NEGATIVE_INFINITY)
      return berryPickup; 
    return Double.POSITIVE_INFINITY;
  }
  
  private double pickupBlockTaskOrNull(AltoClefController mod, Block blockToCheck, Item itemToGrab, double maxRange) {
    return pickupBlockTaskOrNull(mod, blockToCheck, itemToGrab, toAccept -> true, maxRange);
  }
  
  private double pickupBlockTaskOrNull(AltoClefController mod, Block blockToCheck, Item itemToGrab, Predicate<BlockPos> accept, double maxRange) {
    Predicate<BlockPos> acceptPlus = blockPos -> !WorldHelper.canBreak(mod, blockPos) ? false : accept.test(blockPos);
    Optional<BlockPos> nearestBlock = mod.getBlockScanner().getNearestBlock(mod.getPlayer().getPos(), acceptPlus, new Block[] { blockToCheck });
    if (nearestBlock.isPresent() && !((BlockPos)nearestBlock.get()).isCenterWithinDistance((Position)mod.getPlayer().getPos(), maxRange))
      nearestBlock = Optional.empty(); 
    Optional<ItemEntity> nearestDrop = Optional.empty();
    if (mod.getEntityTracker().itemDropped(new Item[] { itemToGrab }))
      nearestDrop = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), new Item[] { itemToGrab }); 
    if (nearestDrop.isPresent())
      return ((ItemEntity)nearestDrop.get()).distanceTo((Entity)mod.getPlayer()); 
    if (nearestBlock.isPresent())
      return Math.sqrt(mod.getPlayer().squaredDistanceTo(WorldHelper.toVec3d(nearestBlock.get()))); 
    return Double.NEGATIVE_INFINITY;
  }
  
  private double pickupTaskOrNull(AltoClefController mod, Item itemToGrab) {
    return pickupTaskOrNull(mod, itemToGrab, Double.POSITIVE_INFINITY);
  }
  
  private double pickupTaskOrNull(AltoClefController mod, Item itemToGrab, double maxRange) {
    Optional<ItemEntity> nearestDrop = Optional.empty();
    if (mod.getEntityTracker().itemDropped(new Item[] { itemToGrab }))
      nearestDrop = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), new Item[] { itemToGrab }); 
    if (nearestDrop.isPresent() && (
      (ItemEntity)nearestDrop.get()).isInRange((Entity)mod.getPlayer(), maxRange)) {
      if (mod.getItemStorage().getSlotsThatCanFitInPlayerInventory(((ItemEntity)nearestDrop.get()).getStack(), false).isEmpty()) {
        Optional<Slot> slot = StorageHelper.getGarbageSlot(mod);
        if (slot.isPresent()) {
          ItemStack stack = StorageHelper.getItemStackInSlot(slot.get());
          if (ItemVer.isFood(stack.getItem())) {
            int inventoryCost = ItemVer.getFoodComponent(stack.getItem()).getHunger() * stack.getCount();
            double hunger = 0.0D;
            if (ItemVer.isFood(itemToGrab)) {
              hunger = ItemVer.getFoodComponent(itemToGrab).getHunger();
            } else if (itemToGrab.equals(Items.WHEAT)) {
              hunger += ItemVer.getFoodComponent(Items.BREAD).getHunger() / 3.0D;
            } else {
              mod.log("unknown food item: " + String.valueOf(itemToGrab));
            } 
            int groundCost = (int)(hunger * ((ItemEntity)nearestDrop.get()).getStack().getCount());
            if (inventoryCost > groundCost)
              return Double.NEGATIVE_INFINITY; 
          } 
        } 
      } 
      return ((ItemEntity)nearestDrop.get()).distanceTo((Entity)mod.getPlayer());
    } 
    return Double.NEGATIVE_INFINITY;
  }
}
