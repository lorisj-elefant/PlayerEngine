package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import net.minecraft.item.Item;

public abstract class CraftWithMatchingMaterialsTask extends ResourceTask {
    private final ItemTarget target;

    private final CraftingRecipe recipe;

    private final boolean[] sameMask;

    private final ItemTarget sameResourceTarget;

    private final int sameResourceRequiredCount;

    private final int sameResourcePerRecipe;

    public CraftWithMatchingMaterialsTask(ItemTarget target, CraftingRecipe recipe, boolean[] sameMask) {
        super(target);
        this.target = target;
        this.recipe = recipe;
        this.sameMask = sameMask;
        int sameResourceRequiredCount = 0;
        ItemTarget sameResourceTarget = null;
        if (recipe.getSlotCount() != sameMask.length)
            Debug.logError("Invalid CraftWithMatchingMaterialsTask constructor parameters: Recipe size must equal \"sameMask\" size.");
        for (int i = 0; i < recipe.getSlotCount(); i++) {
            if (sameMask[i]) {
                sameResourceRequiredCount++;
                sameResourceTarget = recipe.getSlot(i);
            }
        }
        this.sameResourceTarget = sameResourceTarget;
        int craftsNeeded = (int) (1.0D + Math.floor(target.getTargetCount() / recipe.outputCount() - 0.001D));
        this.sameResourcePerRecipe = sameResourceRequiredCount;
        this.sameResourceRequiredCount = sameResourceRequiredCount * craftsNeeded;
    }

    private static CraftingRecipe generateSameRecipe(CraftingRecipe diverseRecipe, Item sameItem, boolean[] sameMask) {
        ItemTarget[] result = new ItemTarget[diverseRecipe.getSlotCount()];
        for (int i = 0; i < result.length; i++) {
            if (sameMask[i]) {
                result[i] = new ItemTarget(sameItem, 1);
            } else {
                result[i] = diverseRecipe.getSlot(i);
            }
        }
        return CraftingRecipe.newShapedRecipe(result, diverseRecipe.outputCount());
    }

    protected void onResourceStart(AltoClefController mod) {
    }

    protected Task onResourceTick(AltoClefController mod) {
        int canCraftTotal = 0;
        int majorityCraftCount = 0;
        Item majorityCraftItem = null;
        for (Item sameCheck : this.sameResourceTarget.getMatches()) {
            int count = getExpectedTotalCountOfSameItem(mod, sameCheck);
            int canCraft = count / this.sameResourcePerRecipe * this.recipe.outputCount();
            canCraftTotal += canCraft;
            if (canCraft > majorityCraftCount) {
                majorityCraftCount = canCraft;
                majorityCraftItem = sameCheck;
            }
        }
        int currentTargetCount = mod.getItemStorage().getItemCount(new ItemTarget[]{this.target});
        int currentTargetsRequired = this.target.getTargetCount() - currentTargetCount;
        if (canCraftTotal >= currentTargetsRequired) {
            int trueCanCraftTotal = 0;
            for (Item sameCheck : this.sameResourceTarget.getMatches()) {
                int trueCount = mod.getItemStorage().getItemCount(new Item[]{sameCheck});
                int trueCanCraft = trueCount / this.sameResourcePerRecipe * this.recipe.outputCount();
                trueCanCraftTotal += trueCanCraft;
            }
            if (trueCanCraftTotal < currentTargetsRequired)
                return getSpecificSameResourceTask(mod, this.sameResourceTarget.getMatches());
            CraftingRecipe sameRecipe = generateSameRecipe(this.recipe, majorityCraftItem, this.sameMask);
            int toCraftTotal = majorityCraftCount;
            toCraftTotal = Math.min(toCraftTotal, this.target.getTargetCount());
            Item output = getSpecificItemCorrespondingToMajorityResource(majorityCraftItem);
            toCraftTotal = Math.min(this.target.getTargetCount(), toCraftTotal + mod.getItemStorage().getItemCount(new Item[]{output}));
            RecipeTarget recipeTarget = new RecipeTarget(output, toCraftTotal, sameRecipe);
            return this.recipe.isBig() ? (Task) new CraftInTableTask(recipeTarget) : (Task) new CraftInInventoryTask(recipeTarget);
        }
        return getAllSameResourcesTask(mod);
    }

    protected void onResourceStop(AltoClefController mod, Task interruptTask) {
    }

    protected Task getAllSameResourcesTask(AltoClefController mod) {
        ItemTarget infinityVersion = new ItemTarget(this.sameResourceTarget, 999999);
        return (Task) TaskCatalogue.getItemTask(infinityVersion);
    }

    protected int getExpectedTotalCountOfSameItem(AltoClefController mod, Item sameItem) {
        return mod.getItemStorage().getItemCount(new Item[]{sameItem});
    }

    protected Task getSpecificSameResourceTask(AltoClefController mod, Item[] toGet) {
        Debug.logError("Uh oh!!! getSpecificSameResourceTask should be implemented!!!! Now we're stuck.");
        return null;
    }

    protected abstract Item getSpecificItemCorrespondingToMajorityResource(Item paramItem);
}
