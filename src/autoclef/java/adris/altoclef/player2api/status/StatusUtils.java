package adris.altoclef.player2api.status;

import adris.altoclef.AltoClefController;
import adris.altoclef.chains.SingleTaskChain;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import baritone.api.entity.IAutomatone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StatusUtils {
    public static String getInventoryString(AltoClefController mod) {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < mod.getBaritone().getEntityContext().inventory().size(); i++) {
            ItemStack stack = mod.getBaritone().getEntityContext().inventory().getStack(i);
            if (!stack.isEmpty()) {
                String name = ItemHelper.stripItemName(stack.getItem());
                counts.put(name, counts.getOrDefault(name, 0) + stack.getCount());
            }
        }
        ObjectStatus status = new ObjectStatus();
        for (Map.Entry<String, Integer> entry : counts.entrySet())
            status.add(entry.getKey(), entry.getValue().toString());
        return status.toString();
    }

    public static String getDimensionString(AltoClefController mod) {
        return mod.getWorld().getRegistryKey().getValue().toString().replace("minecraft:", "");
    }

    public static String getWeatherString(AltoClefController mod) {
        boolean isRaining = mod.getWorld().isRaining();
        boolean isThundering = mod.getWorld().isThundering();
        ObjectStatus status = (new ObjectStatus()).add("isRaining", String.valueOf(isRaining)).add("isThundering", String.valueOf(isThundering));
        return status.toString();
    }

    public static String getSpawnPosString(AltoClefController mod) {
        BlockPos spawnPos = mod.getWorld().getSpawnPos();
        return String.format("(%d, %d, %d)", spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
    }

    public static String getTaskStatusString(AltoClefController mod) {
        List<Task> tasks = mod.getUserTaskChain().getTasks();
        if (tasks.isEmpty())
            return "No tasks currently running.";
        return tasks.get(0).toString();
    }

    public static String getNearbyBlocksString(AltoClefController mod) {
        int radius = 12;
        BlockPos center = mod.getPlayer().getBlockPos();
        Map<String, Integer> blockCounts = new HashMap<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    String blockName = mod.getWorld().getBlockState(pos).getBlock().getTranslationKey().replace("block.minecraft.", "");
                    if (!blockName.equals("air"))
                        blockCounts.put(blockName, blockCounts.getOrDefault(blockName, 0) + 1);
                }
            }
        }
        ObjectStatus status = new ObjectStatus();
        for (Map.Entry<String, Integer> entry : blockCounts.entrySet())
            status.add(entry.getKey(), entry.getValue().toString());
        return status.toString();
    }

    public static String getOxygenString(AltoClefController mod) {
        return String.format("%s/300", mod.getPlayer().getAir());
    }

    public static String getNearbyHostileMobs(AltoClefController mod) {
        int radius = 32;
        List<String> descriptions = new ArrayList<>();
        for (Entity entity : mod.getWorld().iterateEntities()) {
            if (entity instanceof net.minecraft.entity.mob.HostileEntity && entity.distanceTo(mod.getPlayer()) < radius) {
                String type = entity.getType().getTranslationKey();
                String niceName = type.replace("entity.minecraft.", "");
                String position = entity.getPos().floorAlongAxes(EnumSet.allOf(Direction.Axis.class)).toString();
                descriptions.add(niceName + " at " + position);
            }
        }
        if (descriptions.isEmpty())
            return String.format("no nearby hostile mobs within %d", radius);
        return "[" + String.join(",", descriptions.stream()
                .map(s -> "\"" + s + "\"")
                .toArray(String[]::new)) + "]";
    }

    public static String getEquippedArmorStatusString(AltoClefController mod) {
        LivingEntity player = mod.getPlayer();
        ObjectStatus status = new ObjectStatus();
        ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack legs = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack feet = player.getEquippedStack(EquipmentSlot.FEET);
        ItemStack offhand = player.getEquippedStack(EquipmentSlot.OFFHAND);
        status.add("helmet", (head.isEmpty() || !(head.getItem() instanceof net.minecraft.item.ArmorItem)) ? "none" :
                head.getItem().getTranslationKey().replace("item.minecraft.", ""));
        status.add("chestplate", (chest.isEmpty() || !(chest.getItem() instanceof net.minecraft.item.ArmorItem)) ? "none" :
                chest.getItem().getTranslationKey().replace("item.minecraft.", ""));
        status.add("leggings", (legs.isEmpty() || !(legs.getItem() instanceof net.minecraft.item.ArmorItem)) ? "none" :
                legs.getItem().getTranslationKey().replace("item.minecraft.", ""));
        status.add("boots", (feet.isEmpty() || !(feet.getItem() instanceof net.minecraft.item.ArmorItem)) ? "none" :
                feet.getItem().getTranslationKey().replace("item.minecraft.", ""));
        status.add("offhand_shield", (offhand.isEmpty() || !(offhand.getItem() instanceof net.minecraft.item.ShieldItem)) ? "none" :
                offhand.getItem().getTranslationKey().replace("item.minecraft.", ""));
        return status.toString();
    }

    public static String getNearbyPlayers(AltoClefController mod) {
        List<String> descriptions = new ArrayList<>();
        for (Entity entity : mod.getEntityTracker().getCloseEntities()) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                if (entity.distanceTo(mod.getPlayer()) < 32.0F) {
                    String username = player.getName().getString();
                    String position = entity.getPos().floorAlongAxes(EnumSet.allOf(Direction.Axis.class)).toString();
                    descriptions.add(username + " at " + position);
                }
            }
        }
        if (descriptions.isEmpty())
            return String.format("no nearby users within %d", 32);
        return "[" + String.join(",", descriptions.stream()
                .map(s -> "\"" + s + "\"")
                .toArray(String[]::new)) + "]";
    }

    public static String getNearbyNPCs(AltoClefController mod) {
        List<String> descriptions = new ArrayList<>();
        for (Entity entity : mod.getEntityTracker().getCloseEntities()) {
            if (entity instanceof IAutomatone) {
                if (entity.distanceTo(mod.getPlayer()) < 32.0F) {
                    String username = entity.getDisplayName().getString();
                    if (!Objects.equals(username, mod.getPlayer().getDisplayName().getString())) {
                        String position = entity.getPos().floorAlongAxes(EnumSet.allOf(Direction.Axis.class)).toString();
                        descriptions.add(username + " at " + position);
                    }
                }
            }
        }
        if (descriptions.isEmpty())
            return String.format("no nearby npcs within %d", 32);
        return "[" + String.join(",", descriptions.stream()
                .map(s -> "\"" + s + "\"")
                .toArray(String[]::new)) + "]";
    }

    public static float getUserNameDistance(AltoClefController mod, String targetUsername) {
        for (PlayerEntity player : mod.getWorld().getPlayers()) {
            String username = player.getName().getString();
            if (username.equals(targetUsername))
                return player.distanceTo(mod.getPlayer());
        }
        return Float.MAX_VALUE;
    }

    public static String getDifficulty(AltoClefController mod) {
        return mod.getWorld().getDifficulty().toString();
    }

    public static String getTimeString(AltoClefController mod) {
        ObjectStatus status = new ObjectStatus();
        status.add("isDay", Boolean.toString(mod.getWorld().isDay()));
        status.add("timeOfDay", String.format("%d/24,000", mod.getWorld().getTimeOfDay() % 24000L));
        return status.toString();
    }

    public static String getGamemodeString(AltoClefController mod) {
        return mod.getInteractionManager().getGameType().isCreative() ? "creative" : "survival";
    }

    public static String getTaskTree(AltoClefController mod) {
        Task task = mod.getUserTaskChain().getCurrentTask();
        if(task==null) return "Task tree is empty";
        return task.getTaskTree();
    }
}
