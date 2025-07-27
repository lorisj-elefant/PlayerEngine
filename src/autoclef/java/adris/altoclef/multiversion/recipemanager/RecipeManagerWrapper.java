package adris.altoclef.multiversion.recipemanager;

import adris.altoclef.multiversion.recipemanager.WrappedRecipeEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

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
