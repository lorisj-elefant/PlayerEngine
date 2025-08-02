package adris.altoclef.tasks.misc;

import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.container.LootContainerTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public class LootDesertTempleTask extends Task {
    public final Vec3i[] CHEST_POSITIONS_RELATIVE = new Vec3i[]{new Vec3i(2, 0, 0), new Vec3i(-2, 0, 0), new Vec3i(0, 0, 2), new Vec3i(0, 0, -2)};

    private final BlockPos temple;

    private final List<Item> wanted;

    private Task lootTask;

    private short looted = 0;

    public LootDesertTempleTask(BlockPos temple, List<Item> wanted) {
        this.temple = temple;
        this.wanted = wanted;
    }

    protected void onStart() {
        ((List<Block>) (controller.getBaritoneSettings()).blocksToAvoid.get()).add(Blocks.STONE_PRESSURE_PLATE);
    }

    protected Task onTick() {
        if (this.lootTask != null) {
            if (!this.lootTask.isFinished()) {
                setDebugState("Looting a desert temple chest");
                return this.lootTask;
            }
            this.looted = (short) (this.looted + 1);
        }
        if (controller.getWorld().getBlockState(this.temple).getBlock() == Blocks.STONE_PRESSURE_PLATE) {
            setDebugState("Breaking pressure plate");
            return (Task) new DestroyBlockTask(this.temple);
        }
        if (this.looted < 4) {
            setDebugState("Looting a desert temple chest");
            this.lootTask = (Task) new LootContainerTask(this.temple.add(this.CHEST_POSITIONS_RELATIVE[this.looted]), this.wanted);
            return this.lootTask;
        }
        setDebugState("Why is this still running? Report this");
        return null;
    }

    protected void onStop(Task task) {
        ((List) (controller.getBaritoneSettings()).blocksToAvoid.get()).remove(Blocks.STONE_PRESSURE_PLATE);
    }

    protected boolean isEqual(Task other) {
        return (other instanceof adris.altoclef.tasks.misc.LootDesertTempleTask && ((adris.altoclef.tasks.misc.LootDesertTempleTask) other).getTemplePos() == this.temple);
    }

    public boolean isFinished() {
        return (this.looted == 4);
    }

    protected String toDebugString() {
        return "Looting Desert Temple";
    }

    public BlockPos getTemplePos() {
        return this.temple;
    }
}
