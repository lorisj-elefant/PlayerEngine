package adris.altoclef.player2api.status;

import adris.altoclef.AltoClefController;
import net.minecraft.entity.LivingEntity;

public class AgentStatus extends ObjectStatus {
    public static adris.altoclef.player2api.status.AgentStatus fromMod(AltoClefController mod) {
        LivingEntity player = mod.getPlayer();
        return (adris.altoclef.player2api.status.AgentStatus) (new adris.altoclef.player2api.status.AgentStatus())
                .add("health", String.format("%.2f/20", Float.valueOf(player.getHealth())))
                .add("food", String.format("%.2f/20", Float.valueOf(mod.getBaritone().getEntityContext().hungerManager().getFoodLevel())))
                .add("saturation", String.format("%.2f/20", Float.valueOf(mod.getBaritone().getEntityContext().hungerManager().getSaturationLevel())))
                .add("inventory", StatusUtils.getInventoryString(mod))
                .add("taskStatus", StatusUtils.getTaskStatusString(mod))
                .add("oxygenLevel", StatusUtils.getOxygenString(mod))
                .add("armor", StatusUtils.getEquippedArmorStatusString(mod))
                .add("gamemode", StatusUtils.getGamemodeString(mod));
    }
}
