package adris.altoclef.tasks.construction.compound;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.List;
import java.util.Optional;

public class ConstructIronGolemTask extends Task {
    private BlockPos position;

    private boolean canBeFinished = false;

    public ConstructIronGolemTask() {
    }

    public ConstructIronGolemTask(BlockPos pos) {
        this.position = pos;
    }

    protected void onStart() {
        controller.getBehaviour().push();
        controller.getBehaviour().addProtectedItems(Items.IRON_BLOCK, Items.CARVED_PUMPKIN);
        ((List<Block>) (controller.getBaritoneSettings()).blocksToAvoidBreaking.get()).add(Blocks.IRON_BLOCK);
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        if (!StorageHelper.itemTargetsMetInventory(mod, golemMaterials(mod))) {
            setDebugState("Getting materials for the iron golem");
            return (Task) new CataloguedResourceTask(golemMaterials(mod));
        }
        if (this.position == null) {
            for (BlockPos pos : WorldHelper.scanRegion(new BlockPos(mod
                    .getPlayer().getBlockX(), 64, mod.getPlayer().getBlockZ()), new BlockPos(mod
                    .getPlayer().getBlockX(), 128, mod.getPlayer().getBlockZ()))) {
                if (mod.getWorld().getBlockState(pos).getBlock() == Blocks.AIR) {
                    this.position = pos;
                    break;
                }
            }
            if (this.position == null)
                this.position = mod.getPlayer().getBlockPos();
        }
        if (!WorldHelper.isBlock(controller, this.position, Blocks.IRON_BLOCK)) {
            if (!WorldHelper.isBlock(controller, this.position, Blocks.AIR)) {
                setDebugState("Destroying block in way of base iron block");
                return (Task) new DestroyBlockTask(this.position);
            }
            setDebugState("Placing the base iron block");
            return (Task) new PlaceBlockTask(this.position, new Block[]{Blocks.IRON_BLOCK});
        }
        if (!WorldHelper.isBlock(controller, this.position.up(), Blocks.IRON_BLOCK)) {
            if (!WorldHelper.isBlock(controller, this.position.up(), Blocks.AIR)) {
                setDebugState("Destroying block in way of center iron block");
                return (Task) new DestroyBlockTask(this.position.up());
            }
            setDebugState("Placing the center iron block");
            return (Task) new PlaceBlockTask(this.position.up(), new Block[]{Blocks.IRON_BLOCK});
        }
        if (!WorldHelper.isBlock(controller, this.position.up().east(), Blocks.IRON_BLOCK)) {
            if (!WorldHelper.isBlock(controller, this.position.up().east(), Blocks.AIR)) {
                setDebugState("Destroying block in way of east iron block");
                return (Task) new DestroyBlockTask(this.position.up().east());
            }
            setDebugState("Placing the east iron block");
            return (Task) new PlaceBlockTask(this.position.up().east(), new Block[]{Blocks.IRON_BLOCK});
        }
        if (!WorldHelper.isBlock(controller, this.position.up().west(), Blocks.IRON_BLOCK)) {
            if (!WorldHelper.isBlock(controller, this.position.up().west(), Blocks.AIR)) {
                setDebugState("Destroying block in way of west iron block");
                return (Task) new DestroyBlockTask(this.position.up().west());
            }
            setDebugState("Placing the west iron block");
            return (Task) new PlaceBlockTask(this.position.up().west(), new Block[]{Blocks.IRON_BLOCK});
        }
        if (!WorldHelper.isBlock(controller, this.position.east(), Blocks.AIR)) {
            setDebugState("Clearing area on east side...");
            return (Task) new DestroyBlockTask(this.position.east());
        }
        if (!WorldHelper.isBlock(controller, this.position.west(), Blocks.AIR)) {
            setDebugState("Clearing area on west side...");
            return (Task) new DestroyBlockTask(this.position.west());
        }
        if (!WorldHelper.isBlock(controller, this.position.up(2), Blocks.AIR)) {
            setDebugState("Destroying block in way of pumpkin");
            return (Task) new DestroyBlockTask(this.position.up(2));
        }
        this.canBeFinished = true;
        setDebugState("Placing the pumpkin (I think)");
        return (Task) new PlaceBlockTask(this.position.up(2), new Block[]{Blocks.CARVED_PUMPKIN});
    }

    protected void onStop(Task interruptTask) {
        ((List) (controller.getBaritoneSettings()).blocksToAvoidBreaking.get()).remove(Blocks.IRON_BLOCK);
        controller.getBehaviour().pop();
    }

    protected boolean isEqual(Task other) {
        return other instanceof adris.altoclef.tasks.construction.compound.ConstructIronGolemTask;
    }

    public boolean isFinished() {
        if (this.position == null)
            return false;
        Optional<Entity> closestIronGolem = controller.getEntityTracker().getClosestEntity(new Vec3d(this.position.getX(), this.position.getY(), this.position.getZ()), new Class[]{IronGolemEntity.class});
        return (closestIronGolem.isPresent() && ((Entity) closestIronGolem.get()).getBlockPos().isWithinDistance((Vec3i) this.position, 2.0D) && this.canBeFinished);
    }

    protected String toDebugString() {
        return "Construct Iron Golem";
    }

    private int ironBlocksNeeded(AltoClefController mod) {
        if (this.position == null)
            return 4;
        int needed = 0;
        if (mod.getWorld().getBlockState(this.position).getBlock() != Blocks.IRON_BLOCK)
            needed++;
        if (mod.getWorld().getBlockState(this.position.up().west()).getBlock() != Blocks.IRON_BLOCK)
            needed++;
        if (mod.getWorld().getBlockState(this.position.up().east()).getBlock() != Blocks.IRON_BLOCK)
            needed++;
        if (mod.getWorld().getBlockState(this.position.up()).getBlock() != Blocks.IRON_BLOCK)
            needed++;
        return needed;
    }

    private ItemTarget[] golemMaterials(AltoClefController mod) {
        if (this.position == null || mod.getWorld().getBlockState(this.position.up(2)).getBlock() != Blocks.CARVED_PUMPKIN)
            return new ItemTarget[]{new ItemTarget(Items.IRON_BLOCK,
                    ironBlocksNeeded(mod)), new ItemTarget(Items.CARVED_PUMPKIN, 1)};
        return new ItemTarget[]{new ItemTarget(Items.IRON_BLOCK,
                ironBlocksNeeded(mod))};
    }
}
