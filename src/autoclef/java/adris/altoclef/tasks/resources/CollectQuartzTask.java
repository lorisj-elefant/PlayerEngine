package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

public class CollectQuartzTask extends ResourceTask {
    private final int count;

    public CollectQuartzTask(int count) {
        super(Items.QUARTZ, count);
        this.count = count;
    }

    protected boolean shouldAvoidPickingUp(AltoClefController mod) {
        return false;
    }

    protected void onResourceStart(AltoClefController mod) {
    }

    protected Task onResourceTick(AltoClefController mod) {
        if (WorldHelper.getCurrentDimension(mod) != Dimension.NETHER) {
            setDebugState("Going to nether");
            return (Task) new DefaultGoToDimensionTask(Dimension.NETHER);
        }
        setDebugState("Mining");
        return (Task) new MineAndCollectTask(new ItemTarget(Items.QUARTZ, this.count), new Block[]{Blocks.NETHER_QUARTZ_ORE}, MiningRequirement.WOOD);
    }

    protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    }

    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof adris.altoclef.tasks.resources.CollectQuartzTask;
    }

    protected String toDebugStringName() {
        return "Collecting " + this.count + " quartz";
    }
}
