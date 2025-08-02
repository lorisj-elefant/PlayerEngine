package adris.altoclef.multiversion.item;

import adris.altoclef.multiversion.FoodComponentWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemVer {
    public static FoodComponentWrapper getFoodComponent(Item item) {
        return FoodComponentWrapper.of(item.getFoodComponent());
    }

    public static boolean isFood(ItemStack stack) {
        return isFood(stack.getItem());
    }

    public static boolean hasCustomName(ItemStack stack) {
        return stack.hasCustomName();
    }

    public static boolean isFood(Item item) {
        return item.isFood();
    }

    private static boolean isSuitableFor(Item item, BlockState state) {
        return item.isSuitableFor(state);
    }

    private static Item RAW_GOLD() {
        return Items.RAW_GOLD;
    }

    private static Item RAW_IRON() {
        return Items.RAW_IRON;
    }
}
