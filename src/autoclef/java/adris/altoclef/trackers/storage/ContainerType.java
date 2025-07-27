//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package adris.altoclef.trackers.storage;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;

public enum ContainerType {
    CHEST,
    ENDER_CHEST,
    SHULKER,
    FURNACE,
    BREWING,
    MISC,
    EMPTY;

    ContainerType() {
    }

    public static ContainerType getFromBlock(Block block) {
        if (block instanceof ChestBlock) {
            return CHEST;
        } else if (block instanceof AbstractFurnaceBlock) {
            return FURNACE;
        } else if (block.equals(Blocks.ENDER_CHEST)) {
            return ENDER_CHEST;
        } else if (block instanceof ShulkerBoxBlock) {
            return SHULKER;
        } else if (block instanceof BrewingStandBlock) {
            return BREWING;
        } else {
            return !(block instanceof BarrelBlock) && !(block instanceof DispenserBlock) && !(block instanceof HopperBlock) ? EMPTY : MISC;
        }
    }
}
