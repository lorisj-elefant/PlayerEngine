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

package baritone.api.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

import java.util.List;
import java.util.function.Predicate;

public class LivingEntityInventory implements Inventory, Nameable {
    public static final int ITEM_USAGE_COOLDOWN = 5;
    public static final int MAIN_SIZE = 36;
    private static final int HOTBAR_SIZE = 9;
    public static final int OFF_HAND_SLOT = 40;
    public static final int NOT_FOUND = -1;
    public static final int[] ARMOR_SLOTS = new int[]{0, 1, 2, 3};
    public static final int[] HELMET_SLOTS = new int[]{3};
    public final DefaultedList<ItemStack> main;
    public final DefaultedList<ItemStack> armor;
    public final DefaultedList<ItemStack> offHand;
    private final List<DefaultedList<ItemStack>> combinedInventory;
    public int selectedSlot;
    public LivingEntity player;
    private int changeCount;

    public LivingEntityInventory(LivingEntity player) {
        this.main = DefaultedList.ofSize(36, ItemStack.EMPTY);
        this.armor = DefaultedList.ofSize(4, ItemStack.EMPTY);
        this.offHand = DefaultedList.ofSize(1, ItemStack.EMPTY);
        this.combinedInventory = ImmutableList.of(this.main, this.armor, this.offHand);
        this.player = player;
    }

    public ItemStack getMainHandStack() {
        return isValidHotbarIndex(this.selectedSlot) ? (ItemStack) this.main.get(this.selectedSlot) : ItemStack.EMPTY;
    }

    public static int getHotbarSize() {
        return 9;
    }

    private boolean canStackAddMore(ItemStack existingStack, ItemStack stack) {
        return !existingStack.isEmpty() && ItemStack.canCombine(existingStack, stack) && existingStack.isStackable() && existingStack.getCount() < existingStack.getMaxCount() && existingStack.getCount() < this.getMaxCountPerStack();
    }

