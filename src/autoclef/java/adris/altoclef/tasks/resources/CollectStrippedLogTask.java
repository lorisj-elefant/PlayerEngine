package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class CollectStrippedLogTask extends ResourceTask {
    private static final Item[] axes = new Item[]{Items.WOODEN_AXE, Items.STONE_AXE, Items.GOLDEN_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE};

    private final Item[] strippedLogs;

    private final Item[] strippableLogs;

    private final int targetCount;

    public CollectStrippedLogTask(Item[] strippedLogs, Item[] strippableLogs, int count) {
        super(new ItemTarget(strippedLogs, count));
        this.strippedLogs = strippedLogs;
        this.strippableLogs = strippableLogs;
        this.targetCount = count;
    }

    public CollectStrippedLogTask(int count) {
        this(ItemHelper.STRIPPED_LOGS, ItemHelper.STRIPPABLE_LOGS, count);
    }

    public CollectStrippedLogTask(Item strippedLogs, Item strippableLogs, int count) {
        this(new Item[]{strippedLogs}, new Item[]{strippableLogs}, count);
    }

    public CollectStrippedLogTask(Item strippedLog, int count) {
        this(strippedLog, ItemHelper.strippedToLogs(strippedLog), count);
    }

    protected boolean shouldAvoidPickingUp(AltoClefController mod) {
        return false;
    }

    protected void onResourceStart(AltoClefController mod) {
    }

    protected Task onResourceTick(AltoClefController mod) {
        if (!mod.getItemStorage().hasItem(axes)) {
            setDebugState("Getting axe for stripping");
            return (Task) TaskCatalogue.getItemTask(Items.WOODEN_AXE, 1);
        }
        if (mod.getItemStorage().getItemCount(this.strippedLogs) < this.targetCount) {
            Optional<BlockPos> strippedLogBlockPos = mod.getBlockScanner().getNearestBlock(ItemHelper.itemsToBlocks(this.strippedLogs));
            if (strippedLogBlockPos.isPresent()) {
                setDebugState("Getting stripped log");
                return (Task) new MineAndCollectTask(new ItemTarget(this.strippedLogs), ItemHelper.itemsToBlocks(this.strippedLogs), MiningRequirement.HAND);
            }
        }
        Optional<BlockPos> strippableLogBlockPos = mod.getBlockScanner().getNearestBlock(ItemHelper.itemsToBlocks(this.strippableLogs));
        if (strippableLogBlockPos.isPresent()) {
            setDebugState("Stripping log");
            return (Task) new InteractWithBlockTask(new ItemTarget(axes), strippableLogBlockPos.get());
        }
        setDebugState("Searching log");
        return (Task) new TimeoutWanderTask();
    }

    protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    }

    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof adris.altoclef.tasks.resources.CollectStrippedLogTask) {
            adris.altoclef.tasks.resources.CollectStrippedLogTask task = (adris.altoclef.tasks.resources.CollectStrippedLogTask) other;
            return (task.targetCount == this.targetCount);
        }
        return false;
    }

    protected String toDebugStringName() {
        return "Collect Stripped Log";
    }
}
