package adris.altoclef;

import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasks.construction.compound.ConstructIronGolemTask;
import adris.altoclef.tasks.construction.compound.ConstructNetherPortalObsidianTask;
import adris.altoclef.tasks.container.SmeltInFurnaceTask;
import adris.altoclef.tasks.container.StoreInAnyContainerTask;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import adris.altoclef.tasks.examples.ExampleTask2;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.misc.PlaceBedAndSetSpawnTask;
import adris.altoclef.tasks.misc.RavageDesertTemplesTask;
import adris.altoclef.tasks.misc.RavageRuinedPortalsTask;
import adris.altoclef.tasks.movement.EnterNetherPortalTask;
import adris.altoclef.tasks.movement.GoToStrongholdPortalTask;
import adris.altoclef.tasks.movement.LocateDesertTempleTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasks.resources.CollectBlazeRodsTask;
import adris.altoclef.tasks.resources.CollectFlintTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasks.resources.TradeWithPiglinsTask;
import adris.altoclef.tasks.speedrun.KillEnderDragonTask;
import adris.altoclef.tasks.speedrun.KillEnderDragonWithBedsTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.SmeltTarget;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Playground {
    public static void IDLE_TEST_INIT_FUNCTION(AltoClefController mod) {
    }

    public static void IDLE_TEST_TICK_FUNCTION(AltoClefController mod) {
    }

    public static void TEMP_TEST_FUNCTION(AltoClefController mod, String arg) {
        BlockPos p;
        ItemTarget target, material;
        List<ZombieEntity> zombs;
        String fname;
        List<GhastEntity> ghasts;
        Debug.logMessage("Running test...");
        switch (arg) {
            case "":
                Debug.logWarning("Please specify a test (ex. stacked, bed, terminate)");
                return;
            case "pickup":
                mod.runUserTask((Task) new PickupDroppedItemTask(new ItemTarget(Items.RAW_IRON, 3), true));
                return;
            case "chunk":
                p = new BlockPos(100000, 3, 100000);
                Debug.logMessage("LOADED? " + (!(mod.getWorld().getChunk(p) instanceof net.minecraft.world.chunk.EmptyChunk) ? 1 : 0));
                return;
            case "structure":
                mod.runUserTask((Task) new PlaceStructureBlockTask(new BlockPos(10, 6, 10)));
                return;
            case "place":
                mod.runUserTask((Task) new PlaceBlockNearbyTask(new Block[]{Blocks.CRAFTING_TABLE, Blocks.FURNACE}));
                return;
            case "stacked":
                mod.runUserTask((Task) new EquipArmorTask(new Item[]{Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_HELMET, Items.DIAMOND_BOOTS}));
                return;
            case "stacked2":
                mod.runUserTask((Task) new EquipArmorTask(new Item[]{Items.DIAMOND_CHESTPLATE}));
                return;
            case "ravage":
                mod.runUserTask((Task) new RavageRuinedPortalsTask());
                return;
            case "temples":
                mod.runUserTask((Task) new RavageDesertTemplesTask());
                return;
            case "smelt":
                target = new ItemTarget("iron_ingot", 4);
                material = new ItemTarget("iron_ore", 4);
                mod.runUserTask((Task) new SmeltInFurnaceTask(new SmeltTarget(target, material, new Item[0])));
                return;
            case "iron":
                mod.runUserTask((Task) new ConstructIronGolemTask());
                return;
            case "avoid":
                mod.getBehaviour().avoidBlockBreaking(b -> (-1000 < b.getX() && b.getX() < 1000 && -1000 < b.getY() && b.getY() < 1000 && -1000 < b.getZ() && b.getZ() < 1000));
                Debug.logMessage("Testing avoid from -1000, -1000, -1000 to 1000, 1000, 1000");
                return;
            case "portal":
                mod.runUserTask((Task) new EnterNetherPortalTask((Task) new ConstructNetherPortalObsidianTask(), (WorldHelper.getCurrentDimension(mod) == Dimension.OVERWORLD) ? Dimension.NETHER : Dimension.OVERWORLD));
                return;
            case "kill":
                zombs = mod.getEntityTracker().getTrackedEntities(ZombieEntity.class);
                if (zombs.size() == 0) {
                    Debug.logWarning("No zombs found.");
                } else {
                    LivingEntity entity = (LivingEntity) zombs.get(0);
                    mod.runUserTask((Task) new KillEntityTask((Entity) entity));
                }
                return;
            case "craft":
                (new Thread(() -> {
                    for (int i = 3; i > 0; i--) {
                        Debug.logMessage("" + i + "...");
                        sleepSec(1.0D);
                    }
                    Item[] c = {Items.COBBLESTONE};
                    Item[] s = {Items.STICK};
                    CraftingRecipe recipe = CraftingRecipe.newShapedRecipe("test pickaxe", new Item[][]{c, c, c, null, s, null, null, s, null}, 1);
                    //mod.runUserTask((Task)new CraftGenericManuallyTask(new RecipeTarget(Items.STONE_PICKAXE, 1, recipe)));
                })).start();
                return;
            case "food":
                mod.runUserTask((Task) new CollectFoodTask(20.0D));
                return;
            case "temple":
                mod.runUserTask((Task) new LocateDesertTempleTask());
                return;
            case "blaze":
                mod.runUserTask((Task) new CollectBlazeRodsTask(7));
                return;
            case "flint":
                mod.runUserTask((Task) new CollectFlintTask(5));
                return;
            case "unobtainable":
                fname = "unobtainables.txt";
                try {
                    int unobtainable = 0;
                    int total = 0;
                    File f = new File(fname);
                    FileWriter fw = new FileWriter(f);
                    for (Identifier id : Registries.ITEM.getIds()) {
                        Item item = (Item) Registries.ITEM.get(id);
                        if (!TaskCatalogue.isObtainable(item)) {
                            unobtainable++;
                            fw.write(item.getTranslationKey() + "\n");
                        }
                        total++;
                    }
                    fw.flush();
                    fw.close();
                    Debug.logMessage("" + unobtainable + " / " + unobtainable + " unobtainable items. Wrote a list of items to \"" + total + "\".");
                } catch (IOException e) {
                    Debug.logWarning(e.toString());
                }
                return;
            case "piglin":
                mod.runUserTask((Task) new TradeWithPiglinsTask(32, new ItemTarget(Items.ENDER_PEARL, 12)));
                return;
            case "stronghold":
                mod.runUserTask((Task) new GoToStrongholdPortalTask(12));
                return;
            case "bed":
                mod.runUserTask((Task) new PlaceBedAndSetSpawnTask());
                return;
            case "dragon":
                mod.runUserTask((Task) new KillEnderDragonWithBedsTask());
                return;
            case "dragon-pearl":
                mod.runUserTask((Task) new ThrowEnderPearlSimpleProjectileTask(new BlockPos(0, 60, 0)));
                return;
            case "dragon-old":
                mod.runUserTask((Task) new KillEnderDragonTask());
                return;
            case "chest":
                mod.runUserTask((Task) new StoreInAnyContainerTask(true, new ItemTarget[]{new ItemTarget(Items.DIAMOND, 3)}));
                return;
            case "example":
                mod.runUserTask((Task) new ExampleTask2());
                return;
            case "netherite":
                mod.runUserTask((Task) TaskCatalogue.getSquashedItemTask(new ItemTarget[]{new ItemTarget("netherite_pickaxe", 1), new ItemTarget("netherite_sword", 1), new ItemTarget("netherite_helmet", 1), new ItemTarget("netherite_chestplate", 1), new ItemTarget("netherite_leggings", 1), new ItemTarget("netherite_boots", 1)}));
                return;
            case "arrow":
                ghasts = mod.getEntityTracker().getTrackedEntities(GhastEntity.class);
                if (ghasts.size() == 0) {
                    Debug.logWarning("No ghasts found.");
                } else {
                    GhastEntity ghast = ghasts.get(0);
                    mod.runUserTask((Task) new ShootArrowSimpleProjectileTask((Entity) ghast));
                }
                return;
        }
        mod.logWarning("Test not found: \"" + arg + "\".");
    }

    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000.0D * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
