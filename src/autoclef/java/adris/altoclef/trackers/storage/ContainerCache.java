package adris.altoclef.trackers.storage;

import adris.altoclef.util.Dimension;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.function.Consumer;

public class ContainerCache {
    private final BlockPos blockPos;

    private final Dimension dimension;

    private final ContainerType containerType;

    private final HashMap<Item, Integer> itemCounts = new HashMap<>();

    private int emptySlots;

    public ContainerCache(Dimension dimension, BlockPos blockPos, ContainerType containerType) {
        this.dimension = dimension;
        this.blockPos = blockPos;
        this.containerType = containerType;
    }

    public void update(Inventory screenHandler, Consumer<ItemStack> onStack) {
        this.itemCounts.clear();
        this.emptySlots = 0;
        int start = 0;
        int end = screenHandler.size();
        boolean isFurnace = screenHandler instanceof net.minecraft.screen.FurnaceScreenHandler;
        for (int i = start; i < end; i++) {
            ItemStack stack = screenHandler.getStack(i).copy();
            if (stack.isEmpty()) {
                if (!isFurnace || i != 2)
                    this.emptySlots++;
            } else {
                Item item = stack.getItem();
                int count = stack.getCount();
                this.itemCounts.put(item, Integer.valueOf(((Integer) this.itemCounts.getOrDefault(item, Integer.valueOf(0))).intValue() + count));
                onStack.accept(stack);
            }
        }
    }

    public int getItemCount(Item... items) {
        int result = 0;
        for (Item item : items)
            result += ((Integer) this.itemCounts.getOrDefault(item, Integer.valueOf(0))).intValue();
        return result;
    }

    public boolean hasItem(Item... items) {
        for (Item item : items) {
            if (this.itemCounts.containsKey(item) && ((Integer) this.itemCounts.get(item)).intValue() > 0)
                return true;
        }
        return false;
    }

    public int getEmptySlotCount() {
        return this.emptySlots;
    }

    public boolean isFull() {
        return (this.emptySlots == 0);
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public ContainerType getContainerType() {
        return this.containerType;
    }

    public Dimension getDimension() {
        return this.dimension;
    }
}
