package adris.altoclef.util;

import net.minecraft.item.Item;

import java.util.Objects;

public class RecipeTarget {
    private final CraftingRecipe recipe;

    private final Item item;

    private final int targetCount;

    public RecipeTarget(Item item, int targetCount, CraftingRecipe recipe) {
        this.item = item;
        this.targetCount = targetCount;
        this.recipe = recipe;
    }

    public CraftingRecipe getRecipe() {
        return this.recipe;
    }

    public Item getOutputItem() {
        return this.item;
    }

    public int getTargetCount() {
        return this.targetCount;
    }

    public String toString() {
        return (this.targetCount == 1) ? ("Recipe{" +
                String.valueOf(this.item) + "}") : ("Recipe{" +

                String.valueOf(this.item) + " x " + this.targetCount + "}");
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        adris.altoclef.util.RecipeTarget that = (adris.altoclef.util.RecipeTarget) o;
        return (this.targetCount == that.targetCount && this.recipe.equals(that.recipe) && Objects.equals(this.item, that.item));
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.recipe, this.item});
    }
}