    public int getEmptySlot() {
        for (int i = 0; i < this.main.size(); ++i) {
            if (((ItemStack) this.main.get(i)).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    public void addPickBlock(ItemStack stack) {
        int i = this.getSlotWithStack(stack);
        if (isValidHotbarIndex(i)) {
            this.selectedSlot = i;
        } else {
            if (i == -1) {
                this.selectedSlot = this.getSwappableHotbarSlot();
                if (!((ItemStack) this.main.get(this.selectedSlot)).isEmpty()) {
                    int j = this.getEmptySlot();
                    if (j != -1) {
                        this.main.set(j, (ItemStack) this.main.get(this.selectedSlot));
                    }
                }

                this.main.set(this.selectedSlot, stack);
            } else {
                this.swapSlotWithHotbar(i);
            }

        }
    }

    public void swapSlotWithHotbar(int slot) {
        this.selectedSlot = this.getSwappableHotbarSlot();
        ItemStack itemStack = (ItemStack) this.main.get(this.selectedSlot);
        this.main.set(this.selectedSlot, (ItemStack) this.main.get(slot));
        this.main.set(slot, itemStack);
    }

    public static boolean isValidHotbarIndex(int slot) {
        return slot >= 0 && slot < 9;
    }

    public int getSlotWithStack(ItemStack stack) {
        for (int i = 0; i < this.main.size(); ++i) {
            if (!((ItemStack) this.main.get(i)).isEmpty() && ItemStack.canCombine(stack, (ItemStack) this.main.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public int indexOf(ItemStack stack) {
        for (int i = 0; i < this.main.size(); ++i) {
            ItemStack itemStack = (ItemStack) this.main.get(i);
            if (!((ItemStack) this.main.get(i)).isEmpty() && ItemStack.canCombine(stack, (ItemStack) this.main.get(i)) && !((ItemStack) this.main.get(i)).isDamaged() && !itemStack.hasEnchantments() && !itemStack.hasCustomName()) {
                return i;
            }
        }

        return -1;
    }

    public int getSwappableHotbarSlot() {
        for (int i = 0; i < 9; ++i) {
            int j = (this.selectedSlot + i) % 9;
            if (((ItemStack) this.main.get(j)).isEmpty()) {
                return j;
            }
        }

        for (int i = 0; i < 9; ++i) {
            int j = (this.selectedSlot + i) % 9;
            if (!((ItemStack) this.main.get(j)).hasEnchantments()) {
                return j;
            }
        }

        return this.selectedSlot;
    }

    public void scrollInHotbar(double scrollAmount) {
        int i = (int) Math.signum(scrollAmount);

        for (this.selectedSlot -= i; this.selectedSlot < 0; this.selectedSlot += 9) {
        }

        while (this.selectedSlot >= 9) {
            this.selectedSlot -= 9;
        }

    }

    public int remove(Predicate<ItemStack> shouldRemove, int maxCount, Inventory craftingInventory) {
        int i = 0;
        boolean bl = maxCount == 0;
        i += Inventories.remove(this, shouldRemove, maxCount - i, bl);
        i += Inventories.remove(craftingInventory, shouldRemove, maxCount - i, bl);
//        ItemStack itemStack = this.player.currentScreenHandler.getCursorStack();
//        i += Inventories.remove(itemStack, shouldRemove, maxCount - i, bl);
//        if (itemStack.isEmpty()) {
//            this.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
//        }

        return i;
    }

    private int addStack(ItemStack stack) {
        int i = this.getOccupiedSlotWithRoomForStack(stack);
        if (i == -1) {
            i = this.getEmptySlot();
        }

        return i == -1 ? stack.getCount() : this.addStack(i, stack);
    }

    private int addStack(int slot, ItemStack stack) {
        Item item = stack.getItem();
        int i = stack.getCount();
        ItemStack itemStack = this.getStack(slot);
        if (itemStack.isEmpty()) {
            itemStack = new ItemStack(item, 0);
            if (stack.hasNbt()) {
                itemStack.setNbt(stack.getNbt().copy());
            }

            this.setStack(slot, itemStack);
        }

        int j = i;
        if (i > itemStack.getMaxCount() - itemStack.getCount()) {
            j = itemStack.getMaxCount() - itemStack.getCount();
        }

        if (j > this.getMaxCountPerStack() - itemStack.getCount()) {
            j = this.getMaxCountPerStack() - itemStack.getCount();
        }

        if (j == 0) {
            return i;
        } else {
            i -= j;
            itemStack.increment(j);
            itemStack.setCooldown(5);
            return i;
        }
    }

    public int getOccupiedSlotWithRoomForStack(ItemStack stack) {
        if (this.canStackAddMore(this.getStack(this.selectedSlot), stack)) {
            return this.selectedSlot;
        } else if (this.canStackAddMore(this.getStack(40), stack)) {
            return 40;
        } else {
            for (int i = 0; i < this.main.size(); ++i) {
                if (this.canStackAddMore((ItemStack) this.main.get(i), stack)) {
                    return i;
                }
            }

            return -1;
        }
    }

    public void updateItems() {
        for (DefaultedList<ItemStack> defaultedList : this.combinedInventory) {
            for (int i = 0; i < defaultedList.size(); ++i) {
                if (!((ItemStack) defaultedList.get(i)).isEmpty()) {
                    ((ItemStack) defaultedList.get(i)).inventoryTick(this.player.getWorld(), this.player, i, this.selectedSlot == i);
                }
            }
        }

    }

    public boolean insertStack(ItemStack stack) {
        return this.insertStack(-1, stack);
    }

    public boolean insertStack(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else {
            try {
                if (stack.isDamaged()) {
                    if (slot == -1) {
                        slot = this.getEmptySlot();
                    }

                    if (slot >= 0) {
                        this.main.set(slot, stack.copyAndClear());
                        ((ItemStack) this.main.get(slot)).setCooldown(5);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;
                    do {
                        i = stack.getCount();
                        if (slot == -1) {
                            stack.setCount(this.addStack(stack));
                        } else {
                            stack.setCount(this.addStack(slot, stack));
                        }
                    } while (!stack.isEmpty() && stack.getCount() < i);

                    return stack.getCount() < i;
                }
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.create(throwable, "Adding item to inventory");
                CrashReportSection crashReportSection = crashReport.addElement("Item being added");
                crashReportSection.add("Item ID", Item.getRawId(stack.getItem()));
                crashReportSection.add("Item data", stack.getDamage());
                crashReportSection.add("Item name", () -> stack.getName().getString());
                throw new CrashException(crashReport);
            }
        }
    }

    public ItemStack removeStack(int slot, int amount) {
        List<ItemStack> list = null;

        for (DefaultedList<ItemStack> defaultedList : this.combinedInventory) {
            if (slot < defaultedList.size()) {
                list = defaultedList;
                break;
            }

            slot -= defaultedList.size();
        }

        return list != null && !((ItemStack) list.get(slot)).isEmpty() ? Inventories.splitStack(list, slot, amount) : ItemStack.EMPTY;
    }

    public void removeOne(ItemStack stack) {
        for (DefaultedList<ItemStack> defaultedList : this.combinedInventory) {
            for (int i = 0; i < defaultedList.size(); ++i) {
                if (defaultedList.get(i) == stack) {
                    defaultedList.set(i, ItemStack.EMPTY);
                    break;
                }
            }
        }

    }

    public ItemStack removeStack(int slot) {
        DefaultedList<ItemStack> defaultedList = null;

        for (DefaultedList<ItemStack> defaultedList2 : this.combinedInventory) {
            if (slot < defaultedList2.size()) {
                defaultedList = defaultedList2;
                break;
            }

            slot -= defaultedList2.size();
        }

        if (defaultedList != null && !((ItemStack) defaultedList.get(slot)).isEmpty()) {
            ItemStack itemStack = (ItemStack) defaultedList.get(slot);
            defaultedList.set(slot, ItemStack.EMPTY);
            return itemStack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public void setStack(int slot, ItemStack stack) {
        DefaultedList<ItemStack> defaultedList = null;

        for (DefaultedList<ItemStack> defaultedList2 : this.combinedInventory) {
            if (slot < defaultedList2.size()) {
                defaultedList = defaultedList2;
                break;
            }

            slot -= defaultedList2.size();
        }

        if (defaultedList != null) {
            defaultedList.set(slot, stack);
        }

    }

    public float getBlockBreakingSpeed(BlockState block) {
        return ((ItemStack) this.main.get(this.selectedSlot)).getMiningSpeedMultiplier(block);
    }

    public NbtList writeNbt(NbtList nbtList) {
        for (int i = 0; i < this.main.size(); ++i) {
            if (!((ItemStack) this.main.get(i)).isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) i);
                ((ItemStack) this.main.get(i)).writeNbt(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }

        for (int i = 0; i < this.armor.size(); ++i) {
            if (!((ItemStack) this.armor.get(i)).isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) (i + 100));
                ((ItemStack) this.armor.get(i)).writeNbt(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }

        for (int i = 0; i < this.offHand.size(); ++i) {
            if (!((ItemStack) this.offHand.get(i)).isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) (i + 150));
                ((ItemStack) this.offHand.get(i)).writeNbt(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }

        return nbtList;
    }

    public void readNbt(NbtList nbtList) {
        this.main.clear();
        this.armor.clear();
        this.offHand.clear();

        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            int j = nbtCompound.getByte("Slot") & 255;
            ItemStack itemStack = ItemStack.fromNbt(nbtCompound);
            if (!itemStack.isEmpty()) {
                if (j >= 0 && j < this.main.size()) {
                    this.main.set(j, itemStack);
                } else if (j >= 100 && j < this.armor.size() + 100) {
                    this.armor.set(j - 100, itemStack);
                } else if (j >= 150 && j < this.offHand.size() + 150) {
                    this.offHand.set(j - 150, itemStack);
                }
            }
        }

    }

    public int size() {
        return this.main.size() + this.armor.size() + this.offHand.size();
    }

    public boolean isEmpty() {
        for (ItemStack itemStack : this.main) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }

        for (ItemStack itemStack : this.armor) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }

        for (ItemStack itemStack : this.offHand) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public ItemStack getStack(int slot) {
        List<ItemStack> list = null;

        for (DefaultedList<ItemStack> defaultedList : this.combinedInventory) {
            if (slot < defaultedList.size()) {
                list = defaultedList;
                break;
            }

            slot -= defaultedList.size();
        }

        return list == null ? ItemStack.EMPTY : (ItemStack) list.get(slot);
    }

    public Text getName() {
        return Text.translatable("container.inventory");
    }

    public ItemStack getArmorStack(int slot) {
        return (ItemStack) this.armor.get(slot);
    }

    public void damageArmor(DamageSource damageSource, float amount, int[] slots) {
        if (!(amount <= 0.0F)) {
            amount /= 4.0F;
            if (amount < 1.0F) {
                amount = 1.0F;
            }

            for (int i : slots) {
                ItemStack itemStack = (ItemStack) this.armor.get(i);
                if ((!damageSource.isTypeIn(DamageTypeTags.IS_FIRE) || !itemStack.getItem().isFireproof()) && itemStack.getItem() instanceof ArmorItem) {
                    itemStack.damage((int) amount, this.player, (player) -> player.sendEquipmentBreakStatus(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i)));
                }
            }

        }
    }

    public void dropAll() {
        for (List<ItemStack> list : this.combinedInventory) {
            for (int i = 0; i < list.size(); ++i) {
                ItemStack itemStack = (ItemStack) list.get(i);
                if (!itemStack.isEmpty()) {
                    this.player.dropStack(itemStack);
                    list.set(i, ItemStack.EMPTY);
                }
            }
        }

    }

    public void markDirty() {
        ++this.changeCount;
    }

    public int getChangeCount() {
        return this.changeCount;
    }

    public boolean canPlayerUse(PlayerEntity player) {
        if (this.player.isRemoved()) {
            return false;
        } else {
            return !(player.squaredDistanceTo(this.player) > (double) 64.0F);
        }
    }

    public boolean contains(ItemStack stack) {
        for (List<ItemStack> list : this.combinedInventory) {
            for (ItemStack itemStack : list) {
                if (!itemStack.isEmpty() && ItemStack.canCombine(itemStack, stack)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean contains(TagKey<Item> key) {
        for (List<ItemStack> list : this.combinedInventory) {
            for (ItemStack itemStack : list) {
                if (!itemStack.isEmpty() && itemStack.isIn(key)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void clone(LivingEntityInventory other) {
        for (int i = 0; i < this.size(); ++i) {
            this.setStack(i, other.getStack(i));
        }

        this.selectedSlot = other.selectedSlot;
    }

    public void clear() {
        for (List<ItemStack> list : this.combinedInventory) {
            list.clear();
        }

    }

    public void populateRecipeFinder(RecipeMatcher finder) {
        for (ItemStack itemStack : this.main) {
            finder.addUnenchantedInput(itemStack);
        }

    }

    public ItemStack dropSelectedItem(boolean entireStack) {
        ItemStack itemStack = this.getMainHandStack();
        return itemStack.isEmpty() ? ItemStack.EMPTY : this.removeStack(this.selectedSlot, entireStack ? itemStack.getCount() : 1);
    }
}
