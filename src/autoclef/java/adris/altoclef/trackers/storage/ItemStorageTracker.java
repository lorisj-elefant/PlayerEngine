package adris.altoclef.trackers.storage;

import adris.altoclef.AltoClefController;
import adris.altoclef.trackers.Tracker;
import adris.altoclef.trackers.TrackerManager;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemStorageTracker extends Tracker {

  private final InventorySubTracker inventory;
  public final ContainerSubTracker containers;

  public ItemStorageTracker(AltoClefController mod, TrackerManager manager, Consumer<ContainerSubTracker> containerTrackerConsumer) {
    super(manager);
    this .inventory = new InventorySubTracker(manager);
    this.containers = new ContainerSubTracker(manager);
    containerTrackerConsumer.accept(this.containers);
  }

  // --- Item Counting ---

  /**
   * Returns the total count of items the bot has access to in its own inventory, including the cursor.
   */
  public int getItemCount(Item... items) {
    return inventory.getItemCount(items);
  }

  public int getItemCount(ItemTarget... targets) {
    return Arrays.stream(targets).mapToInt(target -> getItemCount(target.getMatches())).sum();
  }

  /**
   * Returns the total count of items in the bot's inventory AND any currently open/cached container.
   * DEPRECATED on server: "Screen" is a client concept. This is an alias for getItemCount().
   */
  @Deprecated
  public int getItemCountScreen(Item... items) {
    return getItemCount(items);
  }

  /**
   * Alias for getItemCount.
   */
  public int getItemCountInventoryOnly(Item... items) {
    return getItemCount(items);
  }

  public boolean hasItemInventoryOnly(Item... items) {
    return inventory.hasItem(items);
  }

  // --- Item Checking ---

  public boolean hasItem(Item... items) {
    return inventory.hasItem(items);
  }

  public boolean hasItemAll(Item... items) {
    return Arrays.stream(items).allMatch(this::hasItem);
  }

  public boolean hasItem(ItemTarget... targets) {
    return Arrays.stream(targets).anyMatch(target -> hasItem(target.getMatches()));
  }

  public boolean hasItemInOffhand(AltoClefController controller, Item item) {
    ItemStack offhand = StorageHelper.getItemStackInSlot(new Slot(controller.getInventory().offHand, PlayerSlot.OFFHAND_SLOT_INDEX));
    return offhand.getItem() == item;
  }

  // --- Slot Access ---

  /**
   * Gets all slots in the player's inventory containing any of the specified items.
   * @param includeArmor Whether to include armor slots in the search.
   */
  public List<Slot> getSlotsWithItemPlayerInventory(boolean includeArmor, Item... items) {
    return inventory.getSlotsWithItemsPlayerInventory(includeArmor, items);
  }

  /**
   * Gets all item stacks from the player's inventory.
   * @param includeCursorSlot Whether to include the simulated cursor stack.
   */
  public List<ItemStack> getItemStacksPlayerInventory(boolean includeCursorSlot) {
    List<ItemStack> stacks = inventory.getInventoryStacks();
    if (includeCursorSlot) {
      stacks.add(0, mod.getSlotHandler().getCursorStack());
    }
    return stacks;
  }

  /**
   * Finds slots in the player's inventory that can fit the given stack.
   * @param stack The stack to fit.
   * @param acceptPartial Whether to accept slots that can only partially fit the stack.
   */
  public List<Slot> getSlotsThatCanFitInPlayerInventory(ItemStack stack, boolean acceptPartial) {
    return inventory.getSlotsThatCanFit(stack, acceptPartial);
  }

  public Optional<Slot> getSlotThatCanFitInPlayerInventory(ItemStack stack, boolean acceptPartial) {
    return getSlotsThatCanFitInPlayerInventory(stack, acceptPartial).stream().findFirst();
  }

  public boolean hasEmptyInventorySlot() {
    return inventory.hasEmptySlot();
  }

  // --- Container Tracking ---

  public boolean hasItemContainer(Predicate<ContainerCache> accept, Item... items) {
    return containers.getCachedContainers(accept).stream().anyMatch(cache -> cache.hasItem(items));
  }

  public Optional<ContainerCache> getContainerAtPosition(BlockPos pos) {
    return containers.getContainerAtPosition(pos);
  }

  public List<ContainerCache> getContainersWithItem(Item... items) {
    return containers.getContainersWithItem(items);
  }

  public Optional<ContainerCache> getClosestContainerWithItem(Vec3d pos, Item... items) {
    return containers.getCachedContainers(c -> c.hasItem(items)).stream()
            .min(Comparator.comparingDouble(c -> c.getBlockPos().getSquaredDistance(new Vec3i((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()))));
  }

  public Optional<BlockPos> getLastBlockPosInteraction() {
    return containers.getLastInteractedContainer();
  }

  // --- State Management ---

  public void registerSlotAction() {
    inventory.setDirty();
  }

  @Override
  protected void updateState() {
    inventory.ensureUpdated();
    containers.ensureUpdated();
  }

  @Override
  protected void reset() {
    inventory.reset();
    containers.reset();
  }
}