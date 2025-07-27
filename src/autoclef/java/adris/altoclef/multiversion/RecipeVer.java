package adris.altoclef.multiversion;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.world.World;

public class RecipeVer {
  public static ItemStack getOutput(Recipe<?> recipe, World world) {
    return recipe.getResult(world.getRegistryManager());
  }
}
