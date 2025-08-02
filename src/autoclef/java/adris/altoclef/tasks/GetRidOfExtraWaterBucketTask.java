package adris.altoclef.tasks;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.resources.CollectBucketLiquidTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class GetRidOfExtraWaterBucketTask extends Task {
    private boolean needsPickup = false;

    protected void onStart() {
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        if (mod.getItemStorage().getItemCount(new Item[]{Items.WATER_BUCKET}) != 0 && !this.needsPickup)
            return (Task) new InteractWithBlockTask(new ItemTarget(Items.WATER_BUCKET, 1), mod.getPlayer().getBlockPos().down(), false);
        this.needsPickup = true;
        if (mod.getItemStorage().getItemCount(new Item[]{Items.WATER_BUCKET}) < 1)
            return (Task) new CollectBucketLiquidTask.CollectWaterBucketTask(1);
        return null;
    }

    public boolean isFinished() {
        return (controller.getItemStorage().getItemCount(new Item[]{Items.WATER_BUCKET}) == 1 && this.needsPickup);
    }

    protected void onStop(Task interruptTask) {
    }

    protected boolean isEqual(Task other) {
        return other instanceof adris.altoclef.tasks.GetRidOfExtraWaterBucketTask;
    }

    protected String toDebugString() {
        return null;
    }
}
