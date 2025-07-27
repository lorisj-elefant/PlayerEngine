package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClefController;
import adris.altoclef.tasks.movement.SearchWithinBiomeTask;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biomes;

public class RavageDesertTemplesTask extends Task {
  public final Item[] LOOT = new Item[] { 
      Items.BONE, Items.ROTTEN_FLESH, Items.GUNPOWDER, Items.SAND, Items.STRING, Items.SPIDER_EYE, Items.ENCHANTED_BOOK, Items.SADDLE, Items.GOLDEN_APPLE, Items.GOLD_INGOT, 
      Items.IRON_INGOT, Items.EMERALD, Items.IRON_HORSE_ARMOR, Items.GOLDEN_HORSE_ARMOR, Items.DIAMOND, Items.DIAMOND_HORSE_ARMOR, Items.ENCHANTED_GOLDEN_APPLE };
  
  private BlockPos currentTemple;
  
  private Task lootTask;
  
  private Task pickaxeTask;
  
  protected void onStart() {
    controller.getBehaviour().push();
  }
  
  protected Task onTick() {
    if (this.pickaxeTask != null && !this.pickaxeTask.isFinished()) {
      setDebugState("Need to get pickaxes first");
      return this.pickaxeTask;
    } 
    if (this.lootTask != null && !this.lootTask.isFinished()) {
      setDebugState("Looting found temple");
      return this.lootTask;
    } 
    if (StorageHelper.miningRequirementMetInventory(controller, MiningRequirement.WOOD)) {
      setDebugState("Need to get pickaxes first");
      this.pickaxeTask = (Task)new CataloguedResourceTask(new ItemTarget[] { new ItemTarget(Items.WOODEN_PICKAXE, 2) });
      return this.pickaxeTask;
    } 
    this.currentTemple = WorldHelper.getADesertTemple(controller);
    if (this.currentTemple != null) {
      this.lootTask = (Task)new LootDesertTempleTask(this.currentTemple, List.of(this.LOOT));
      setDebugState("Looting found temple");
      return this.lootTask;
    } 
    return (Task)new SearchWithinBiomeTask(Biomes.DESERT);
  }
  
  protected void onStop(Task task) {
    controller.getBehaviour().pop();
  }
  
  protected boolean isEqual(Task other) {
    return other instanceof adris.altoclef.tasks.misc.RavageDesertTemplesTask;
  }
  
  public boolean isFinished() {
    return false;
  }
  
  protected String toDebugString() {
    return "Ravaging Desert Temples";
  }
}
