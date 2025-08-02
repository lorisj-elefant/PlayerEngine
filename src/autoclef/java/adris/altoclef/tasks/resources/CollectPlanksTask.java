package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.Arrays;

public class CollectPlanksTask extends ResourceTask {
    private final Item[] planks;

    private final Item[] logs;

    private final int targetCount;

    private boolean logsInNether;

    public CollectPlanksTask(Item[] planks, Item[] logs, int count, boolean logsInNether) {
        super(new ItemTarget(planks, count));
        this.planks = planks;
        this.logs = logs;
        this.targetCount = count;
        this.logsInNether = logsInNether;
    }

    public CollectPlanksTask(int count) {
        this(ItemHelper.PLANKS, ItemHelper.LOG, count, false);
    }

    public CollectPlanksTask(Item plank, Item log, int count) {
        this(new Item[]{plank}, new Item[]{log}, count, false);
    }

    public CollectPlanksTask(Item plank, int count) {
        this(plank, ItemHelper.planksToLog(plank), count);
    }

    private static CraftingRecipe generatePlankRecipe(Item[] logs) {
        return CraftingRecipe.newShapedRecipe("planks", new Item[][]{logs, null, null, null}, 4);
    }

    protected double getPickupRange(AltoClefController mod) {
        ItemStorageTracker storage = mod.getItemStorage();
        if (storage.getItemCount(ItemHelper.LOG) * 4 > this.targetCount)
            return 10.0D;
        return 50.0D;
    }

    protected boolean shouldAvoidPickingUp(AltoClefController mod) {
        return false;
    }

    protected void onResourceStart(AltoClefController mod) {
    }

    protected Task onResourceTick(AltoClefController mod) {
        int totalInventoryPlankCount = mod.getItemStorage().getItemCount(this.planks);
        int potentialPlanks = totalInventoryPlankCount + mod.getItemStorage().getItemCount(this.logs) * 4;
        if (potentialPlanks >= this.targetCount)
            for (Item logCheck : this.logs) {
                int count = mod.getItemStorage().getItemCount(new Item[]{logCheck});
                if (count > 0) {
                    Item plankCheck = ItemHelper.logToPlanks(logCheck);
                    if (plankCheck == null)
                        Debug.logError("Invalid/Un-convertable log: " + String.valueOf(logCheck) + " (failed to find corresponding plank)");
                    int plankCount = mod.getItemStorage().getItemCount(new Item[]{plankCheck});
                    int otherPlankCount = totalInventoryPlankCount - plankCount;
                    int targetTotalPlanks = Math.min(count * 4 + plankCount, this.targetCount - otherPlankCount);
                    setDebugState("We have " + String.valueOf(logCheck) + ", crafting " + targetTotalPlanks + " planks.");
                    return (Task) new CraftInInventoryTask(new RecipeTarget(plankCheck, targetTotalPlanks, generatePlankRecipe(this.logs)));
                }
            }
        ArrayList<ItemTarget> blocksTomine = new ArrayList<>(2);
        blocksTomine.add(new ItemTarget(this.logs));
        if (!mod.getBehaviour().exclusivelyMineLogs()) ;
        MineAndCollectTask mineAndCollectTask = new MineAndCollectTask((ItemTarget[]) blocksTomine.toArray(x$0 -> new ItemTarget[x$0]), MiningRequirement.HAND);
        if (this.logsInNether)
            mineAndCollectTask.forceDimension(Dimension.NETHER);
        return (Task) mineAndCollectTask;
    }

    protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    }

    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof adris.altoclef.tasks.resources.CollectPlanksTask;
    }

    protected String toDebugStringName() {
        return "Crafting " + this.targetCount + " planks " + Arrays.toString(this.planks);
    }

    public adris.altoclef.tasks.resources.CollectPlanksTask logsInNether() {
        this.logsInNether = true;
        return this;
    }
}
