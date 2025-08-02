package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.movement.GetCloseToBlockTask;
import adris.altoclef.tasks.movement.SearchChunkForBlockTask;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;

import java.util.Optional;

public class CollectHoneycombTask extends ResourceTask {
    private final boolean campfire;

    private final int count;

    private BlockPos nest;

    public CollectHoneycombTask(int targetCount) {
        super(Items.HONEYCOMB, targetCount);
        this.campfire = true;
        this.count = targetCount;
    }

    public CollectHoneycombTask(int targetCount, boolean useCampfire) {
        super(Items.HONEYCOMB, targetCount);
        this.campfire = useCampfire;
        this.count = targetCount;
    }

    protected void onResourceStart(AltoClefController mod) {
        mod.getBehaviour().push();
    }

    protected Task onResourceTick(AltoClefController mod) {
        if (this.nest == null) {
            Optional<BlockPos> getNearestNest = mod.getBlockScanner().getNearestBlock(new Block[]{Blocks.BEE_NEST});
            if (getNearestNest.isPresent())
                this.nest = getNearestNest.get();
        }
        if (this.nest == null) {
            if (this.campfire && !mod.getItemStorage().hasItemInventoryOnly(new Item[]{Items.CAMPFIRE})) {
                setDebugState("Can't find nest, getting campfire first...");
                return (Task) new CataloguedResourceTask(new ItemTarget[]{new ItemTarget(Items.CAMPFIRE, 1)});
            }
            setDebugState("Alright, we're searching");
            return (Task) new SearchChunkForBlockTask(new Block[]{Blocks.BEE_NEST});
        }
        if (this.campfire && !isCampfireUnderNest(mod, this.nest)) {
            if (!mod.getItemStorage().hasItemInventoryOnly(new Item[]{Items.CAMPFIRE})) {
                setDebugState("Getting a campfire");
                return (Task) new CataloguedResourceTask(new ItemTarget[]{new ItemTarget(Items.CAMPFIRE, 1)});
            }
            setDebugState("Placing campfire");
            return (Task) new PlaceBlockTask(this.nest.down(2), new Block[]{Blocks.CAMPFIRE});
        }
        if (!mod.getItemStorage().hasItemInventoryOnly(new Item[]{Items.SHEARS})) {
            setDebugState("Getting shears");
            return (Task) new CataloguedResourceTask(new ItemTarget[]{new ItemTarget(Items.SHEARS, 1)});
        }
        if (((Integer) mod.getWorld().getBlockState(this.nest).get((Property) Properties.HONEY_LEVEL)).intValue() != 5) {
            if (!this.nest.isCenterWithinDistance((Position) mod.getPlayer().getPos(), 20.0D)) {
                setDebugState("Getting close to nest");
                return (Task) new GetCloseToBlockTask(this.nest);
            }
            setDebugState("Waiting for nest to get honey...");
            return null;
        }
        return (Task) new InteractWithBlockTask(Items.SHEARS, this.nest);
    }

    protected void onResourceStop(AltoClefController mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof adris.altoclef.tasks.resources.CollectHoneycombTask;
    }

    protected String toDebugStringName() {
        return "Collecting " + this.count + " Honeycombs " + (this.campfire ? "Peacefully" : "Recklessly");
    }

    protected boolean shouldAvoidPickingUp(AltoClefController mod) {
        return false;
    }

    private boolean isCampfireUnderNest(AltoClefController mod, BlockPos pos) {
        for (BlockPos underPos : WorldHelper.scanRegion(pos.down(6), pos.down())) {
            if (mod.getWorld().getBlockState(underPos).getBlock() == Blocks.CAMPFIRE)
                return true;
        }
        return false;
    }
}
