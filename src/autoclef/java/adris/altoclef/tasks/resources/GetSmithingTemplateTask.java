package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.SearchChunkForBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class GetSmithingTemplateTask extends ResourceTask {
    private final Task searcher = (Task) new SearchChunkForBlockTask(new Block[]{Blocks.BLACKSTONE});

    private final int count;

    private BlockPos chestloc = null;

    public GetSmithingTemplateTask(int count) {
        super(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, count);
        this.count = count;
    }

    protected void onResourceStart(AltoClefController mod) {
    }

    protected Task onResourceTick(AltoClefController mod) {
        if (WorldHelper.getCurrentDimension(mod) != Dimension.NETHER) {
            setDebugState("Going to nether");
            return (Task) new DefaultGoToDimensionTask(Dimension.NETHER);
        }
        if (this.chestloc == null)
            for (BlockPos pos : mod.getBlockScanner().getKnownLocations(new Block[]{Blocks.CHEST})) {
                if (WorldHelper.isInteractableBlock(mod, pos)) {
                    this.chestloc = pos;
                    break;
                }
            }
        if (this.chestloc != null) {
            setDebugState("Destroying Chest");
            if (WorldHelper.isInteractableBlock(mod, this.chestloc))
                return (Task) new DestroyBlockTask(this.chestloc);
            this.chestloc = null;
            for (BlockPos pos : mod.getBlockScanner().getKnownLocations(new Block[]{Blocks.CHEST})) {
                if (WorldHelper.isInteractableBlock(mod, pos)) {
                    this.chestloc = pos;
                    break;
                }
            }
        }
        setDebugState("Searching for/Traveling around bastion");
        return this.searcher;
    }

    protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    }

    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof adris.altoclef.tasks.resources.GetSmithingTemplateTask;
    }

    protected String toDebugStringName() {
        return "Collect " + this.count + " smithing templates";
    }

    protected boolean shouldAvoidPickingUp(AltoClefController mod) {
        return false;
    }
}
