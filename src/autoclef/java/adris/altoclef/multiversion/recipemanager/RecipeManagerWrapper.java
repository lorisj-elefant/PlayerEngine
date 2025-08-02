package adris.altoclef.multiversion.recipemanager;

import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecipeManagerWrapper {
    private final RecipeManager recipeManager;

    public static adris.altoclef.multiversion.recipemanager.RecipeManagerWrapper of(RecipeManager recipeManager) {
        if (recipeManager == null)
            return null;
        return new adris.altoclef.multiversion.recipemanager.RecipeManagerWrapper(recipeManager);
    }

    private RecipeManagerWrapper(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    public Collection<WrappedRecipeEntry> values() {
        List<WrappedRecipeEntry> result = new ArrayList<>();
        for (Identifier id : this.recipeManager.keys().toList())
            result.add(new WrappedRecipeEntry(id, this.recipeManager.get(id).get()));
        return result;
    }
}
