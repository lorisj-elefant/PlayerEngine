package adris.altoclef.tasks.resources;

import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.item.Item;

public class GetBuildingMaterialsTask extends Task {
    private final int count;

    public GetBuildingMaterialsTask(int count) {
        this.count = count;
    }

    protected void onStart() {
    }

    protected Task onTick() {
        Item[] throwaways = controller.getModSettings().getThrowawayItems(controller, true);
        return (Task) new MineAndCollectTask(new ItemTarget[]{new ItemTarget(throwaways, this.count)}, MiningRequirement.WOOD);
    }

    protected void onStop(Task interruptTask) {
    }

    protected boolean isEqual(Task other) {
        if (other instanceof adris.altoclef.tasks.resources.GetBuildingMaterialsTask) {
            adris.altoclef.tasks.resources.GetBuildingMaterialsTask task = (adris.altoclef.tasks.resources.GetBuildingMaterialsTask) other;
            return (task.count == this.count);
        }
        return false;
    }

    public boolean isFinished() {
        return (StorageHelper.getBuildingMaterialCount(controller) >= this.count);
    }

    protected String toDebugString() {
        return "Collecting " + this.count + " building materials.";
    }
}
