package adris.altoclef.tasks.speedrun.beatgame;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commands.BlockScanner;
import adris.altoclef.multiversion.blockpos.BlockPosVer;

import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.GetRidOfExtraWaterBucketTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.SafeNetherPortalTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasks.construction.PlaceObsidianBucketTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasks.container.LootContainerTask;
import adris.altoclef.tasks.container.SmeltInFurnaceTask;
import adris.altoclef.tasks.container.SmeltInSmokerTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.misc.PlaceBedAndSetSpawnTask;
import adris.altoclef.tasks.misc.SleepThroughNightTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasks.movement.GetWithinRangeOfBlockTask;
import adris.altoclef.tasks.movement.GoToStrongholdPortalTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.SearchChunkForBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.resources.CollectBlazeRodsTask;
import adris.altoclef.tasks.resources.CollectBucketLiquidTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasks.resources.CollectMeatTask;
import adris.altoclef.tasks.resources.GetBuildingMaterialsTask;
import adris.altoclef.tasks.resources.KillEndermanTask;
import adris.altoclef.tasks.resources.MineAndCollectTask;
import adris.altoclef.tasks.resources.TradeWithPiglinsTask;
import adris.altoclef.tasks.speedrun.BeatMinecraftConfig;
import adris.altoclef.tasks.speedrun.DragonBreathTracker;
import adris.altoclef.tasks.speedrun.KillEnderDragonTask;
import adris.altoclef.tasks.speedrun.KillEnderDragonWithBedsTask;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.CollectFoodPriorityCalculator;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.DistanceItemPriorityCalculator;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.DistancePriorityCalculator;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.ItemPriorityCalculator;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.StaticItemPriorityCalculator;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks.ActionPriorityTask;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks.CraftItemPriorityTask;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks.MineBlockPriorityTask;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks.PriorityTask;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks.RecraftableItemPriorityTask;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks.ResourcePriorityTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.EntityTracker;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.Pair;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.SmeltTarget;
import adris.altoclef.util.helpers.ConfigHelper;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.entity.LivingEntityInventory;
import baritone.api.utils.input.Input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.state.property.Property;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Difficulty;
import org.apache.commons.lang3.ArrayUtils;

public class BeatMinecraftTask extends Task {
    private static final Item[] COLLECT_EYE_ARMOR = new Item[]{Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS};

    private static final Item[] COLLECT_IRON_ARMOR = ItemHelper.IRON_ARMORS;

    private static final Item[] COLLECT_EYE_ARMOR_END = ItemHelper.DIAMOND_ARMORS;

    private static final ItemTarget[] COLLECT_EYE_GEAR_MIN = combine(new ItemTarget[][]{ItemTarget.of(new Item[]{Items.DIAMOND_SWORD}), ItemTarget.of(new Item[]{Items.DIAMOND_PICKAXE})});

    private static final int END_PORTAL_FRAME_COUNT = 12;

    private static final double END_PORTAL_BED_SPAWN_RANGE = 8.0D;

    private static final Predicate<ItemStack> noCurseOfBinding;

    private static BeatMinecraftConfig config;

    private static GoToStrongholdPortalTask locateStrongholdTask;

    static {
        noCurseOfBinding = (stack -> !EnchantmentHelper.hasBindingCurse(stack));
    }

    private static boolean openingEndPortal = false;

    private final UselessItems uselessItems;

    static {
        ConfigHelper.loadConfig("configs/beat_minecraft.json", BeatMinecraftConfig::new, BeatMinecraftConfig.class, newConfig -> config = newConfig);
    }

    private final HashMap<Item, Integer> cachedEndItemDrops = new HashMap<>();

    private final TimerGame cachedEndItemNothingWaitTime = new TimerGame(10.0D);

    private final Task buildMaterialsTask;

    private final PlaceBedAndSetSpawnTask setBedSpawnTask = new PlaceBedAndSetSpawnTask();

    private final Task getOneBedTask = (Task) TaskCatalogue.getItemTask("bed", 1);

    private final Task sleepThroughNightTask = (Task) new SleepThroughNightTask();

    private final Task killDragonBedStratsTask = (Task) new KillEnderDragonWithBedsTask();

    private final DragonBreathTracker dragonBreathTracker = new DragonBreathTracker();

    private final TimerGame timer1 = new TimerGame(5.0D);

    private final TimerGame timer2 = new TimerGame(35.0D);

    private final TimerGame timer3 = new TimerGame(60.0D);

    private final List<PriorityTask> gatherResources = new LinkedList<>();

    private final TimerGame changedTaskTimer = new TimerGame(3.0D);

    private final TimerGame forcedTaskTimer = new TimerGame(10.0D);

    private final List<BlockPos> blacklistedChests = new LinkedList<>();

    private final TimerGame waterPlacedTimer = new TimerGame(1.5D);

    private final TimerGame fortressTimer = new TimerGame(20.0D);

    private final AltoClefController mod;

    private PriorityTask lastGather = null;

    private Task lastTask = null;

    private boolean pickupFurnace = false;

    private boolean pickupSmoker = false;

    private boolean pickupCrafting = false;

    private Task rePickupTask = null;

    private Task searchTask = null;

    private boolean hasRods = false;

    private boolean gotToBiome = false;

    private GetRidOfExtraWaterBucketTask getRidOfExtraWaterBucketTask = null;

    private int repeated = 0;

    private boolean gettingPearls = false;

    private SafeNetherPortalTask safeNetherPortalTask;

    private boolean escaped = false;

    private boolean gotToFortress = false;

    private GetWithinRangeOfBlockTask cachedFortressTask = null;

    private boolean resetFortressTask = false;

    private BlockPos prevPos = null;

    private Task goToNetherTask = (Task) new DefaultGoToDimensionTask(Dimension.NETHER);

    private boolean dragonIsDead = false;

    private BlockPos endPortalCenterLocation;

    private boolean ranStrongholdLocator;

    private boolean endPortalOpened;

    private BlockPos bedSpawnLocation;

    private int cachedFilledPortalFrames = 0;

    private boolean enterindEndPortal = false;

    private Task lootTask;

    private boolean collectingEyes;

    private boolean escapingDragonsBreath = false;

    private Task getBedTask;

    private List<TaskChange> taskChanges = new ArrayList<>();

    private PriorityTask prevLastGather = null;

    private BlockPos biomePos = null;

