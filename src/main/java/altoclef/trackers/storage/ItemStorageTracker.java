/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package altoclef.trackers.storage;

import altoclef.AltoClefController;
import altoclef.trackers.Tracker;
import altoclef.trackers.TrackerManager;
import altoclef.util.ItemTarget;
import altoclef.util.helpers.StorageHelper;
import altoclef.util.slots.Slot;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ItemStorageTracker extends Tracker {
   private final InventorySubTracker inventory;
   public final ContainerSubTracker containers;

   public ItemStorageTracker(AltoClefController mod, TrackerManager manager, Consumer<ContainerSubTracker> containerTrackerConsumer) {
      super(manager);
      this.inventory = new InventorySubTracker(manager);
      this.containers = new ContainerSubTracker(manager);
      containerTrackerConsumer.accept(this.containers);
   }

   public int getItemCount(Item... items) {
      return this.inventory.getItemCount(items);
   }

   public int getItemCount(ItemTarget... targets) {
      return Arrays.stream(targets).mapToInt(target -> this.getItemCount(target.getMatches())).sum();
   }

   @Deprecated
   public int getItemCountScreen(Item... items) {
      return this.getItemCount(items);
   }

   public int getItemCountInventoryOnly(Item... items) {
      return this.getItemCount(items);
   }

   public boolean hasItemInventoryOnly(Item... items) {
      return this.inventory.hasItem(items);
   }

   public boolean hasItem(Item... items) {
      return this.inventory.hasItem(items);
   }

   public boolean hasItemAll(Item... items) {
      return Arrays.stream(items).allMatch(xva$0 -> this.hasItem(xva$0));
   }

   public boolean hasItem(ItemTarget... targets) {
      return Arrays.stream(targets).anyMatch(target -> this.hasItem(target.getMatches()));
   }

   public boolean hasItemInOffhand(AltoClefController controller, Item item) {
      ItemStack offhand = StorageHelper.getItemStackInSlot(new Slot(controller.getInventory().offHand, 0));
      return offhand.getItem() == item;
   }

   public List<Slot> getSlotsWithItemPlayerInventory(boolean includeArmor, Item... items) {
      return this.inventory.getSlotsWithItemsPlayerInventory(includeArmor, items);
   }

   public List<ItemStack> getItemStacksPlayerInventory(boolean includeCursorSlot) {
      List<ItemStack> stacks = this.inventory.getInventoryStacks();
      if (includeCursorSlot) {
         stacks.add(0, this.mod.getSlotHandler().getCursorStack());
      }

      return stacks;
   }

   public List<Slot> getSlotsThatCanFitInPlayerInventory(ItemStack stack, boolean acceptPartial) {
      return this.inventory.getSlotsThatCanFit(stack, acceptPartial);
   }

   public Optional<Slot> getSlotThatCanFitInPlayerInventory(ItemStack stack, boolean acceptPartial) {
      return this.getSlotsThatCanFitInPlayerInventory(stack, acceptPartial).stream().findFirst();
   }

   public boolean hasEmptyInventorySlot() {
      return this.inventory.hasEmptySlot();
   }

   public boolean hasItemContainer(Predicate<ContainerCache> accept, Item... items) {
      return this.containers.getCachedContainers(accept).stream().anyMatch(cache -> cache.hasItem(items));
   }

   public Optional<ContainerCache> getContainerAtPosition(BlockPos pos) {
      return this.containers.getContainerAtPosition(pos);
   }

   public List<ContainerCache> getContainersWithItem(Item... items) {
      return this.containers.getContainersWithItem(items);
   }

   public Optional<ContainerCache> getClosestContainerWithItem(Vec3 pos, Item... items) {
      return this.containers
         .getCachedContainers(c -> c.hasItem(items))
         .stream()
         .min(Comparator.comparingDouble(c -> c.getBlockPos().distSqr(new Vec3i((int)pos.x(), (int)pos.y(), (int)pos.z()))));
   }

   public Optional<BlockPos> getLastBlockPosInteraction() {
      return this.containers.getLastInteractedContainer();
   }

   public void registerSlotAction() {
      this.inventory.setDirty();
   }

   @Override
   protected void updateState() {
      this.inventory.ensureUpdated();
      this.containers.ensureUpdated();
   }

   @Override
   protected void reset() {
      this.inventory.reset();
      this.containers.reset();
   }
}
