package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.container.LootContainerTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RavageRuinedPortalsTask extends Task {
    public final Item[] LOOT = new Item[]{
            Items.IRON_NUGGET, Items.FLINT, Items.OBSIDIAN, Items.FIRE_CHARGE, Items.FLINT_AND_STEEL, Items.GOLD_NUGGET, Items.GOLDEN_APPLE, Items.GOLDEN_AXE, Items.GOLDEN_HOE, Items.GOLDEN_PICKAXE,
            Items.GOLDEN_SHOVEL, Items.GOLDEN_SWORD, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, Items.GLISTERING_MELON_SLICE, Items.GOLDEN_CARROT, Items.GOLD_INGOT, Items.CLOCK,
            Items.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.GOLDEN_HORSE_ARMOR, Items.GOLD_BLOCK, Items.BELL, Items.ENCHANTED_GOLDEN_APPLE};

    private List<BlockPos> notRuinedPortalChests = new ArrayList<>();

    private Task lootTask;

    protected void onStart() {
        controller.getBehaviour().push();
    }

    protected Task onTick() {
        if (this.lootTask != null && this.lootTask.isActive() && !this.lootTask.isFinished())
            return this.lootTask;
        Optional<BlockPos> closest = locateClosestUnopenedRuinedPortalChest(controller);
        if (closest.isPresent()) {
            this.lootTask = (Task) new LootContainerTask(closest.get(), List.of(this.LOOT));
            return this.lootTask;
        }
        return (Task) new TimeoutWanderTask();
    }

    protected void onStop(Task task) {
        controller.getBehaviour().pop();
    }

    protected boolean isEqual(Task task) {
        return task instanceof adris.altoclef.tasks.misc.RavageRuinedPortalsTask;
    }

    public boolean isFinished() {
        return false;
    }

    protected String toDebugString() {
        return "Ravaging Ruined Portals";
    }

    private boolean canBeLootablePortalChest(AltoClefController mod, BlockPos blockPos) {
        if (mod.getWorld().getBlockState(blockPos.up(1)).getBlock() == Blocks.WATER || blockPos.getY() < 50)
            return false;
        for (BlockPos check : WorldHelper.scanRegion(blockPos.add(-4, -2, -4), blockPos.add(4, 2, 4))) {
            if (mod.getWorld().getBlockState(check).getBlock() == Blocks.NETHERRACK)
                return true;
        }
        this.notRuinedPortalChests.add(blockPos);
        return false;
    }

    private Optional<BlockPos> locateClosestUnopenedRuinedPortalChest(AltoClefController mod) {
        if (WorldHelper.getCurrentDimension(controller) != Dimension.OVERWORLD)
            return Optional.empty();
        return mod.getBlockScanner().getNearestBlock(blockPos -> (!this.notRuinedPortalChests.contains(blockPos) && WorldHelper.isUnopenedChest(controller, blockPos) && canBeLootablePortalChest(mod, blockPos)), new Block[]{Blocks.CHEST});
    }
}
