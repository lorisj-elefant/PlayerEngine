package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClefController;
import adris.altoclef.chains.MobDefenseChain;
import adris.altoclef.mixins.LivingEntityMixin;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public abstract class AbstractKillEntityTask extends AbstractDoToEntityTask {
    private static final double OTHER_FORCE_FIELD_RANGE = 2.0D;

    private static final double CONSIDER_COMBAT_RANGE = 10.0D;

    protected AbstractKillEntityTask() {
        this(10.0D, 2.0D);
    }

    protected AbstractKillEntityTask(double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    protected AbstractKillEntityTask(double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    public static Item bestWeapon(AltoClefController mod) {
        ToolItem toolItem1 = null;
        List<ItemStack> invStacks = mod.getItemStorage().getItemStacksPlayerInventory(true);
        ToolItem toolItem2 = MobDefenseChain.getBestWeapon(mod);
        if (toolItem2 != null)
            return (Item) toolItem2;
        Item item = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot(mod.getInventory())).getItem();
        float bestDamage = Float.NEGATIVE_INFINITY;
        if (item instanceof ToolItem) {
            ToolItem handToolItem = (ToolItem) item;
            bestDamage = handToolItem.getMaterial().getAttackDamage();
        }
        for (ItemStack invStack : invStacks) {
            Item item1 = invStack.getItem();
            if (item1 instanceof ToolItem) {
                ToolItem toolItem = (ToolItem) item1;
                float itemDamage = toolItem.getMaterial().getAttackDamage();
                if (itemDamage > bestDamage) {
                    toolItem1 = toolItem;
                    bestDamage = itemDamage;
                }
            }
        }
        return (Item) toolItem1;
    }

    public static boolean equipWeapon(AltoClefController mod) {
        Item bestWeapon = bestWeapon(mod);
        Item equipedWeapon = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot(mod.getInventory())).getItem();
        if (bestWeapon != null && bestWeapon != equipedWeapon) {
            mod.getSlotHandler().forceEquipItem(bestWeapon);
            return true;
        }
        return false;
    }

    protected Task onEntityInteract(AltoClefController mod, Entity entity) {
        if (!equipWeapon(mod)) {
            float hitProg = getAttackCooldownProgress(mod.getPlayer(), 0.0F);
            if (hitProg >= 1.0F && (mod.getPlayer().isOnGround() || mod.getPlayer().getVelocity().getY() < 0.0D || mod.getPlayer().isTouchingWater())) {
                LookHelper.lookAt(mod, entity.getEyePos());
                mod.getControllerExtras().attack(entity);
            }
        }
        return null;
    }

    public float getAttackCooldownProgressPerTick(LivingEntity entity) {
        return (float) ((double) 1.0F / 4 * (double) 20.0F);
    }

    public float getAttackCooldownProgress(LivingEntity entity, float baseTime) {
        return MathHelper.clamp(((float) ((LivingEntityMixin) entity).getLastAttackedTicks() + baseTime) / this.getAttackCooldownProgressPerTick(entity), 0.0F, 1.0F);
    }
}