    public BeatMinecraftTask(AltoClefController mod) {
        this.mod = mod;
        locateStrongholdTask = new GoToStrongholdPortalTask(config.targetEyes);
        this.buildMaterialsTask = (Task) new GetBuildingMaterialsTask(config.buildMaterialCount);
        this.uselessItems = new UselessItems(config);
        if (mod.getWorld().getDifficulty() != Difficulty.EASY) {
            mod.logWarning("Detected that the difficulty is other than easy!");
            if (mod.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
                mod.logWarning("No mobs spawn on peaceful difficulty, so the bot will not be able to beat the game. Please change it!");
            } else {
                mod.logWarning("This could cause the bot to die sooner, please consider changing it...");
            }
        }
        ItemStorageTracker itemStorage = mod.getItemStorage();
        this.gatherResources.add(new MineBlockPriorityTask(
                ItemHelper.itemsToBlocks(ItemHelper.LOG), ItemHelper.LOG, MiningRequirement.STONE, (DistancePriorityCalculator) new DistanceItemPriorityCalculator(1050.0D, 450.0D, 5.0D, 4, 10), a -> Boolean.valueOf(

                (itemStorage.hasItem(new Item[]{Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE}) && itemStorage.getItemCount(ItemHelper.LOG) < 5))));
        addOreMiningTasks();
        addCollectFoodTask(mod);
        addStoneToolsTasks();
        addPickaxeTasks(mod);
        addDiamondArmorTasks(mod);
        addLootChestsTasks(mod);
        addPickupImportantItemsTask(mod);
        this.gatherResources.add(new MineBlockPriorityTask(new Block[]{Blocks.GRAVEL}, new Item[]{Items.FLINT}, MiningRequirement.STONE, (DistancePriorityCalculator) new DistanceItemPriorityCalculator(17500.0D, 7500.0D, 5.0D, 1, 1), a -> Boolean.valueOf(

                (itemStorage.hasItem(new Item[]{Items.STONE_SHOVEL}) && !itemStorage.hasItem(new Item[]{Items.FLINT_AND_STEEL})))));
        this.gatherResources.add(new MineBlockPriorityTask(ItemHelper.itemsToBlocks(ItemHelper.BED), ItemHelper.BED, MiningRequirement.HAND, (DistancePriorityCalculator) new DistanceItemPriorityCalculator(25000.0D, 25000.0D, 5.0D,
                getTargetBeds(mod), getTargetBeds(mod))));
        this.gatherResources.add(new CraftItemPriorityTask(200.0D, getRecipeTarget(Items.SHIELD), a -> Boolean.valueOf(itemStorage.hasItem(new Item[]{Items.IRON_INGOT}))));
        this.gatherResources.add(new CraftItemPriorityTask(300.0D, mod.getCraftingRecipeTracker().getFirstRecipeTarget(Items.BUCKET, 2), a -> Boolean.valueOf((itemStorage.getItemCount(new Item[]{Items.IRON_INGOT}) >= 6))));
        this.gatherResources.add(new CraftItemPriorityTask(100.0D, getRecipeTarget(Items.FLINT_AND_STEEL), a -> Boolean.valueOf(
                (itemStorage.hasItem(new Item[]{Items.IRON_INGOT}) && itemStorage.hasItem(new Item[]{Items.FLINT})))));
        this.gatherResources.add(new CraftItemPriorityTask(330.0D, getRecipeTarget(Items.DIAMOND_SWORD), a -> Boolean.valueOf((itemStorage.getItemCount(new Item[]{Items.DIAMOND}) >= 2 && StorageHelper.miningRequirementMet(mod, MiningRequirement.DIAMOND)))));
        this.gatherResources.add(new CraftItemPriorityTask(400.0D, getRecipeTarget(Items.GOLDEN_HELMET), a -> Boolean.valueOf((itemStorage.getItemCount(new Item[]{Items.GOLD_INGOT}) >= 5))));
        addSleepTask(mod);
        this.gatherResources.add(new ActionPriorityTask(a -> {
            Pair<Task, Double> pair = new Pair(TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1), Double.valueOf(Double.NEGATIVE_INFINITY));
            if (itemStorage.hasItem(new Item[]{Items.WATER_BUCKET}) || hasItem(mod, Items.WATER_BUCKET))
                return pair;
            Optional<BlockPos> optionalPos = mod.getBlockScanner().getNearestBlock(new Block[]{Blocks.WATER});
            if (optionalPos.isEmpty())
                return pair;
            double distance = Math.sqrt(BlockPosVer.getSquaredDistance(optionalPos.get(), (Position) mod.getPlayer().getPos()));
            if (distance > 55.0D)
                return pair;
            pair.setRight(Double.valueOf(10.0D / distance * 77.3D));
            return pair;
        }, a -> Boolean.valueOf(itemStorage.hasItem(new Item[]{Items.BUCKET})), false, true, true));
        addSmeltTasks(mod);
        addCookFoodTasks(mod);
    }

    public static BeatMinecraftConfig getConfig() {
        if (config == null) {
            Debug.logInternal("Initializing BeatMinecraftConfig");
            config = new BeatMinecraftConfig();
        }
        return config;
    }

    private static List<BlockPos> getFrameBlocks(AltoClefController mod, BlockPos endPortalCenter) {
        List<BlockPos> frameBlocks = new ArrayList<>();
        for (BlockPos pos : mod.getBlockScanner().getKnownLocations(new Block[]{Blocks.END_PORTAL_FRAME})) {
            if (pos.isWithinDistance((Vec3i) endPortalCenter, 20.0D))
                frameBlocks.add(pos);
        }
        Debug.logInternal("Frame blocks: " + String.valueOf(frameBlocks));
        return frameBlocks;
    }

    private static ItemTarget[] combine(ItemTarget[]... targets) {
        List<ItemTarget> combinedTargets = new ArrayList<>();
        for (ItemTarget[] targetArray : targets)
            combinedTargets.addAll(Arrays.asList(targetArray));
        Debug.logInternal("Combined Targets: " + String.valueOf(combinedTargets));
        ItemTarget[] combinedArray = combinedTargets.<ItemTarget>toArray(new ItemTarget[0]);
        Debug.logInternal("Combined Array: " + Arrays.toString(combinedArray));
        return combinedArray;
    }

    private static boolean isEndPortalFrameFilled(AltoClefController mod, BlockPos pos) {
        if (!mod.getChunkTracker().isChunkLoaded(pos)) {
            Debug.logInternal("Chunk is not loaded");
            return false;
        }
        BlockState blockState = mod.getWorld().getBlockState(pos);
        if (blockState.getBlock() != Blocks.END_PORTAL_FRAME) {
            Debug.logInternal("Block is not an End Portal Frame");
            return false;
        }
        boolean isFilled = ((Boolean) blockState.get((Property) EndPortalFrameBlock.EYE)).booleanValue();
        Debug.logInternal("End Portal Frame is " + (isFilled ? "filled" : "not filled"));
        return isFilled;
    }

    public static boolean isTaskRunning(AltoClefController mod, Task task) {
        if (task == null) {
            Debug.logInternal("Task is null");
            return false;
        }
        boolean taskActive = task.isActive();
        boolean taskFinished = task.isFinished();
        Debug.logInternal("Task is not null");
        Debug.logInternal("Task is " + (taskActive ? "active" : "not active"));
        Debug.logInternal("Task is " + (taskFinished ? "finished" : "not finished"));
        return (taskActive && !taskFinished);
    }

    public static void throwAwayItems(AltoClefController mod, Item... items) {
        throwAwaySlots(mod, mod.getItemStorage().getSlotsWithItemPlayerInventory(false, items));
    }

    public static void throwAwaySlots(AltoClefController mod, List<Slot> slots) {
        for (Slot slot : slots) {
            if (Slot.isCursor(slot)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                continue;
            }
            mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP);
        }
    }

    public static boolean hasItem(AltoClefController mod, Item item) {
        LivingEntity player = mod.getPlayer();
        LivingEntityInventory inv = mod.getInventory();
        List<DefaultedList<ItemStack>> combinedInventory = List.of(inv.main, inv.armor, inv.offHand);
        for (List<ItemStack> list : combinedInventory) {
            for (ItemStack itemStack : list) {
                if (itemStack.getItem().equals(item))
                    return true;
            }
        }
        return false;
    }

    public static int getCountWithCraftedFromOre(AltoClefController mod, Item item) {
        ItemStorageTracker itemStorage = mod.getItemStorage();
        if (item == Items.COAL)
            return itemStorage.getItemCount(new Item[]{item});
        if (item == Items.RAW_IRON) {
            int count = itemStorage.getItemCount(new Item[]{Items.RAW_IRON, Items.IRON_INGOT});
            count += itemStorage.getItemCount(new Item[]{Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.AXOLOTL_BUCKET, Items.POWDER_SNOW_BUCKET}) * 3;
            count += hasItem(mod, Items.SHIELD) ? 1 : 0;
            count += hasItem(mod, Items.FLINT_AND_STEEL) ? 1 : 0;
            count += hasItem(mod, Items.IRON_SWORD) ? 2 : 0;
            count += hasItem(mod, Items.IRON_PICKAXE) ? 3 : 0;
            count += hasItem(mod, Items.IRON_HELMET) ? 5 : 0;
            count += hasItem(mod, Items.IRON_CHESTPLATE) ? 8 : 0;
            count += hasItem(mod, Items.IRON_LEGGINGS) ? 7 : 0;
            count += hasItem(mod, Items.IRON_BOOTS) ? 4 : 0;
            return count;
        }
        if (item == Items.RAW_GOLD) {
            int count = itemStorage.getItemCount(new Item[]{Items.RAW_GOLD, Items.GOLD_INGOT});
            count += hasItem(mod, Items.GOLDEN_PICKAXE) ? 3 : 0;
            count += hasItem(mod, Items.GOLDEN_HELMET) ? 5 : 0;
            count += hasItem(mod, Items.GOLDEN_CHESTPLATE) ? 8 : 0;
            count += hasItem(mod, Items.GOLDEN_LEGGINGS) ? 7 : 0;
            count += hasItem(mod, Items.GOLDEN_BOOTS) ? 4 : 0;
            return count;
        }
        if (item == Items.DIAMOND) {
            int count = itemStorage.getItemCount(new Item[]{Items.DIAMOND});
            count += hasItem(mod, Items.DIAMOND_SWORD) ? 2 : 0;
            count += hasItem(mod, Items.DIAMOND_PICKAXE) ? 3 : 0;
            count += hasItem(mod, Items.DIAMOND_HELMET) ? 5 : 0;
            count += hasItem(mod, Items.DIAMOND_CHESTPLATE) ? 8 : 0;
            count += hasItem(mod, Items.DIAMOND_LEGGINGS) ? 7 : 0;
            count += hasItem(mod, Items.DIAMOND_BOOTS) ? 4 : 0;
            return count;
        }
        throw new IllegalStateException("Invalid ore item: " + String.valueOf(item));
    }

    private static Block[] mapOreItemToBlocks(Item item) {
        if (item.equals(Items.RAW_IRON))
            return new Block[]{Blocks.DEEPSLATE_IRON_ORE, Blocks.IRON_ORE};
        if (item.equals(Items.RAW_GOLD))
            return new Block[]{Blocks.DEEPSLATE_GOLD_ORE, Blocks.GOLD_ORE};
        if (item.equals(Items.DIAMOND))
            return new Block[]{Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DIAMOND_ORE};
        if (item.equals(Items.COAL))
            return new Block[]{Blocks.DEEPSLATE_COAL_ORE, Blocks.COAL_ORE};
        throw new IllegalStateException("Invalid ore: " + String.valueOf(item));
    }

    private void addSleepTask(AltoClefController mod) {
        boolean[] skipNight = {false};
        this.gatherResources.add(new ActionPriorityTask(a -> new PlaceBedAndSetSpawnTask(), () -> {
            if (!WorldHelper.canSleep(mod)) {
                skipNight[0] = false;
                return Double.NEGATIVE_INFINITY;
            }
            if (this.lastTask instanceof PlaceBedAndSetSpawnTask && this.lastTask.isFinished()) {
                skipNight[0] = true;
                mod.log("Failed to sleep :(");
                mod.log("Skipping night");
            }
            if (skipNight[0])
                return Double.NEGATIVE_INFINITY;
            Optional<BlockPos> pos = mod.getBlockScanner().getNearestBlock(ItemHelper.itemsToBlocks(ItemHelper.BED));
            return (pos.isPresent() && ((BlockPos) pos.get()).isCenterWithinDistance((Position) mod.getPlayer().getPos(), 30.0D)) ? 1000000.0D : Double.NEGATIVE_INFINITY;
        }));
    }

    private RecipeTarget getRecipeTarget(Item item) {
        ResourceTask task = TaskCatalogue.getItemTask(item, 1);
        if (task instanceof CraftInTableTask) {
            CraftInTableTask craftInTableTask = (CraftInTableTask) task;
            return craftInTableTask.getRecipeTargets()[0];
        }
        if (task instanceof CraftInInventoryTask) {
            CraftInInventoryTask craftInInventoryTask = (CraftInInventoryTask) task;
            return craftInInventoryTask.getRecipeTarget();
        }
        throw new IllegalStateException("Item isn't cataloged");
    }

    private void addPickupImportantItemsTask(AltoClefController mod) {
        List<Item> importantItems = List.of(Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.GOLDEN_HELMET, Items.DIAMOND_SWORD, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, Items.FLINT_AND_STEEL);
        this.gatherResources.add(new ActionPriorityTask(mod1 -> {
            Pair<Task, Double> pair = new Pair(null, Double.valueOf(0.0D));
            for (Item item : importantItems) {
                if (item == Items.IRON_PICKAXE && mod1.getItemStorage().hasItem(new Item[]{Items.DIAMOND_PICKAXE}))
                    continue;
                if (!mod1.getItemStorage().hasItem(new Item[]{item}) && mod1.getEntityTracker().itemDropped(new Item[]{item})) {
                    pair.setLeft(new PickupDroppedItemTask(item, 1));
                    pair.setRight(Double.valueOf(8000.0D));
                    return pair;
                }
            }
            return pair;
        }));
    }

    private void addCookFoodTasks(AltoClefController mod) {
        this.gatherResources.add(new ActionPriorityTask(a -> {
            Pair<Task, Double> pair = new Pair(null, Double.valueOf(Double.NEGATIVE_INFINITY));
            int rawFoodCount = a.getItemStorage().getItemCount(ItemHelper.RAW_FOODS);
            int readyFoodCount = a.getItemStorage().getItemCount(ItemHelper.COOKED_FOODS) + a.getItemStorage().getItemCount(new Item[]{Items.BREAD});
            double priority = (rawFoodCount >= 8) ? 450.0D : (rawFoodCount * 25);
            if (this.lastTask instanceof SmeltInSmokerTask)
                priority = Double.POSITIVE_INFINITY;
            if (readyFoodCount > 5 && priority < Double.POSITIVE_INFINITY)
                priority = 0.01D;
            CollectFoodTask.CookableFoodTarget[] arrayOfCookableFoodTarget = CollectMeatTask.COOKABLE_MEATS;
            int i = arrayOfCookableFoodTarget.length;
            byte b = 0;
            while (b < i) {
                CollectFoodTask.CookableFoodTarget cookable = arrayOfCookableFoodTarget[b];
                int rawCount = a.getItemStorage().getItemCount(new Item[]{cookable.getRaw()});
                if (rawCount == 0) {
                    b++;
                    continue;
                }
                int toSmelt = rawCount + a.getItemStorage().getItemCount(new Item[]{cookable.getCooked()});
                SmeltTarget target = new SmeltTarget(new ItemTarget(cookable.cookedFood, toSmelt), new ItemTarget(cookable.rawFood, rawCount), new Item[0]);
                pair.setLeft(new SmeltInSmokerTask(target));
                pair.setRight(Double.valueOf(priority));
                return pair;
            }
            return pair;
        }, a -> Boolean.valueOf(StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE)), true, false, false));
    }

    private void addSmeltTasks(AltoClefController mod) {
        ItemStorageTracker itemStorage = mod.getItemStorage();
        this.gatherResources.add(new ActionPriorityTask(a -> {
            Pair<Task, Double> pair = new Pair(null, Double.valueOf(Double.NEGATIVE_INFINITY));
            boolean hasSufficientPickaxe = itemStorage.hasItem(new Item[]{Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE});
            int neededIron = 11;
            if (itemStorage.hasItem(new Item[]{Items.FLINT_AND_STEEL}))
                neededIron--;
            if (hasItem(mod, Items.SHIELD))
                neededIron--;
            if (hasSufficientPickaxe)
                neededIron -= 3;
            neededIron -= Math.min(itemStorage.getItemCount(new Item[]{Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET}), 2) * 3;
            int count = itemStorage.getItemCount(new Item[]{Items.RAW_IRON});
            int includedCount = count + itemStorage.getItemCount(new Item[]{Items.IRON_INGOT});
            if ((!hasSufficientPickaxe && includedCount >= 3) || (!hasItem(mod, Items.SHIELD) && includedCount >= 1) || includedCount >= neededIron) {
                int toSmelt = Math.min(includedCount, neededIron);
                if (toSmelt <= 0)
                    return pair;
                pair.setLeft(new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.IRON_INGOT, toSmelt), new ItemTarget(Items.RAW_IRON, toSmelt), new Item[0])));
                pair.setRight(Double.valueOf(350.0D));
                return pair;
            }
            return pair;
        }, a -> Boolean.valueOf(itemStorage.hasItem(new Item[]{Items.RAW_IRON})), true, false, false));
        this.gatherResources.add(new ActionPriorityTask(a -> new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.GOLD_INGOT, 5), new ItemTarget(Items.RAW_GOLD, 5), new Item[0])), () -> 140.0D, a -> Boolean.valueOf(

                (itemStorage.getItemCount(new Item[]{Items.RAW_GOLD, Items.GOLD_INGOT}) >= 5 && !itemStorage.hasItem(new Item[]{Items.GOLDEN_HELMET}))), true, true, false));
    }

    private void addLootChestsTasks(AltoClefController mod) {
        this.gatherResources.add(new ActionPriorityTask(a -> {
            Pair<Task, Double> pair = new Pair(null, Double.valueOf(Double.NEGATIVE_INFINITY));
            Optional<BlockPos> chest = locateClosestUnopenedChest(mod);
            if (chest.isEmpty())
                return pair;
            double dst = Math.sqrt(BlockPosVer.getSquaredDistance(chest.get(), (Position) mod.getPlayer().getPos()));
            pair.setRight(Double.valueOf(30.0D / dst * 175.0D));
            pair.setLeft(new GetToBlockTask(((BlockPos) chest.get()).up()));
            return pair;
        }, a -> Boolean.valueOf(true), false, false, true));
        this.gatherResources.add(new ActionPriorityTask(m -> {
            Pair<Task, Double> pair = new Pair(null, Double.valueOf(Double.NEGATIVE_INFINITY));
            Optional<BlockPos> chest = locateClosestUnopenedChest(mod);
            if (chest.isEmpty())
                return pair;
            if (LookHelper.cleanLineOfSight((Entity) mod.getPlayer(), chest.get(), 10.0D) && ((BlockPos) chest.get()).isCenterWithinDistance((Position) mod.getPlayer().getEyePos(), 5.0D)) {
                pair.setLeft(new LootContainerTask(chest.get(), lootableItems(mod), noCurseOfBinding));
                pair.setRight(Double.valueOf(Double.POSITIVE_INFINITY));
            }
            return pair;
        }, a -> Boolean.valueOf(true), true, false, true));
    }

    private void addCollectFoodTask(AltoClefController mod) {
        List<Item> food = new LinkedList<>(ItemHelper.cookableFoodMap.values());
        food.addAll(ItemHelper.cookableFoodMap.keySet());
        food.addAll(List.of(Items.WHEAT, Items.BREAD));
        this.gatherResources.add(new ResourcePriorityTask((ItemPriorityCalculator) new CollectFoodPriorityCalculator(mod, config.foodUnits), a -> Boolean.valueOf(

                (StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE) && mod.getItemStorage().hasItem(new Item[]{Items.STONE_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD}) && CollectFoodTask.calculateFoodPotential(mod) < config.foodUnits)), (Task) new CollectFoodTask(config.foodUnits), ItemTarget.of(food.<Item>toArray(new Item[0]))));
        this.gatherResources.add(new ActionPriorityTask(mod12 -> {
            Pair<Task, Double> pair = new Pair(null, Double.valueOf(0.0D));
            pair.setLeft(TaskCatalogue.getItemTask(Items.WHEAT, mod12.getItemStorage().getItemCount(new Item[]{Items.HAY_BLOCK}) * 9 + mod12.getItemStorage().getItemCount(new Item[]{Items.WHEAT})));
            pair.setRight(Double.valueOf(10.0D));
            if (StorageHelper.calculateInventoryFoodScore(mod) < 5)
                pair.setRight(Double.valueOf(270.0D));
            return pair;
        }, a -> Boolean.valueOf(mod.getItemStorage().hasItem(new Item[]{Items.HAY_BLOCK}))));
        this.gatherResources.add(new ActionPriorityTask(mod1 -> {
            Pair<Task, Double> pair = new Pair(null, Double.valueOf(0.0D));
            pair.setLeft(TaskCatalogue.getItemTask("bread", mod1.getItemStorage().getItemCount(new Item[]{Items.WHEAT}) / 3 + mod1.getItemStorage().getItemCount(new Item[]{Items.BREAD})));
            pair.setRight(Double.valueOf(5.0D));
            if (StorageHelper.calculateInventoryFoodScore(mod) < 5)
                pair.setRight(Double.valueOf(250.0D));
            return pair;
        }, a -> Boolean.valueOf((mod.getItemStorage().getItemCount(new Item[]{Items.WHEAT}) >= 3))));
    }

    private void addOreMiningTasks() {
        this.gatherResources.add(getOrePriorityTask(Items.COAL, MiningRequirement.STONE, 1050, 250, 5, 4, 7));
        this.gatherResources.add(getOrePriorityTask(Items.RAW_IRON, MiningRequirement.STONE, 1050, 250, 5, 11, 11));
        this.gatherResources.add(getOrePriorityTask(Items.RAW_GOLD, MiningRequirement.IRON, 1050, 250, 5, 5, 5));
        this.gatherResources.add(getOrePriorityTask(Items.DIAMOND, MiningRequirement.IRON, 1050, 250, 5, 27, 30));
    }

    private PriorityTask getOrePriorityTask(Item item, MiningRequirement requirement, int multiplier, int unneededMultiplier, int unneededThreshold, int minCount, int maxCount) {
        Block[] blocks = mapOreItemToBlocks(item);
        return (PriorityTask) new MineBlockPriorityTask(blocks, new Item[]{item}, requirement, new DistanceOrePriorityCalculator(item, multiplier, unneededMultiplier, unneededThreshold, minCount, maxCount));
    }

    private void addStoneToolsTasks() {
        this.gatherResources.add(new ResourcePriorityTask((ItemPriorityCalculator) StaticItemPriorityCalculator.of(520), altoClef -> Boolean.valueOf(StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE)), true, true, false,

                ItemTarget.of(new Item[]{Items.STONE_AXE, Items.STONE_SWORD, Items.STONE_SHOVEL, Items.STONE_HOE})));
        this.gatherResources.add(new CraftItemPriorityTask(300.0D, getRecipeTarget(Items.STONE_SWORD), a -> Boolean.valueOf(
                (StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE) && !this.mod.getItemStorage().hasItem(new Item[]{Items.DIAMOND_SWORD, Items.IRON_SWORD})))));
        this.gatherResources.add(new CraftItemPriorityTask(300.0D, getRecipeTarget(Items.STONE_AXE), a -> Boolean.valueOf(
                (StorageHelper.miningRequirementMet(mod, MiningRequirement.STONE) && !this.mod.getItemStorage().hasItem(new Item[]{Items.DIAMOND_AXE, Items.IRON_AXE})))));
    }

    private void addDiamondArmorTasks(AltoClefController mod) {
        this.gatherResources.add(new CraftItemPriorityTask(350.0D, getRecipeTarget(Items.DIAMOND_CHESTPLATE), a -> Boolean.valueOf((mod.getItemStorage().getItemCount(new Item[]{Items.DIAMOND}) >= 8))));
        this.gatherResources.add(new CraftItemPriorityTask(300.0D, getRecipeTarget(Items.DIAMOND_LEGGINGS), a -> Boolean.valueOf((mod.getItemStorage().getItemCount(new Item[]{Items.DIAMOND}) >= 7))));
        this.gatherResources.add(new CraftItemPriorityTask(220.0D, getRecipeTarget(Items.DIAMOND_BOOTS), a -> Boolean.valueOf((mod.getItemStorage().getItemCount(new Item[]{Items.DIAMOND}) >= 5))));
    }

    private void addPickaxeTasks(AltoClefController mod) {
        this.gatherResources.add(new ResourcePriorityTask((ItemPriorityCalculator) StaticItemPriorityCalculator.of(400), a -> Boolean.valueOf(!mod.getItemStorage().hasItem(new Item[]{Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE})), ItemTarget.of(new Item[]{Items.WOODEN_PICKAXE})));
        this.gatherResources.add(new RecraftableItemPriorityTask(410.0D, 10000.0D, getRecipeTarget(Items.STONE_PICKAXE), a -> {
            List<Slot> list = mod.getItemStorage().getSlotsWithItemPlayerInventory(false, new Item[0]);
            boolean hasSafeIronPick = false;
            for (Slot slot : list) {
                if (slot.getInventorySlot() == -1)
                    continue;
                ItemStack stack = mod.getBaritone().getEntityContext().inventory().getStack(slot.getInventorySlot());
                if (!StorageHelper.shouldSaveStack(mod, Blocks.STONE, stack) && stack.getItem().equals(Items.IRON_PICKAXE)) {
                    hasSafeIronPick = true;
                    break;
                }
            }
            return Boolean.valueOf((StorageHelper.miningRequirementMet(mod, MiningRequirement.WOOD) && !mod.getItemStorage().hasItem(new Item[]{Items.STONE_PICKAXE}) && !hasSafeIronPick && !mod.getItemStorage().hasItem(new Item[]{Items.DIAMOND_PICKAXE})));
        }));
        this.gatherResources.add(new CraftItemPriorityTask(420.0D, getRecipeTarget(Items.IRON_PICKAXE), a -> Boolean.valueOf(
                (!mod.getItemStorage().hasItem(new Item[]{Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE}) && mod.getItemStorage().getItemCount(new Item[]{Items.IRON_INGOT}) >= 3))));
        this.gatherResources.add(new CraftItemPriorityTask(430.0D, getRecipeTarget(Items.DIAMOND_PICKAXE), a -> Boolean.valueOf((mod.getItemStorage().getItemCount(new Item[]{Items.DIAMOND}) >= 3))));
    }

    public boolean isFinished() {
        if (WorldHelper.getCurrentDimension(mod) == Dimension.OVERWORLD && this.dragonIsDead) {
            Debug.logInternal("isFinished - Dragon is dead in the Overworld");
            return true;
        }
        Debug.logInternal("isFinished - Returning false");
        return false;
    }

    private boolean needsBuildingMaterials(AltoClefController mod) {
        int materialCount = StorageHelper.getBuildingMaterialCount(mod);
        boolean shouldForce = isTaskRunning(mod, this.buildMaterialsTask);
        if (materialCount < config.minBuildMaterialCount || shouldForce) {
            Debug.logInternal("Building materials needed: " + materialCount);
            Debug.logInternal("Force build materials: " + shouldForce);
            return true;
        }
        Debug.logInternal("Building materials not needed");
        return false;
    }

    private void updateCachedEndItems(AltoClefController mod) {
        List<ItemEntity> droppedItems = mod.getEntityTracker().getDroppedItems();
        if (droppedItems.isEmpty() && !this.cachedEndItemNothingWaitTime.elapsed()) {
            Debug.logInternal("No dropped items and cache wait time not elapsed.");
            return;
        }
        this.cachedEndItemNothingWaitTime.reset();
        this.cachedEndItemDrops.clear();
        for (ItemEntity entity : droppedItems) {
            Item item = entity.getStack().getItem();
            int count = entity.getStack().getCount();
            this.cachedEndItemDrops.put(item, Integer.valueOf(((Integer) this.cachedEndItemDrops.getOrDefault(item, Integer.valueOf(0))).intValue() + count));
            Debug.logInternal("Added dropped item: " + String.valueOf(item) + " with count: " + count);
        }
    }

    private List<Item> lootableItems(AltoClefController mod) {
        List<Item> lootable = new ArrayList<>();
        lootable.add(Items.APPLE);
        lootable.add(Items.GOLDEN_APPLE);
        lootable.add(Items.ENCHANTED_GOLDEN_APPLE);
        lootable.add(Items.GOLDEN_CARROT);
        lootable.add(Items.OBSIDIAN);
        lootable.add(Items.STICK);
        lootable.add(Items.COAL);
        lootable.addAll(Arrays.<Item>stream(ItemHelper.LOG).toList());
        lootable.add(Items.BREAD);
        boolean isGoldenHelmetEquipped = StorageHelper.isArmorEquipped(mod, new Item[]{Items.GOLDEN_HELMET});
        boolean hasGoldenHelmet = mod.getItemStorage().hasItemInventoryOnly(new Item[]{Items.GOLDEN_HELMET});
        if (!mod.getItemStorage().hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE}))
            lootable.add(Items.IRON_PICKAXE);
        if (mod.getItemStorage().getItemCount(new Item[]{Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET}) < 2)
            lootable.add(Items.BUCKET);
        boolean hasEnoughGoldIngots = (mod.getItemStorage().getItemCountInventoryOnly(new Item[]{Items.GOLD_INGOT}) >= 5);
        if (!isGoldenHelmetEquipped && !hasGoldenHelmet)
            lootable.add(Items.GOLDEN_HELMET);
        if ((!hasEnoughGoldIngots && !isGoldenHelmetEquipped && !hasGoldenHelmet) || config.barterPearlsInsteadOfEndermanHunt)
            lootable.add(Items.GOLD_INGOT);
        lootable.add(Items.FLINT_AND_STEEL);
        if (!mod.getItemStorage().hasItemInventoryOnly(new Item[]{Items.FLINT_AND_STEEL}) && !mod.getItemStorage().hasItemInventoryOnly(new Item[]{Items.FIRE_CHARGE}))
            lootable.add(Items.FIRE_CHARGE);
        if (!mod.getItemStorage().hasItemInventoryOnly(new Item[]{Items.BUCKET}) && !mod.getItemStorage().hasItemInventoryOnly(new Item[]{Items.WATER_BUCKET}))
            lootable.add(Items.IRON_INGOT);
        if (!StorageHelper.itemTargetsMetInventory(mod, COLLECT_EYE_GEAR_MIN))
            lootable.add(Items.DIAMOND);
        if (!mod.getItemStorage().hasItemInventoryOnly(new Item[]{Items.FLINT}))
            lootable.add(Items.FLINT);
        Debug.logInternal("Lootable items: " + String.valueOf(lootable));
        return lootable;
    }

    protected void onStop(Task interruptTask) {
        this.mod.getExtraBaritoneSettings().canWalkOnEndPortal(false);
        this.mod.getBehaviour().pop();
        Debug.logInternal("Stopped onStop method");
        Debug.logInternal("canWalkOnEndPortal set to false");
        Debug.logInternal("Behaviour popped");
        Debug.logInternal("Stopped tracking BED blocks");
        Debug.logInternal("Stopped tracking TRACK_BLOCKS");
    }

    protected boolean isEqual(Task other) {
        boolean isSameTask = other instanceof adris.altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;
        if (!isSameTask)
            Debug.logInternal("The 'other' task is not of type BeatMinecraftTask");
        return isSameTask;
    }

    protected String toDebugString() {
        return "Beating the game (Miran version).";
    }

    private boolean endPortalFound(AltoClefController mod, BlockPos endPortalCenter) {
        if (endPortalCenter == null) {
            Debug.logInternal("End portal center is null");
            return false;
        }
        return true;
    }

    private boolean endPortalOpened(AltoClefController mod, BlockPos endPortalCenter) {
        if (this.endPortalOpened && endPortalCenter != null) {
            BlockScanner blockTracker = mod.getBlockScanner();
            if (blockTracker != null) {
                boolean isValid = blockTracker.isBlockAtPosition(endPortalCenter, new Block[]{Blocks.END_PORTAL});
                Debug.logInternal("End Portal is " + (isValid ? "valid" : "invalid"));
                return isValid;
            }
        }
        Debug.logInternal("End Portal is not opened yet");
        return false;
    }

    private boolean spawnSetNearPortal(AltoClefController mod, BlockPos endPortalCenter) {
        if (this.bedSpawnLocation == null) {
            Debug.logInternal("Bed spawn location is null");
            return false;
        }
        BlockScanner blockTracker = mod.getBlockScanner();
        boolean isValid = blockTracker.isBlockAtPosition(this.bedSpawnLocation, ItemHelper.itemsToBlocks(ItemHelper.BED));
        Debug.logInternal("Spawn set near portal: " + isValid);
        return isValid;
    }

    private Optional<BlockPos> locateClosestUnopenedChest(AltoClefController mod) {
        if (!WorldHelper.getCurrentDimension(mod).equals(Dimension.OVERWORLD)) {
            return Optional.empty();
        }

        // Find the nearest tracking block position
        return mod.getBlockScanner().getNearestBlock(blockPos -> {
            if (blacklistedChests.contains(blockPos)) return false;

            boolean isUnopenedChest = WorldHelper.isUnopenedChest(mod, blockPos);
            boolean isWithinDistance = mod.getPlayer().getBlockPos().isWithinDistance(blockPos, 150);
            boolean isLootableChest = canBeLootablePortalChest(mod, blockPos);

            // TODO make more sophisticated
            //dont open spawner chests
            Optional<BlockPos> nearestSpawner = mod.getBlockScanner().getNearestBlock(WorldHelper.toVec3d(blockPos), Blocks.SPAWNER);
            if (nearestSpawner.isPresent() && nearestSpawner.get().isWithinDistance(blockPos, 6)) {
                blacklistedChests.add(blockPos);
                return false;
            }

            // TODO use shipwreck finder instead

            Box box = new Box(blockPos.getX() - 5, blockPos.getY() - 5, blockPos.getZ() - 5,
                    blockPos.getX() + 5, blockPos.getY() + 5, blockPos.getZ() + 5);

            Stream<BlockState> states = BlockPos.stream(box).map(pos -> mod.getWorld().getBlockState(pos));

            if (states.anyMatch((state) -> state.getBlock().equals(Blocks.WATER))) {
                blacklistedChests.add(blockPos);
                return false;
            }

            Debug.logInternal("isUnopenedChest: " + isUnopenedChest);
            Debug.logInternal("isWithinDistance: " + isWithinDistance);
            Debug.logInternal("isLootableChest: " + isLootableChest);

            return isUnopenedChest && isWithinDistance && isLootableChest;
        }, Blocks.CHEST);
    }

    protected void onStart() {
        resetTimers();
        this.mod.getBehaviour().push();
        addThrowawayItemsWarning(this.mod);
        addProtectedItems(this.mod);
        allowWalkingOnEndPortal(this.mod);
        avoidDragonBreath(this.mod);
        avoidBreakingBed(this.mod);
        this.mod.getBehaviour().avoidBlockBreaking(pos -> this.mod.getWorld().getBlockState(pos).getBlock().equals(Blocks.NETHER_PORTAL));
    }

    private void resetTimers() {
        this.timer1.reset();
        this.timer2.reset();
        this.timer3.reset();
    }

    private void addThrowawayItemsWarning(AltoClefController mod) {
        String settingsWarningTail = "in \".minecraft/altoclef_settings.json\". @gamer may break if you don't add this! (sorry!)";
        if (!ArrayUtils.contains(mod.getModSettings().getThrowawayItems(mod), Items.END_STONE))
            Debug.logWarning("\"end_stone\" is not part of your \"throwawayItems\" list " + settingsWarningTail);
        if (!mod.getModSettings().shouldThrowawayUnusedItems())
            Debug.logWarning("\"throwawayUnusedItems\" is not set to true " + settingsWarningTail);
    }

    private void addProtectedItems(AltoClefController mod) {
        mod.getBehaviour().addProtectedItems(new Item[]{
                Items.ENDER_EYE, Items.BLAZE_ROD, Items.BLAZE_POWDER, Items.ENDER_PEARL, Items.CRAFTING_TABLE, Items.IRON_INGOT, Items.WATER_BUCKET, Items.FLINT_AND_STEEL, Items.SHIELD, Items.SHEARS,
                Items.BUCKET, Items.GOLDEN_HELMET, Items.SMOKER, Items.FURNACE});
        mod.getBehaviour().addProtectedItems(ItemHelper.BED);
        mod.getBehaviour().addProtectedItems(ItemHelper.IRON_ARMORS);
        mod.getBehaviour().addProtectedItems(ItemHelper.LOG);
        Debug.logInternal("Protected items added successfully.");
    }

    private void allowWalkingOnEndPortal(AltoClefController mod) {
        mod.getBehaviour().allowWalkingOn(blockPos -> {
            if (this.enterindEndPortal && mod.getChunkTracker().isChunkLoaded(blockPos)) {
                BlockState blockState = mod.getWorld().getBlockState(blockPos);
                boolean isEndPortal = (blockState.getBlock() == Blocks.END_PORTAL);
                if (isEndPortal)
                    Debug.logInternal("Walking on End Portal at " + blockPos.toString());
                return isEndPortal;
            }
            return false;
        });
    }

    private void avoidDragonBreath(AltoClefController mod) {
        mod.getBehaviour().avoidWalkingThrough(blockPos -> {
            Dimension currentDimension = WorldHelper.getCurrentDimension(mod);
            boolean isEndDimension = (currentDimension == Dimension.END);
            boolean isTouchingDragonBreath = this.dragonBreathTracker.isTouchingDragonBreath(blockPos);
            if (isEndDimension && !this.escapingDragonsBreath && isTouchingDragonBreath) {
                Debug.logInternal("Avoiding dragon breath at blockPos: " + String.valueOf(blockPos));
                return true;
            }
            return false;
        });
    }

    private void avoidBreakingBed(AltoClefController mod) {
        mod.getBehaviour().avoidBlockBreaking(blockPos -> {
            if (this.bedSpawnLocation != null) {
                BlockPos bedHead = WorldHelper.getBedHead(mod, this.bedSpawnLocation);
                BlockPos bedFoot = WorldHelper.getBedFoot(mod, this.bedSpawnLocation);
                boolean shouldAvoidBreaking = (blockPos.equals(bedHead) || blockPos.equals(bedFoot));
                if (shouldAvoidBreaking)
                    Debug.logInternal("Avoiding breaking bed at block position: " + String.valueOf(blockPos));
                return shouldAvoidBreaking;
            }
            return false;
        });
    }

    private void blackListDangerousBlock(AltoClefController mod, Block block) {
        Optional<BlockPos> nearestTracking = mod.getBlockScanner().getNearestBlock(new Block[]{block});
        if (nearestTracking.isPresent()) {
            Iterable<Entity> entities = mod.getWorld().iterateEntities();
            for (Entity entity : entities) {
                if (mod.getBlockScanner().isUnreachable(nearestTracking.get()) || !(entity instanceof net.minecraft.entity.mob.HostileEntity))
                    continue;
                if (mod.getPlayer().squaredDistanceTo(entity.getPos()) < 150.0D && ((BlockPos) nearestTracking.get()).isCenterWithinDistance((Position) entity.getPos(), 30.0D)) {
                    Debug.logMessage("Blacklisting dangerous " + block.toString());
                    mod.getBlockScanner().requestBlockUnreachable(nearestTracking.get(), 0);
                }
            }
        }
    }

    protected Task onTick() {
        ItemStorageTracker itemStorage = this.mod.getItemStorage();
        double blockPlacementPenalty = 10.0D;
        if (StorageHelper.getNumberOfThrowawayBlocks(this.mod) > 128) {
            blockPlacementPenalty = 5.0D;
        } else if (StorageHelper.getNumberOfThrowawayBlocks(this.mod) > 64) {
            blockPlacementPenalty = 7.5D;
        }
        (this.mod.getBaritoneSettings()).blockPlacementPenalty.set(Double.valueOf(blockPlacementPenalty));
        if (this.mod.getPlayer().getMainHandStack().getItem() instanceof net.minecraft.item.EnderEyeItem && !openingEndPortal) {
            List<ItemStack> itemStacks = itemStorage.getItemStacksPlayerInventory(true);
            for (ItemStack itemStack : itemStacks) {
                Item item = itemStack.getItem();
                if (item instanceof net.minecraft.item.SwordItem || item instanceof net.minecraft.item.AxeItem)
                    this.mod.getSlotHandler().forceEquipItem(item);
            }
        }
        boolean shouldSwap = false;
        boolean hasInHotbar = false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mod.getBaritone().getEntityContext().inventory().getStack(i);
            if (stack.getItem().equals(Items.IRON_PICKAXE) && StorageHelper.shouldSaveStack(this.mod, Blocks.STONE, stack))
                shouldSwap = true;
            if (stack.getItem().equals(Items.STONE_PICKAXE))
                hasInHotbar = true;
        }
        if (shouldSwap && !hasInHotbar &&
                itemStorage.hasItem(new Item[]{Items.STONE_PICKAXE}))
            this.mod.getSlotHandler().forceEquipItem(Items.STONE_PICKAXE);
        boolean eyeGearSatisfied = StorageHelper.isArmorEquippedAll(mod, COLLECT_EYE_ARMOR);
        boolean ironGearSatisfied = StorageHelper.isArmorEquippedAll(mod, COLLECT_IRON_ARMOR);
        if (itemStorage.hasItem(new Item[]{Items.DIAMOND_PICKAXE})) {
            this.mod.getBehaviour().setBlockBreakAdditionalPenalty(1.2D);
        } else {
            this.mod.getBehaviour().setBlockBreakAdditionalPenalty(((Double) (this.mod.getBaritoneSettings()).blockBreakAdditionalPenalty.defaultValue).doubleValue());
        }
        Predicate<Task> isCraftingTableTask = task -> {
            return task instanceof CraftInTableTask;
        };
        List<BlockPos> craftingTables = this.mod.getBlockScanner().getKnownLocations(new Block[]{Blocks.CRAFTING_TABLE});
        for (BlockPos craftingTable : craftingTables) {
            if (itemStorage.hasItem(new Item[]{Items.CRAFTING_TABLE}) && !thisOrChildSatisfies(isCraftingTableTask) && !this.mod.getBlockScanner().isUnreachable(craftingTable)) {
                Debug.logMessage("Blacklisting extra crafting table.");
                this.mod.getBlockScanner().requestBlockUnreachable(craftingTable, 0);
            }
            if (!this.mod.getBlockScanner().isUnreachable(craftingTable)) {
                BlockState craftingTablePosUp = this.mod.getWorld().getBlockState(craftingTable.up(2));
                if (this.mod.getEntityTracker().entityFound(new Class[]{WitchEntity.class})) {
                    Optional<Entity> witch = this.mod.getEntityTracker().getClosestEntity(new Class[]{WitchEntity.class});
                    if (witch.isPresent() && craftingTable.isCenterWithinDistance((Position) ((Entity) witch.get()).getPos(), 15.0D)) {
                        Debug.logMessage("Blacklisting witch crafting table.");
                        this.mod.getBlockScanner().requestBlockUnreachable(craftingTable, 0);
                    }
                }
                if (craftingTablePosUp.getBlock() == Blocks.WHITE_WOOL) {
                    Debug.logMessage("Blacklisting pillage crafting table.");
                    this.mod.getBlockScanner().requestBlockUnreachable(craftingTable, 0);
                }
            }
        }
        List<BlockPos> smokers = this.mod.getBlockScanner().getKnownLocations(new Block[]{Blocks.SMOKER});
        for (BlockPos smoker : smokers) {
            if (itemStorage.hasItem(new Item[]{Items.SMOKER}) && !this.mod.getBlockScanner().isUnreachable(smoker)) {
                Debug.logMessage("Blacklisting extra smoker.");
                this.mod.getBlockScanner().requestBlockUnreachable(smoker, 0);
            }
        }
        List<BlockPos> furnaces = this.mod.getBlockScanner().getKnownLocations(new Block[]{Blocks.FURNACE});
        for (BlockPos furnace : furnaces) {
            if (itemStorage.hasItem(new Item[]{Items.FURNACE}) && !this.goToNetherTask.isActive() && !this.ranStrongholdLocator && !this.mod.getBlockScanner().isUnreachable(furnace)) {
                Debug.logMessage("Blacklisting extra furnace.");
                this.mod.getBlockScanner().requestBlockUnreachable(furnace, 0);
            }
        }
        List<BlockPos> logs = this.mod.getBlockScanner().getKnownLocations(ItemHelper.itemsToBlocks(ItemHelper.LOG));
        for (BlockPos log : logs) {
            Iterable<Entity> entities = this.mod.getWorld().iterateEntities();
            for (Entity entity : entities) {
                if (entity instanceof net.minecraft.entity.mob.PillagerEntity && !this.mod.getBlockScanner().isUnreachable(log) && log.isCenterWithinDistance((Position) entity.getPos(), 40.0D)) {
                    Debug.logMessage("Blacklisting pillage log.");
                    this.mod.getBlockScanner().requestBlockUnreachable(log, 0);
                }
            }
            if (log.getY() < 62 && !this.mod.getBlockScanner().isUnreachable(log) && !ironGearSatisfied && !eyeGearSatisfied) {
                Debug.logMessage("Blacklisting dangerous log.");
                this.mod.getBlockScanner().requestBlockUnreachable(log, 0);
            }
        }
        if (!ironGearSatisfied && !eyeGearSatisfied) {
            blackListDangerousBlock(this.mod, Blocks.DEEPSLATE_COAL_ORE);
            blackListDangerousBlock(this.mod, Blocks.COAL_ORE);
            blackListDangerousBlock(this.mod, Blocks.DEEPSLATE_IRON_ORE);
            blackListDangerousBlock(this.mod, Blocks.IRON_ORE);
        }
        List<Block> ancientCityBlocks = List.of(Blocks.DEEPSLATE_BRICKS, Blocks.SCULK, Blocks.SCULK_VEIN, Blocks.SCULK_SENSOR, Blocks.SCULK_SHRIEKER, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.CRACKED_DEEPSLATE_BRICKS, Blocks.SOUL_LANTERN, Blocks.DEEPSLATE_TILES, Blocks.POLISHED_DEEPSLATE);
        int radius = 5;
        label477:
        for (BlockPos pos : this.mod.getBlockScanner().getKnownLocations(ItemHelper.itemsToBlocks(ItemHelper.WOOL))) {
            for (int x = -5; x < 5; x++) {
                for (int y = -5; y < 5; y++) {
                    for (int z = -5; z < 5; z++) {
                        BlockPos p = pos.add(x, y, z);
                        Block block = this.mod.getWorld().getBlockState(p).getBlock();
                        if (ancientCityBlocks.contains(block)) {
                            Debug.logMessage("Blacklisting ancient city wool " + String.valueOf(pos));
                            this.mod.getBlockScanner().requestBlockUnreachable(pos, 0);
                            continue label477;
                        }
                    }
                }
            }
        }
        if (locateStrongholdTask.isActive() && WorldHelper.getCurrentDimension(mod) == Dimension.OVERWORLD && !this.mod.getBaritone().getExploreProcess().isActive() && this.timer1.elapsed())
            this.timer1.reset();
        if (((this.getOneBedTask != null && this.getOneBedTask.isActive()) || (this.sleepThroughNightTask.isActive() && !itemStorage.hasItem(ItemHelper.BED))) && this.getBedTask == null && !this.mod.getBaritone().getExploreProcess().isActive() && this.timer3.elapsed())
            this.timer3.reset();
        if (WorldHelper.getCurrentDimension(mod) != Dimension.END && itemStorage.hasItem(new Item[]{Items.SHIELD}) && !itemStorage.hasItemInOffhand(controller, Items.SHIELD))
            return (Task) new EquipArmorTask(new Item[]{Items.SHIELD});
        if (WorldHelper.getCurrentDimension(mod) == Dimension.NETHER) {
            if (itemStorage.hasItem(new Item[]{Items.GOLDEN_HELMET}))
                return (Task) new EquipArmorTask(new Item[]{Items.GOLDEN_HELMET});
            if (itemStorage.hasItem(new Item[]{Items.DIAMOND_HELMET}) && !hasItem(this.mod, Items.GOLDEN_HELMET))
                return (Task) new EquipArmorTask(new Item[]{Items.DIAMOND_HELMET});
        } else if (itemStorage.hasItem(new Item[]{Items.DIAMOND_HELMET})) {
            return (Task) new EquipArmorTask(new Item[]{Items.DIAMOND_HELMET});
        }
        if (itemStorage.hasItem(new Item[]{Items.DIAMOND_CHESTPLATE}))
            return (Task) new EquipArmorTask(new Item[]{Items.DIAMOND_CHESTPLATE});
        if (itemStorage.hasItem(new Item[]{Items.DIAMOND_LEGGINGS}))
            return (Task) new EquipArmorTask(new Item[]{Items.DIAMOND_LEGGINGS});
        if (itemStorage.hasItem(new Item[]{Items.DIAMOND_BOOTS}))
            return (Task) new EquipArmorTask(new Item[]{Items.DIAMOND_BOOTS});
        if (itemStorage.getItemCount(new Item[]{Items.FURNACE}) > 1)
            return (Task) new PlaceBlockNearbyTask(new Block[]{Blocks.FURNACE});
        if (itemStorage.getItemCount(new Item[]{Items.CRAFTING_TABLE}) > 1)
            return (Task) new PlaceBlockNearbyTask(new Block[]{Blocks.CRAFTING_TABLE});
        throwAwayItems(this.mod, new Item[]{Items.SAND, Items.RED_SAND});
        throwAwayItems(this.mod, new Item[]{Items.TORCH});
        throwAwayItems(this.mod, this.uselessItems.uselessItems);
        if (itemStorage.hasItem(new Item[]{Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE}))
            throwAwayItems(this.mod, new Item[]{Items.WOODEN_PICKAXE});
        if (itemStorage.hasItem(new Item[]{Items.DIAMOND_PICKAXE}))
            throwAwayItems(this.mod, new Item[]{Items.IRON_PICKAXE, Items.STONE_PICKAXE});
        if (itemStorage.hasItem(new Item[]{Items.DIAMOND_SWORD}))
            throwAwayItems(this.mod, new Item[]{Items.STONE_SWORD, Items.IRON_SWORD});
        if (itemStorage.hasItem(new Item[]{Items.GOLDEN_HELMET}))
            throwAwayItems(this.mod, new Item[]{Items.RAW_GOLD, Items.GOLD_INGOT});
        if (itemStorage.hasItem(new Item[]{Items.FLINT}) || itemStorage.hasItem(new Item[]{Items.FLINT_AND_STEEL}))
            throwAwayItems(this.mod, new Item[]{Items.GRAVEL});
        if (itemStorage.hasItem(new Item[]{Items.FLINT_AND_STEEL}))
            throwAwayItems(this.mod, new Item[]{Items.FLINT});
        if (isTaskRunning(this.mod, (Task) this.getRidOfExtraWaterBucketTask))
            return (Task) this.getRidOfExtraWaterBucketTask;
        if (itemStorage.getItemCount(new Item[]{Items.WATER_BUCKET}) > 1) {
            this.getRidOfExtraWaterBucketTask = new GetRidOfExtraWaterBucketTask();
            return (Task) this.getRidOfExtraWaterBucketTask;
        }
        if (itemStorage.getItemCount(new Item[]{Items.FLINT_AND_STEEL}) > 1)
            throwAwayItems(this.mod, new Item[]{Items.FLINT_AND_STEEL});
        if (itemStorage.getItemCount(ItemHelper.BED) > getTargetBeds(this.mod) && !endPortalFound(this.mod, this.endPortalCenterLocation) && WorldHelper.getCurrentDimension(mod) != Dimension.END)
            throwAwayItems(this.mod, ItemHelper.BED);
        this.enterindEndPortal = false;
        if (WorldHelper.getCurrentDimension(mod) == Dimension.END) {
            if (!this.mod.getWorld().isChunkLoaded(0, 0)) {
                setDebugState("Waiting for chunks to load");
                return null;
            }
            updateCachedEndItems(this.mod);
            if (this.mod.getEntityTracker().itemDropped(ItemHelper.BED) && (needsBeds(this.mod) || WorldHelper.getCurrentDimension(mod) == Dimension.END))
                return (Task) new PickupDroppedItemTask(new ItemTarget(ItemHelper.BED), true);
            if (!itemStorage.hasItem(new Item[]{Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE})) {
                if (this.mod.getEntityTracker().itemDropped(new Item[]{Items.IRON_PICKAXE}))
                    return (Task) new PickupDroppedItemTask(Items.IRON_PICKAXE, 1);
                if (this.mod.getEntityTracker().itemDropped(new Item[]{Items.DIAMOND_PICKAXE}))
                    return (Task) new PickupDroppedItemTask(Items.DIAMOND_PICKAXE, 1);
            }
            if (!itemStorage.hasItem(new Item[]{Items.WATER_BUCKET}) && this.mod.getEntityTracker().itemDropped(new Item[]{Items.WATER_BUCKET}))
                return (Task) new PickupDroppedItemTask(Items.WATER_BUCKET, 1);
            for (Item armorCheck : COLLECT_EYE_ARMOR_END) {
                if (!StorageHelper.isArmorEquipped(mod, new Item[]{armorCheck})) {
                    if (itemStorage.hasItem(new Item[]{armorCheck})) {
                        setDebugState("Equipping armor.");
                        return (Task) new EquipArmorTask(new Item[]{armorCheck});
                    }
                    if (this.mod.getEntityTracker().itemDropped(new Item[]{armorCheck}))
                        return (Task) new PickupDroppedItemTask(armorCheck, 1);
                }
            }
            this.dragonBreathTracker.updateBreath(this.mod);
            for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer(controller.getPlayer())) {
                if (this.dragonBreathTracker.isTouchingDragonBreath(playerIn)) {
                    setDebugState("ESCAPE dragons breath");
                    this.escapingDragonsBreath = true;
                    return this.dragonBreathTracker.getRunAwayTask();
                }
            }
            this.escapingDragonsBreath = false;
            if (this.mod.getBlockScanner().anyFound(new Block[]{Blocks.END_PORTAL})) {
                setDebugState("WOOHOO");
                this.dragonIsDead = true;
                this.enterindEndPortal = true;
                if (!this.mod.getExtraBaritoneSettings().isCanWalkOnEndPortal())
                    this.mod.getExtraBaritoneSettings().canWalkOnEndPortal(true);
                return (Task) new DoToClosestBlockTask(blockPos -> new GetToBlockTask(blockPos.up()), pos -> (Math.abs(pos.getX()) + Math.abs(pos.getZ()) <= 1), new Block[]{Blocks.END_PORTAL});
            }
            if (itemStorage.hasItem(ItemHelper.BED) || this.mod.getBlockScanner().anyFound(ItemHelper.itemsToBlocks(ItemHelper.BED))) {
                setDebugState("Bed strats");
                return this.killDragonBedStratsTask;
            }
            setDebugState("No beds, regular strats.");
            return (Task) new KillEnderDragonTask();
        }
        this.cachedEndItemNothingWaitTime.reset();
        if (!endPortalOpened(this.mod, this.endPortalCenterLocation) && WorldHelper.getCurrentDimension(mod) == Dimension.OVERWORLD) {
            Optional<BlockPos> endPortal = this.mod.getBlockScanner().getNearestBlock(new Block[]{Blocks.END_PORTAL});
            if (endPortal.isPresent()) {
                this.endPortalCenterLocation = endPortal.get();
                this.endPortalOpened = true;
            } else {
                this.endPortalCenterLocation = doSimpleSearchForEndPortal(this.mod);
            }
        }
        if (isTaskRunning(this.mod, this.rePickupTask))
            return this.rePickupTask;
        if (!this.endPortalOpened && WorldHelper.getCurrentDimension(mod) != Dimension.END && config.rePickupCraftingTable && !itemStorage.hasItem(new Item[]{Items.CRAFTING_TABLE}) && !thisOrChildSatisfies(isCraftingTableTask) && (this.mod.getBlockScanner().anyFound(blockPos -> (WorldHelper.canBreak(mod, blockPos) && WorldHelper.canReach(mod, blockPos)), new Block[]{Blocks.CRAFTING_TABLE}) || this.mod.getEntityTracker().itemDropped(new Item[]{Items.CRAFTING_TABLE})) && this.pickupCrafting) {
            setDebugState("Picking up the crafting table while we are at it.");
            return (Task) new MineAndCollectTask(Items.CRAFTING_TABLE, 1, new Block[]{Blocks.CRAFTING_TABLE}, MiningRequirement.HAND);
        }
        if (config.rePickupSmoker && !this.endPortalOpened && WorldHelper.getCurrentDimension(mod) != Dimension.END && !itemStorage.hasItem(new Item[]{Items.SMOKER}) && (this.mod.getBlockScanner().anyFound(blockPos -> (WorldHelper.canBreak(mod, blockPos) && WorldHelper.canReach(mod, blockPos)), new Block[]{Blocks.SMOKER}) || this.mod.getEntityTracker().itemDropped(new Item[]{Items.SMOKER})) && this.pickupSmoker) {
            setDebugState("Picking up the smoker while we are at it.");
            this.rePickupTask = (Task) new MineAndCollectTask(Items.SMOKER, 1, new Block[]{Blocks.SMOKER}, MiningRequirement.WOOD);
            return this.rePickupTask;
        }
        if (config.rePickupFurnace && !this.endPortalOpened && WorldHelper.getCurrentDimension(mod) != Dimension.END && !itemStorage.hasItem(new Item[]{Items.FURNACE}) && (this.mod.getBlockScanner().anyFound(blockPos -> (WorldHelper.canBreak(mod, blockPos) && WorldHelper.canReach(mod, blockPos)), new Block[]{Blocks.FURNACE}) || this.mod.getEntityTracker().itemDropped(new Item[]{Items.FURNACE})) && !this.goToNetherTask.isActive() && !this.ranStrongholdLocator && this.pickupFurnace) {
            setDebugState("Picking up the furnace while we are at it.");
            this.rePickupTask = (Task) new MineAndCollectTask(Items.FURNACE, 1, new Block[]{Blocks.FURNACE}, MiningRequirement.WOOD);
            return this.rePickupTask;
        }
        this.pickupFurnace = false;
        this.pickupSmoker = false;
        this.pickupCrafting = false;
        if (config.sleepThroughNight && !this.endPortalOpened && WorldHelper.getCurrentDimension(mod) == Dimension.OVERWORLD) {
            if (WorldHelper.canSleep(mod)) {
                if (this.timer2.elapsed())
                    this.timer2.reset();
                if (this.timer2.getDuration() >= 30.0D && !this.mod.getPlayer().isSleeping()) {
                    if (this.mod.getEntityTracker().itemDropped(ItemHelper.BED) && needsBeds(this.mod)) {
                        setDebugState("Resetting sleep through night task.");
                        return (Task) new PickupDroppedItemTask(new ItemTarget(ItemHelper.BED), true);
                    }
                    if (anyBedsFound(this.mod)) {
                        setDebugState("Resetting sleep through night task.");
                        return (Task) new DoToClosestBlockTask(DestroyBlockTask::new, ItemHelper.itemsToBlocks(ItemHelper.BED));
                    }
                }
                setDebugState("Sleeping through night");
                return this.sleepThroughNightTask;
            }
            if (!itemStorage.hasItem(ItemHelper.BED) && (this.mod.getBlockScanner().anyFound(blockPos -> WorldHelper.canBreak(mod, blockPos), ItemHelper.itemsToBlocks(ItemHelper.BED)) || isTaskRunning(this.mod, this.getOneBedTask))) {
                setDebugState("Getting one bed to sleep in at night.");
                return this.getOneBedTask;
            }
        }
        boolean needsEyes = (!endPortalOpened(this.mod, this.endPortalCenterLocation) && WorldHelper.getCurrentDimension(mod) != Dimension.END);
        int filledPortalFrames = getFilledPortalFrames(this.mod, this.endPortalCenterLocation);
        int eyesNeededMin = needsEyes ? (config.minimumEyes - filledPortalFrames) : 0;
        int eyesNeeded = needsEyes ? (config.targetEyes - filledPortalFrames) : 0;
        int eyes = itemStorage.getItemCount(new Item[]{Items.ENDER_EYE});
        if (eyes < eyesNeededMin || (!this.ranStrongholdLocator && this.collectingEyes && eyes < eyesNeeded)) {
            this.collectingEyes = true;
            return getEyesOfEnderTask(this.mod, eyesNeeded);
        }
        this.collectingEyes = false;
        if (itemStorage.getItemCount(new Item[]{Items.DIAMOND}) >= 3 && !itemStorage.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE}))
            return (Task) TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
        if (itemStorage.getItemCount(new Item[]{Items.IRON_INGOT}) >= 3 && !itemStorage.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE}))
            return (Task) TaskCatalogue.getItemTask(Items.IRON_PICKAXE, 1);
        if (!itemStorage.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE}))
            return (Task) TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 1);
        if (!itemStorage.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE}))
            return (Task) TaskCatalogue.getItemTask(Items.WOODEN_PICKAXE, 1);
        if (WorldHelper.getCurrentDimension(mod) == Dimension.OVERWORLD) {
            if (itemStorage.hasItem(new Item[]{Items.DIAMOND_PICKAXE})) {
                Item[] throwGearItems = {Items.STONE_SWORD, Items.STONE_PICKAXE, Items.IRON_SWORD, Items.IRON_PICKAXE};
                List<Slot> ironArmors = itemStorage.getSlotsWithItemPlayerInventory(true, COLLECT_IRON_ARMOR);
                List<Slot> throwGears = itemStorage.getSlotsWithItemPlayerInventory(true, throwGearItems);
                if ((itemStorage.hasItem(new Item[]{Items.FLINT_AND_STEEL}) || itemStorage.hasItem(new Item[]{Items.FIRE_CHARGE}))) {
                    for (Slot throwGear : throwGears) {
                        if (Slot.isCursor(throwGear)) {
                            if (!this.mod.getControllerExtras().isBreakingBlock())
                                LookHelper.randomOrientation(controller);
                            this.mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                            continue;
                        }
                        this.mod.getSlotHandler().clickSlot(throwGear, 0, SlotActionType.PICKUP);
                    }
                    for (Slot ironArmor : ironArmors) {
                        if (Slot.isCursor(ironArmor)) {
                            if (!this.mod.getControllerExtras().isBreakingBlock())
                                LookHelper.randomOrientation(controller);
                            this.mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                            continue;
                        }
                        this.mod.getSlotHandler().clickSlot(ironArmor, 0, SlotActionType.PICKUP);
                    }
                }
            }
            this.ranStrongholdLocator = true;
            if (WorldHelper.getCurrentDimension(mod) == Dimension.OVERWORLD && needsBeds(this.mod)) {
                setDebugState("Getting beds before stronghold search.");
                if (!this.mod.getBaritone().getExploreProcess().isActive() && this.timer1.elapsed())
                    this.timer1.reset();
                this.getBedTask = getBedTask(this.mod);
                return this.getBedTask;
            }
            this.getBedTask = null;
            if (!itemStorage.hasItem(new Item[]{Items.WATER_BUCKET})) {
                setDebugState("Getting water bucket.");
                return (Task) TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
            }
            if (!itemStorage.hasItem(new Item[]{Items.FLINT_AND_STEEL})) {
                setDebugState("Getting flint and steel.");
                return (Task) TaskCatalogue.getItemTask(Items.FLINT_AND_STEEL, 1);
            }
            if (needsBuildingMaterials(this.mod)) {
                setDebugState("Collecting building materials.");
                return this.buildMaterialsTask;
            }
            if (!endPortalFound(this.mod, this.endPortalCenterLocation)) {
                setDebugState("Locating End Portal...");
                return (Task) locateStrongholdTask;
            }
            if (StorageHelper.miningRequirementMetInventory(controller, MiningRequirement.WOOD)) {
                Optional<BlockPos> silverfish = this.mod.getBlockScanner().getNearestBlock(blockPos -> WorldHelper.getSpawnerEntity(mod, blockPos) instanceof net.minecraft.entity.mob.SilverfishEntity, new Block[]{Blocks.SPAWNER});
                if (silverfish.isPresent()) {
                    setDebugState("Breaking silverfish spawner.");
                    return (Task) new DestroyBlockTask(silverfish.get());
                }
            }
            if (endPortalOpened(this.mod, this.endPortalCenterLocation)) {
                openingEndPortal = false;
                if (needsBuildingMaterials(this.mod)) {
                    setDebugState("Collecting building materials.");
                    return this.buildMaterialsTask;
                }
                if (config.placeSpawnNearEndPortal && itemStorage.hasItem(ItemHelper.BED) && !spawnSetNearPortal(this.mod, this.endPortalCenterLocation)) {
                    setDebugState("Setting spawn near end portal");
                    return setSpawnNearPortalTask(this.mod);
                }
                setDebugState("Entering End");
                this.enterindEndPortal = true;
                if (!this.mod.getExtraBaritoneSettings().isCanWalkOnEndPortal())
                    this.mod.getExtraBaritoneSettings().canWalkOnEndPortal(true);
                return (Task) new DoToClosestBlockTask(blockPos -> new GetToBlockTask(blockPos.up()), new Block[]{Blocks.END_PORTAL});
            }
            if (!itemStorage.hasItem(new Item[]{Items.OBSIDIAN})) {
                if (this.mod.getBlockScanner().anyFoundWithinDistance(10.0D, new Block[]{Blocks.OBSIDIAN}) || this.mod.getEntityTracker().itemDropped(new Item[]{Items.OBSIDIAN})) {
                    if (!itemStorage.hasItem(new Item[]{Items.WATER_BUCKET}))
                        return (Task) new CollectBucketLiquidTask.CollectWaterBucketTask(1);
                    if (!this.waterPlacedTimer.elapsed()) {
                        setDebugState("waitin " + this.waterPlacedTimer.getDuration());
                        return null;
                    }
                    return (Task) TaskCatalogue.getItemTask(Items.OBSIDIAN, 1);
                }
                if (this.repeated > 2 && !itemStorage.hasItem(new Item[]{Items.WATER_BUCKET}))
                    return (Task) new CollectBucketLiquidTask.CollectWaterBucketTask(1);
                if (this.waterPlacedTimer.elapsed()) {
                    if (!itemStorage.hasItem(new Item[]{Items.WATER_BUCKET})) {
                        this.repeated++;
                        this.waterPlacedTimer.reset();
                        return null;
                    }
                    this.repeated = 0;
                    return (Task) new PlaceObsidianBucketTask(this.mod
                            .getBlockScanner().getNearestBlock(WorldHelper.toVec3d(this.endPortalCenterLocation), blockPos -> !blockPos.isWithinDistance((Vec3i) this.endPortalCenterLocation, 8.0D), new Block[]{Blocks.LAVA}).get());
                }
                setDebugState("" + this.waterPlacedTimer.getDuration());
                return null;
            }
            setDebugState("Opening End Portal");
            openingEndPortal = true;
            return (Task) new DoToClosestBlockTask(blockPos -> new InteractWithBlockTask(Items.ENDER_EYE, blockPos), blockPos -> !isEndPortalFrameFilled(this.mod, blockPos), new Block[]{Blocks.END_PORTAL_FRAME});
        }
        if (WorldHelper.getCurrentDimension(mod) == Dimension.NETHER) {
            Item[] throwGearItems = {Items.STONE_SWORD, Items.STONE_PICKAXE, Items.IRON_SWORD, Items.IRON_PICKAXE};
            List<Slot> ironArmors = itemStorage.getSlotsWithItemPlayerInventory(true, COLLECT_IRON_ARMOR);
            List<Slot> throwGears = itemStorage.getSlotsWithItemPlayerInventory(true, throwGearItems);
            if ((itemStorage.hasItem(new Item[]{Items.FLINT_AND_STEEL}) || itemStorage.hasItem(new Item[]{Items.FIRE_CHARGE}))) {
                for (Slot throwGear : throwGears) {
                    if (Slot.isCursor(throwGear)) {
                        if (!this.mod.getControllerExtras().isBreakingBlock())
                            LookHelper.randomOrientation(controller);
                        this.mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                        continue;
                    }
                    this.mod.getSlotHandler().clickSlot(throwGear, 0, SlotActionType.PICKUP);
                }
                for (Slot ironArmor : ironArmors) {
                    if (Slot.isCursor(ironArmor)) {
                        if (!this.mod.getControllerExtras().isBreakingBlock())
                            LookHelper.randomOrientation(controller);
                        this.mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                        continue;
                    }
                    this.mod.getSlotHandler().clickSlot(ironArmor, 0, SlotActionType.PICKUP);
                }
            }
            setDebugState("Locating End Portal...");
            return (Task) locateStrongholdTask;
        }
        return null;
    }

    private Task setSpawnNearPortalTask(AltoClefController mod) {
        if (this.setBedSpawnTask.isSpawnSet()) {
            this.bedSpawnLocation = this.setBedSpawnTask.getBedSleptPos();
        } else {
            this.bedSpawnLocation = null;
        }
        if (isTaskRunning(mod, (Task) this.setBedSpawnTask)) {
            setDebugState("Setting spawnpoint now.");
            return (Task) this.setBedSpawnTask;
        }
        if (WorldHelper.inRangeXZ((Entity) mod.getPlayer(), WorldHelper.toVec3d(this.endPortalCenterLocation), 8.0D))
            return (Task) this.setBedSpawnTask;
        setDebugState("Approaching portal (to set spawnpoint)");
        return (Task) new GetToXZTask(this.endPortalCenterLocation.getX(), this.endPortalCenterLocation.getZ());
    }

    private Task getBlazeRodsTask(AltoClefController mod, int count) {
        EntityTracker entityTracker = mod.getEntityTracker();
        if (entityTracker.itemDropped(new Item[]{Items.BLAZE_ROD})) {
            Debug.logInternal("Blaze Rod dropped, picking it up.");
            return (Task) new PickupDroppedItemTask(Items.BLAZE_ROD, 1);
        }
        if (entityTracker.itemDropped(new Item[]{Items.BLAZE_POWDER})) {
            Debug.logInternal("Blaze Powder dropped, picking it up.");
            return (Task) new PickupDroppedItemTask(Items.BLAZE_POWDER, 1);
        }
        Debug.logInternal("No Blaze Rod or Blaze Powder dropped, collecting Blaze Rods.");
        return (Task) new CollectBlazeRodsTask(count);
    }

    private Task getEnderPearlTask(AltoClefController mod, int count) {
        if (mod.getEntityTracker().itemDropped(new Item[]{Items.ENDER_PEARL}))
            return (Task) new PickupDroppedItemTask(Items.ENDER_PEARL, 1);
        if (config.barterPearlsInsteadOfEndermanHunt) {
            if (!StorageHelper.isArmorEquipped(mod, new Item[]{Items.GOLDEN_HELMET}))
                return (Task) new EquipArmorTask(new Item[]{Items.GOLDEN_HELMET});
            return (Task) new TradeWithPiglinsTask(32, Items.ENDER_PEARL, count);
        }
        boolean endermanFound = mod.getEntityTracker().entityFound(new Class[]{EndermanEntity.class});
        boolean pearlDropped = mod.getEntityTracker().itemDropped(new Item[]{Items.ENDER_PEARL});
        if (endermanFound || pearlDropped) {
            Optional<Entity> toKill = mod.getEntityTracker().getClosestEntity(new Class[]{EndermanEntity.class});
            if (toKill.isPresent() && mod.getEntityTracker().isEntityReachable(toKill.get()))
                return (Task) new KillEndermanTask(count);
        }
        setDebugState("Waiting for endermen to spawn... ");
        return null;
    }

    private int getTargetBeds(AltoClefController mod) {
        boolean needsToSetSpawn = (config.placeSpawnNearEndPortal && !spawnSetNearPortal(mod, this.endPortalCenterLocation) && !isTaskRunning(mod, (Task) this.setBedSpawnTask));
        int bedsInEnd = Arrays.<Item>stream(ItemHelper.BED).mapToInt(bed -> ((Integer) this.cachedEndItemDrops.getOrDefault(bed, Integer.valueOf(0))).intValue()).sum();
        int targetBeds = config.requiredBeds + (needsToSetSpawn ? 1 : 0) - bedsInEnd;
        Debug.logInternal("needsToSetSpawn: " + needsToSetSpawn);
        Debug.logInternal("bedsInEnd: " + bedsInEnd);
        Debug.logInternal("targetBeds: " + targetBeds);
        return targetBeds;
    }

    private boolean needsBeds(AltoClefController mod) {
        int totalEndItems = 0;
        for (Item bed : ItemHelper.BED)
            totalEndItems += ((Integer) this.cachedEndItemDrops.getOrDefault(bed, Integer.valueOf(0))).intValue();
        int itemCount = mod.getItemStorage().getItemCount(ItemHelper.BED);
        int targetBeds = getTargetBeds(mod);
        Debug.logInternal("Total End Items: " + totalEndItems);
        Debug.logInternal("Item Count: " + itemCount);
        Debug.logInternal("Target Beds: " + targetBeds);
        boolean needsBeds = (itemCount + totalEndItems < targetBeds);
        Debug.logInternal("Needs Beds: " + needsBeds);
        return needsBeds;
    }

    private Task getBedTask(AltoClefController mod) {
        int targetBeds = getTargetBeds(mod);
        if (!mod.getItemStorage().hasItem(new Item[]{Items.SHEARS}) && !anyBedsFound(mod)) {
            Debug.logInternal("Getting shears.");
            return (Task) TaskCatalogue.getItemTask(Items.SHEARS, 1);
        }
        Debug.logInternal("Getting beds.");
        return (Task) TaskCatalogue.getItemTask("bed", targetBeds);
    }

    private boolean anyBedsFound(AltoClefController mod) {
        BlockScanner blockTracker = mod.getBlockScanner();
        EntityTracker entityTracker = mod.getEntityTracker();
        boolean bedsFoundInBlocks = blockTracker.anyFound(ItemHelper.itemsToBlocks(ItemHelper.BED));
        boolean bedsFoundInEntities = entityTracker.itemDropped(ItemHelper.BED);
        if (bedsFoundInBlocks)
            Debug.logInternal("Beds found in blocks");
        if (bedsFoundInEntities)
            Debug.logInternal("Beds found in entities");
        return (bedsFoundInBlocks || bedsFoundInEntities);
    }

    private BlockPos doSimpleSearchForEndPortal(AltoClefController mod) {
        List<BlockPos> frames = mod.getBlockScanner().getKnownLocations(new Block[]{Blocks.END_PORTAL_FRAME});
        if (frames.size() >= 12) {
            Vec3d average = ((Vec3d) frames.stream().reduce(Vec3d.ZERO, (accum, bpos) -> accum.add((int) Math.round(bpos.getX() + 0.5D), (int) Math.round(bpos.getY() + 0.5D), (int) Math.round(bpos.getZ() + 0.5D)), Vec3d::add)).multiply(1.0D / frames.size());
            mod.log("Average Position: " + String.valueOf(average));
            return new BlockPos(new Vec3i((int) average.x, (int) average.y, (int) average.z));
        }
        Debug.logInternal("Not enough frames");
        return null;
    }

    private int getFilledPortalFrames(AltoClefController mod, BlockPos endPortalCenter) {
        if (endPortalCenter == null)
            return 0;
        List<BlockPos> frameBlocks = getFrameBlocks(mod, endPortalCenter);
        if (frameBlocks.stream().allMatch(blockPos -> mod.getChunkTracker().isChunkLoaded(blockPos)))
            this

                    .cachedFilledPortalFrames = frameBlocks.stream().mapToInt(blockPos -> {
                boolean isFilled = isEndPortalFrameFilled(mod, blockPos);
                if (isFilled) {
                    Debug.logInternal("Portal frame at " + String.valueOf(blockPos) + " is filled.");
                } else {
                    Debug.logInternal("Portal frame at " + String.valueOf(blockPos) + " is not filled.");
                }
                return isFilled ? 1 : 0;
            }).sum();
        return this.cachedFilledPortalFrames;
    }

    private boolean canBeLootablePortalChest(AltoClefController mod, BlockPos blockPos) {
        return (mod.getWorld().getBlockState(blockPos.up()).getBlock() != Blocks.WATER && blockPos.getY() >= 50);
    }

    private Task getEyesOfEnderTask(AltoClefController mod, int targetEyes) {
        PriorityTask toGather;
        BlockPos pos;
        double maxPriority;
        ItemStorageTracker itemStorage;
        double rodDistance;
        ItemStorageTracker itemStorageTracker1;
        double pearlDistance;
        if (mod.getEntityTracker().itemDropped(new Item[]{Items.ENDER_EYE})) {
            setDebugState("Picking up Dropped Eyes");
            return (Task) new PickupDroppedItemTask(Items.ENDER_EYE, targetEyes);
        }
        int eyeCount = mod.getItemStorage().getItemCount(new Item[]{Items.ENDER_EYE});
        int blazePowderCount = mod.getItemStorage().getItemCount(new Item[]{Items.BLAZE_POWDER});
        int blazeRodCount = mod.getItemStorage().getItemCount(new Item[]{Items.BLAZE_ROD});
        int blazeRodTarget = (int) Math.ceil((targetEyes - eyeCount - blazePowderCount) / 2.0D);
        int enderPearlTarget = targetEyes - eyeCount;
        boolean needsBlazeRods = (blazeRodCount < blazeRodTarget);
        boolean needsBlazePowder = (eyeCount + blazePowderCount < targetEyes);
        boolean needsEnderPearls = (mod.getItemStorage().getItemCount(new Item[]{Items.ENDER_PEARL}) < enderPearlTarget);
        if (needsBlazePowder && !needsBlazeRods) {
            setDebugState("Crafting blaze powder");
            return (Task) TaskCatalogue.getItemTask(Items.BLAZE_POWDER, targetEyes - eyeCount);
        }
        if (!needsBlazePowder && !needsEnderPearls) {
            setDebugState("Crafting Ender Eyes");
            return (Task) TaskCatalogue.getItemTask(Items.ENDER_EYE, targetEyes);
        }
        switch (WorldHelper.getCurrentDimension(mod)) {
            case OVERWORLD:
                toGather = null;
                maxPriority = 0.0D;
                if (!this.gatherResources.isEmpty()) {
                    if (!this.forcedTaskTimer.elapsed() && isTaskRunning(mod, this.lastTask) && this.lastGather != null && this.lastGather.calculatePriority(mod) > 0.0D)
                        return this.lastTask;
                    if (!this.changedTaskTimer.elapsed() && this.lastTask != null && !this.lastGather.bypassForceCooldown && isTaskRunning(mod, this.lastTask))
                        return this.lastTask;
                    if (isTaskRunning(mod, this.lastTask) && this.lastGather != null && this.lastGather.shouldForce())
                        return this.lastTask;
                    for (PriorityTask gatherResource : this.gatherResources) {
                        double priority = gatherResource.calculatePriority(mod);
                        if (priority > maxPriority) {
                            maxPriority = priority;
                            toGather = gatherResource;
                        }
                    }
                }
                if (toGather != null) {
                    boolean sameTask = (this.lastGather == toGather);
                    setDebugState("Priority: " + String.format(Locale.US, "%.2f", new Object[]{Double.valueOf(maxPriority)}) + ", " + String.valueOf(toGather));
                    if (!sameTask && this.prevLastGather == toGather && this.lastTask != null && this.lastGather.calculatePriority(mod) > 0.0D && isTaskRunning(mod, this.lastTask)) {
                        mod.logWarning("might be stuck or switching too much, forcing current resource for a bit more");
                        this.changedTaskTimer.reset();
                        this.prevLastGather = null;
                        setDebugState("Priority: FORCED, " + String.valueOf(this.lastGather));
                        return this.lastTask;
                    }
                    if (sameTask && toGather.canCache())
                        return this.lastTask;
                    if (!sameTask)
                        this.taskChanges.add(0, new TaskChange(this.lastGather, toGather, mod.getPlayer().getBlockPos()));
                    if (this.taskChanges.size() >= 3 && !sameTask) {
                        TaskChange t1 = this.taskChanges.get(0);
                        TaskChange t2 = this.taskChanges.get(1);
                        TaskChange t3 = this.taskChanges.get(2);
                        if (t1.original == t2.interrupt && t1.pos.isWithinDistance((Vec3i) t3.pos, 5.0D) && t3.original == t1.interrupt) {
                            this.forcedTaskTimer.reset();
                            mod.logWarning("Probably stuck! Forcing timer...");
                            this.taskChanges.clear();
                            return this.lastTask;
                        }
                        if (this.taskChanges.size() > 3)
                            this.taskChanges.remove(this.taskChanges.size() - 1);
                    }
                    this.prevLastGather = this.lastGather;
                    this.lastGather = toGather;
                    Task task = toGather.getTask(mod);
                    if (!sameTask) {
                        if (this.lastTask instanceof SmeltInFurnaceTask && !(task instanceof SmeltInFurnaceTask) && !mod.getItemStorage().hasItem(new Item[]{Items.FURNACE})) {
                            this.pickupFurnace = true;
                            this.lastGather = null;
                            this.lastTask = null;
                            //StorageHelper.closeScreen();
                            return null;
                        }
                        if (this.lastTask instanceof SmeltInSmokerTask && !(task instanceof SmeltInSmokerTask) && !mod.getItemStorage().hasItem(new Item[]{Items.SMOKER})) {
                            this.pickupSmoker = true;
                            this.lastGather = null;
                            this.lastTask = null;
                            //StorageHelper.closeScreen();
                            return null;
                        }
                        if (this.lastTask != null && task != null && !toGather.needCraftingOnStart(mod)) {
                            this.pickupCrafting = true;
                            this.lastGather = null;
                            this.lastTask = null;
                            //StorageHelper.closeScreen();
                            return null;
                        }
                    }
                    this.lastTask = task;
                    this.changedTaskTimer.reset();
                    return task;
                }
                if (needsBuildingMaterials(mod)) {
                    setDebugState("Collecting building materials.");
                    return this.buildMaterialsTask;
                }
                setDebugState("Going to Nether");
                itemStorageTracker1 = mod.getItemStorage();
                if (itemStorageTracker1.getItemCount(new Item[]{Items.DIAMOND}) >= 3 && !itemStorageTracker1.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE}))
                    return (Task) TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
                if (itemStorageTracker1.getItemCount(new Item[]{Items.IRON_INGOT}) >= 3 && !itemStorageTracker1.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE}))
                    return (Task) TaskCatalogue.getItemTask(Items.IRON_PICKAXE, 1);
                if (!itemStorageTracker1.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE}))
                    return (Task) TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 1);
                if (!itemStorageTracker1.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE}))
                    return (Task) TaskCatalogue.getItemTask(Items.WOODEN_PICKAXE, 1);
                this.gatherResources.clear();
                if (!(this.lastTask instanceof DefaultGoToDimensionTask))
                    this.goToNetherTask = (Task) new DefaultGoToDimensionTask(Dimension.NETHER);
                this.lastTask = this.goToNetherTask;
                return this.goToNetherTask;
            case NETHER:
                if (isTaskRunning(mod, (Task) this.safeNetherPortalTask))
                    return (Task) this.safeNetherPortalTask;
                if (mod.getPlayer().getNetherPortalCooldown() != 0 && this.safeNetherPortalTask == null) {
                    this.safeNetherPortalTask = new SafeNetherPortalTask();
                    return (Task) this.safeNetherPortalTask;
                }
                mod.getInputControls().release(Input.MOVE_FORWARD);
                mod.getInputControls().release(Input.MOVE_LEFT);
                mod.getInputControls().release(Input.SNEAK);
                pos = mod.getPlayer().getSteppingPosition();
                if (!this.escaped && mod.getWorld().getBlockState(pos).getBlock().equals(Blocks.SOUL_SAND) && (mod
                        .getWorld().getBlockState(pos.east()).getBlock().equals(Blocks.OBSIDIAN) || mod
                        .getWorld().getBlockState(pos.west()).getBlock().equals(Blocks.OBSIDIAN) || mod
                        .getWorld().getBlockState(pos.south()).getBlock().equals(Blocks.OBSIDIAN) || mod
                        .getWorld().getBlockState(pos.north()).getBlock().equals(Blocks.OBSIDIAN))) {
                    LookHelper.lookAt(mod, pos);
                    mod.getInputControls().hold(Input.CLICK_LEFT);
                    return null;
                }
                if (!this.escaped) {
                    this.escaped = true;
                    mod.getInputControls().release(Input.CLICK_LEFT);
                }
                itemStorage = mod.getItemStorage();
                if (itemStorage.getItemCount(new Item[]{Items.DIAMOND}) >= 3 && !itemStorage.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE}))
                    return (Task) TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
                if (itemStorage.getItemCount(new Item[]{Items.IRON_INGOT}) >= 3 && !itemStorage.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE}))
                    return (Task) TaskCatalogue.getItemTask(Items.IRON_PICKAXE, 1);
                if (!itemStorage.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE}))
                    return (Task) TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 1);
                if (!itemStorage.hasItem(new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE}))
                    return (Task) TaskCatalogue.getItemTask(Items.WOODEN_PICKAXE, 1);
                if (mod.getItemStorage().getItemCount(new Item[]{Items.BLAZE_ROD}) * 2 + mod.getItemStorage().getItemCount(new Item[]{Items.BLAZE_POWDER}) + mod.getItemStorage().getItemCount(new Item[]{Items.ENDER_EYE}) >= 14)
                    this.hasRods = true;
                rodDistance = mod.getBlockScanner().distanceToClosest(new Block[]{Blocks.NETHER_BRICKS});
                pearlDistance = mod.getBlockScanner().distanceToClosest(new Block[]{Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WARPED_HYPHAE, Blocks.WARPED_NYLIUM});
                if (pearlDistance == Double.POSITIVE_INFINITY && rodDistance == Double.POSITIVE_INFINITY) {
                    setDebugState("Neither fortress or warped forest found... wandering");
                    if (isTaskRunning(mod, this.searchTask))
                        return this.searchTask;
                    this.searchTask = (Task) new SearchChunkForBlockTask(new Block[]{Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WARPED_HYPHAE, Blocks.WARPED_NYLIUM, Blocks.NETHER_BRICKS});
                    return this.searchTask;
                }
                if ((rodDistance < pearlDistance && !this.hasRods && !this.gettingPearls) || !needsEnderPearls) {
                    if (!this.gotToFortress)
                        if (mod.getBlockScanner().anyFoundWithinDistance(5.0D, new Block[]{Blocks.NETHER_BRICKS})) {
                            this.gotToFortress = true;
                        } else {
                            if (!mod.getBlockScanner().anyFound(new Block[]{Blocks.NETHER_BRICKS})) {
                                setDebugState("Searching for fortress");
                                return (Task) new TimeoutWanderTask();
                            }
                            if (WorldHelper.inRangeXZ(mod.getPlayer().getPos(),
                                    WorldHelper.toVec3d(mod.getBlockScanner().getNearestBlock(new Block[]{Blocks.NETHER_BRICKS}).get()), 2.0D)) {
                                setDebugState("trying to get to fortress");
                                return (Task) new GetToBlockTask(mod.getBlockScanner().getNearestBlock(new Block[]{Blocks.NETHER_BRICKS}).get());
                            }
                            setDebugState("Getting close to fortress");
                            if (((this.cachedFortressTask != null && !this.fortressTimer.elapsed() && mod
                                    .getPlayer().getPos().distanceTo(WorldHelper.toVec3d(this.cachedFortressTask.blockPos)) - 1.0D > this.prevPos.getManhattanDistance((Vec3i) this.cachedFortressTask.blockPos) / 2.0D) ||
                                    !mod.getBaritone().getPathingBehavior().isSafeToCancel()) &&
                                    this.cachedFortressTask != null) {
                                mod.log("" + mod.getPlayer().getPos().distanceTo(WorldHelper.toVec3d(this.cachedFortressTask.blockPos)) + " : " + mod.getPlayer().getPos().distanceTo(WorldHelper.toVec3d(this.cachedFortressTask.blockPos)));
                                return (Task) this.cachedFortressTask;
                            }
                            if (this.resetFortressTask) {
                                this.resetFortressTask = false;
                                return null;
                            }
                            this.resetFortressTask = true;
                            this.fortressTimer.reset();
                            mod.log("new");
                            this.prevPos = mod.getPlayer().getBlockPos();
                            BlockPos p = mod.getBlockScanner().getNearestBlock(new Block[]{Blocks.NETHER_BRICKS}).get();
                            int distance = (int) (mod.getPlayer().getPos().distanceTo(WorldHelper.toVec3d(p)) / 2.0D);
                            if (this.cachedFortressTask != null)
                                distance = Math.min(this.cachedFortressTask.range - 1, distance);
                            if (distance < 0) {
                                this.gotToFortress = true;
                            } else {
                                this.cachedFortressTask = new GetWithinRangeOfBlockTask(p, distance);
                                return (Task) this.cachedFortressTask;
                            }
                        }
                    setDebugState("Getting Blaze Rods");
                    return getBlazeRodsTask(mod, blazeRodTarget);
                }
                if (!mod.getBlockScanner().anyFound(new Block[]{Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WARPED_HYPHAE, Blocks.WARPED_NYLIUM}))
                    return (Task) new TimeoutWanderTask();
                if (!this.gotToBiome && (this.biomePos == null || !WorldHelper.inRangeXZ((Entity) mod.getPlayer(), this.biomePos, 30.0D) || !mod.getBaritone().getPathingBehavior().isSafeToCancel())) {
                    if (this.biomePos != null) {
                        setDebugState("Going to biome");
                        return (Task) new GetWithinRangeOfBlockTask(this.biomePos, 20);
                    }
                    this.gettingPearls = true;
                    setDebugState("Getting Ender Pearls");
                    Optional<BlockPos> closestBlock = mod.getBlockScanner().getNearestBlock(new Block[]{Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WARPED_HYPHAE, Blocks.WARPED_NYLIUM});
                    if (closestBlock.isPresent()) {
                        this.biomePos = closestBlock.get();
                    } else {
                        setDebugState("biome not found, wandering");
                    }
                    return (Task) new TimeoutWanderTask();
                }
                this.gotToBiome = true;
                return getEnderPearlTask(mod, enderPearlTarget);
            case END:
                throw new UnsupportedOperationException("You're in the end. Don't collect eyes here.");
        }
        return null;
    }

    private record TaskChange(PriorityTask original, PriorityTask interrupt, BlockPos pos) {
    }

    private class DistanceOrePriorityCalculator extends DistanceItemPriorityCalculator {

        private final Item oreItem;

        public DistanceOrePriorityCalculator(Item oreItem, double multiplier, double unneededMultiplier, double unneededDistanceThreshold, int minCount, int maxCount) {
            super(multiplier, unneededMultiplier, unneededDistanceThreshold, minCount, maxCount);
            this.oreItem = oreItem;
        }

        @Override
        public void update(int count) {
            super.update(getCountWithCraftedFromOre(mod, oreItem));
        }

    }
}
