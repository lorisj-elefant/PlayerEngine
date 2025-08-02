package adris.altoclef.trackers;

import adris.altoclef.AltoClefController;
import adris.altoclef.multiversion.recipemanager.RecipeManagerWrapper;
import adris.altoclef.multiversion.recipemanager.WrappedRecipeEntry;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.RecipeTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CraftingRecipeTracker extends Tracker {
    private final HashMap<Item, List<CraftingRecipe>> itemRecipeMap = new HashMap<>();

    private final HashMap<CraftingRecipe, ItemStack> recipeResultMap = new HashMap<>();

    private boolean shouldRebuild;

    public CraftingRecipeTracker(TrackerManager manager) {
        super(manager);
        this.shouldRebuild = true;
    }

    public List<CraftingRecipe> getRecipeForItem(Item item) {
        ensureUpdated();
        if (!hasRecipeForItem(item)) {
            this.mod.logWarning("trying to access recipe for unknown item: " + item);
            return null;
        }
        return this.itemRecipeMap.get(item);
    }

    public CraftingRecipe getFirstRecipeForItem(Item item) {
        ensureUpdated();
        if (!hasRecipeForItem(item)) {
            this.mod.logWarning("trying to access recipe for unknown item: " + item);
            return null;
        }
        return ((List<CraftingRecipe>) this.itemRecipeMap.get(item)).get(0);
    }

    public List<RecipeTarget> getRecipeTarget(Item item, int targetCount) {
        ensureUpdated();
        List<RecipeTarget> targets = new ArrayList<>();
        for (CraftingRecipe recipe : getRecipeForItem(item))
            targets.add(new RecipeTarget(item, targetCount, recipe));
        return targets;
    }

    public RecipeTarget getFirstRecipeTarget(Item item, int targetCount) {
        ensureUpdated();
        return new RecipeTarget(item, targetCount, getFirstRecipeForItem(item));
    }

    public boolean hasRecipeForItem(Item item) {
        ensureUpdated();
        return this.itemRecipeMap.containsKey(item);
    }

    public ItemStack getRecipeResult(CraftingRecipe recipe) {
        ensureUpdated();
        if (!hasRecipe(recipe)) {
            this.mod.logWarning("Trying to get result for unknown recipe: " + String.valueOf(recipe));
            return null;
        }
        ItemStack result = this.recipeResultMap.get(recipe);
        return new ItemStack((ItemConvertible) result.getItem(), result.getCount());
    }

    public boolean hasRecipe(CraftingRecipe recipe) {
        ensureUpdated();
        return this.recipeResultMap.containsKey(recipe);
    }

    protected void updateState() {
        if (!this.shouldRebuild)
            return;
        if (!AltoClefController.inGame())
            return;
        RecipeManagerWrapper recipeManager = RecipeManagerWrapper.of(mod.getWorld().getRecipeManager());
        for (WrappedRecipeEntry recipe : recipeManager.values()) {
            Recipe<?> recipe1 = recipe.value();
            if (recipe1 instanceof CraftingRecipe) {
                net.minecraft.recipe.CraftingRecipe craftingRecipe = (net.minecraft.recipe.CraftingRecipe) recipe1;
                if (craftingRecipe instanceof net.minecraft.recipe.SpecialCraftingRecipe)
                    continue;
                ItemStack result = new ItemStack((ItemConvertible) craftingRecipe.getResult(null).getItem(), craftingRecipe.getResult(null).getCount());
                Item[][] altoclefRecipeItems = getShapedCraftingRecipe((List<Ingredient>) craftingRecipe.getIngredients());
                CraftingRecipe altoclefRecipe = CraftingRecipe.newShapedRecipe(altoclefRecipeItems, result.getCount());
                if (this.itemRecipeMap.containsKey(result.getItem())) {
                    ((List<CraftingRecipe>) this.itemRecipeMap.get(result.getItem())).add(altoclefRecipe);
                } else {
                    List<CraftingRecipe> recipes = new ArrayList<>();
                    recipes.add(altoclefRecipe);
                    this.itemRecipeMap.put(result.getItem(), recipes);
                }
                this.recipeResultMap.put(altoclefRecipe, result);
            }
        }
        this.itemRecipeMap.replaceAll((k, v) -> Collections.unmodifiableList(v));
        this.shouldRebuild = false;
    }

    private static Item[][] getShapedCraftingRecipe(List<Ingredient> ingredients) {
        Item[][] result = new Item[9][];
        int x = 0;
        for (Ingredient ingredient : ingredients) {
            ItemStack[] stacks = ingredient.getMatchingStacks();
            Item[] items = new Item[stacks.length];
            for (int i = 0; i < stacks.length; i++) {
                ItemStack stack = stacks[i];
                if (stack.getCount() > 1)
                    throw new IllegalStateException("recipe needs more then one item on a slot... well... shit (ingredients: " + String.valueOf(ingredient) + ")");
                items[i] = stack.getItem();
            }
            if (stacks.length != 0) {
                (new Item[1])[0] = items[0];
                result[x] = new Item[1];
            } else {
                result[x] = null;
            }
            x++;
        }
        return result;
    }

    protected void reset() {
        this.shouldRebuild = true;
        this.itemRecipeMap.clear();
        this.recipeResultMap.clear();
    }

    protected boolean isDirty() {
        return this.shouldRebuild;
    }
}
