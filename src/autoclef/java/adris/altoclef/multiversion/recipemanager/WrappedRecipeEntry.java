package adris.altoclef.multiversion.recipemanager;

import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

public record WrappedRecipeEntry(Identifier id, Recipe<?> value) {

}