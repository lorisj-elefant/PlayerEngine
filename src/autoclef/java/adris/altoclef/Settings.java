package adris.altoclef;

import adris.altoclef.control.KillAura;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.util.BlockRange;
import adris.altoclef.util.helpers.ConfigHelper;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.serialization.IFailableConfigFile;
import adris.altoclef.util.serialization.ItemDeserializer;
import adris.altoclef.util.serialization.ItemSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Streams;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Settings implements IFailableConfigFile {
    public static final String SETTINGS_PATH = "altoclef_settings.json";

    @JsonIgnore
    private transient boolean failedToLoad = false;

    private boolean showDebugTickMs = false;

    private boolean showTaskChains = true;

    private boolean hideAllWarningLogs = false;

    private String commandPrefix = "@";

    private String logLevel = "NORMAL";

    private String chatLogPrefix = "[Alto Clef] ";

    private boolean showTimer = true;

    private float containerItemMoveDelay = 0.2F;

    private boolean useCraftingBookToCraft = true;

    private float resourcePickupDropRange = 16.0F;

    private int minimumFoodAllowed = 0;

    private int foodUnitsToCollect = 0;

    private float resourceChestLocateRange = 500.0F;

    private float resourceMineRange = 100.0F;

    private boolean avoidSearchingDungeonChests = true;

    private boolean avoidOceanBlocks = false;

    private float entityReachRange = 4.0F;

    private boolean collectPickaxeFirst = true;

    private boolean replantCrops = true;

    private boolean mobDefense = true;

    private KillAura.Strategy forceFieldStrategy = KillAura.Strategy.SMART;

    private boolean dodgeProjectiles = true;

    private boolean killOrAvoidAnnoyingHostiles = true;

    private boolean avoidDrowning = true;

    private boolean autoCloseScreenWhenLookingOrMining = true;

    private boolean extinguishSelfWithWater = true;

    private boolean autoEat = true;

    private boolean autoMLGBucket = true;

    private boolean autoReconnect = true;

    private boolean autoRespawn = true;

    private DefaultGoToDimensionTask.OVERWORLD_TO_NETHER_BEHAVIOUR overworldToNetherBehaviour = DefaultGoToDimensionTask.OVERWORLD_TO_NETHER_BEHAVIOUR.BUILD_PORTAL_VANILLA;

    private int netherFastTravelWalkingRange = 600;

    private String idleCommand = "idle";

    private String deathCommand = "";

    @JsonSerialize(using = ItemSerializer.class)
    @JsonDeserialize(using = ItemDeserializer.class)
    private List<Item> throwawayItems = Arrays.asList(new Item[]{
            Items.DRIPSTONE_BLOCK, Items.ROOTED_DIRT, Items.GRAVEL, Items.SAND, Items.DIORITE, Items.ANDESITE, Items.GRANITE, Items.TUFF, Items.COBBLESTONE, Items.DIRT,
            Items.COBBLED_DEEPSLATE, Items.ACACIA_LEAVES, Items.BIRCH_LEAVES, Items.DARK_OAK_LEAVES, Items.OAK_LEAVES, Items.JUNGLE_LEAVES, Items.SPRUCE_LEAVES, Items.NETHERRACK, Items.MAGMA_BLOCK, Items.SOUL_SOIL,
            Items.SOUL_SAND, Items.NETHER_BRICKS, Items.NETHER_BRICK, Items.BASALT, Items.BLACKSTONE, Items.END_STONE, Items.SANDSTONE, Items.STONE_BRICKS});

    private int reservedBuildingBlockCount = 64;

    private boolean dontThrowAwayCustomNameItems = true;

    private boolean dontThrowAwayEnchantedItems = true;

    private boolean throwAwayUnusedItems = true;

    @JsonSerialize(using = ItemSerializer.class)
    @JsonDeserialize(using = ItemDeserializer.class)
    private List<Item> importantItems = Streams.concat(new Stream[]{Stream.of(new Item[]{Items.TOTEM_OF_UNDYING, Items.ENCHANTED_GOLDEN_APPLE, Items.ENDER_EYE, Items.TRIDENT, Items.DIAMOND, Items.DIAMOND_BLOCK, Items.NETHERITE_SCRAP, Items.NETHERITE_INGOT, Items.NETHERITE_BLOCK}), Stream.of(ItemHelper.DIAMOND_ARMORS),
            Stream.of(ItemHelper.NETHERITE_ARMORS),
            Stream.of(ItemHelper.DIAMOND_TOOLS),
            Stream.of(ItemHelper.NETHERITE_TOOLS),

            Stream.of(ItemHelper.SHULKER_BOXES)}).toList();

    private boolean limitFuelsToSupportedFuels = true;

    @JsonSerialize(using = ItemSerializer.class)
    @JsonDeserialize(using = ItemDeserializer.class)
    private List<Item> supportedFuels = Streams.concat(new Stream[]{Stream.of(new Item[]{Items.COAL, Items.CHARCOAL})}).toList();

    private BlockPos homeBasePosition = new BlockPos(0, 64, 0);

    private List<BlockRange> areasToProtect = Collections.emptyList();

    public static void load(Consumer<adris.altoclef.Settings> onReload) {
        ConfigHelper.loadConfig("altoclef_settings.json", adris.altoclef.Settings::new, adris.altoclef.Settings.class, onReload);
    }

    public boolean shouldShowTaskChain() {
        return this.showTaskChains;
    }

    public boolean shouldShowDebugTickMs() {
        return this.showDebugTickMs;
    }

    public boolean shouldHideAllWarningLogs() {
        return this.hideAllWarningLogs;
    }

    public String getLogLevel() {
        return this.logLevel;
    }

    public String getCommandPrefix() {
        return this.commandPrefix;
    }

    public String getChatLogPrefix() {
        return this.chatLogPrefix;
    }

    public boolean shouldShowTimer() {
        return this.showTimer;
    }

    public float getResourcePickupRange() {
        return this.resourcePickupDropRange;
    }

    public float getResourceChestLocateRange() {
        return this.resourceChestLocateRange;
    }

    public float getResourceMineRange() {
        return this.resourceMineRange;
    }

    public float getContainerItemMoveDelay() {
        return this.containerItemMoveDelay;
    }

    public boolean shouldUseCraftingBookToCraft() {
        return this.useCraftingBookToCraft;
    }

    public int getFoodUnitsToCollect() {
        return this.foodUnitsToCollect;
    }

    public int getMinimumFoodAllowed() {
        return this.minimumFoodAllowed;
    }

    public boolean isMobDefense() {
        return this.mobDefense;
    }

    public boolean isDodgeProjectiles() {
        return this.dodgeProjectiles;
    }

    public boolean isAutoEat() {
        return this.autoEat;
    }

    public boolean isAutoReconnect() {
        return this.autoReconnect;
    }

    public boolean isAutoRespawn() {
        return this.autoRespawn;
    }

    public boolean shouldReplantCrops() {
        return this.replantCrops;
    }

    public boolean shouldDealWithAnnoyingHostiles() {
        return this.killOrAvoidAnnoyingHostiles;
    }

    public KillAura.Strategy getForceFieldStrategy() {
        return this.forceFieldStrategy;
    }

    public String getIdleCommand() {
        return (this.idleCommand == "") ? "idle" : this.idleCommand;
    }

    public String getDeathCommand() {
        return this.deathCommand;
    }

    public boolean shouldRunIdleCommandWhenNotActive() {
        String command = getIdleCommand();
        return (command != null && !command.isBlank());
    }

    public boolean shouldAutoMLGBucket() {
        return this.autoMLGBucket;
    }

    public boolean shouldCollectPickaxeFirst() {
        return this.collectPickaxeFirst;
    }

    public boolean shouldAvoidDrowning() {
        return this.avoidDrowning;
    }

    public boolean shouldCloseScreenWhenLookingOrMining() {
        return this.autoCloseScreenWhenLookingOrMining;
    }

    public boolean shouldExtinguishSelfWithWater() {
        return this.extinguishSelfWithWater;
    }

    public boolean shouldAvoidSearchingForDungeonChests() {
        return this.avoidSearchingDungeonChests;
    }

    public boolean shouldAvoidOcean() {
        return this.avoidOceanBlocks;
    }

    public boolean isThrowaway(Item item) {
        return this.throwawayItems.contains(item);
    }

    public boolean isImportant(Item item) {
        return this.importantItems.contains(item);
    }

    public boolean shouldThrowawayUnusedItems() {
        return this.throwAwayUnusedItems;
    }

    public int getReservedBuildingBlockCount() {
        return this.reservedBuildingBlockCount;
    }

    public boolean getDontThrowAwayCustomNameItems() {
        return this.dontThrowAwayCustomNameItems;
    }

    public boolean getDontThrowAwayEnchantedItems() {
        return this.dontThrowAwayEnchantedItems;
    }

    public float getEntityReachRange() {
        return this.entityReachRange;
    }

    public Item[] getThrowawayItems(AltoClefController mod, boolean includeProtected) {
        return (Item[]) this.throwawayItems.stream().filter(item -> (includeProtected || !mod.getBehaviour().isProtected(item))).toArray(x$0 -> new Item[x$0]);
    }

    public Item[] getThrowawayItems(AltoClefController mod) {
        return getThrowawayItems(mod, false);
    }

    public boolean shouldLimitFuelsToSupportedFuels() {
        return this.limitFuelsToSupportedFuels;
    }

    public boolean isSupportedFuel(Item item) {
        return (!this.limitFuelsToSupportedFuels || this.supportedFuels.contains(item));
    }

    @JsonIgnore
    public Item[] getSupportedFuelItems() {
        return (Item[]) this.supportedFuels.toArray(x$0 -> new Item[x$0]);
    }

//  public boolean isPositionExplicitlyProtected(BlockPos pos) {
//    if (!this.areasToProtect.isEmpty())
//      for (BlockRange protection : this.areasToProtect) {
//        if (protection != null && protection.isValid() && protection.contains(pos))
//          return true;
//      }
//    return false;
//  }

    public DefaultGoToDimensionTask.OVERWORLD_TO_NETHER_BEHAVIOUR getOverworldToNetherBehaviour() {
        return this.overworldToNetherBehaviour;
    }

    public int getNetherFastTravelWalkingRange() {
        return this.netherFastTravelWalkingRange;
    }

    public BlockPos getHomeBasePosition() {
        return this.homeBasePosition;
    }

    public void onFailLoad() {
        this.failedToLoad = true;
    }

    public boolean failedToLoad() {
        return this.failedToLoad;
    }
}
