/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package baritone.api.fakeplayer;

import baritone.api.utils.IEntityAccessor;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AutomatoneEntity extends ZombieEntity implements IAutomatone, IInventoryProvider, IInteractionManagerProvider {

    protected @Nullable GameProfile displayProfile;
    private boolean release;
    public LivingEntityInteractionManager manager;
    public LivingEntityInventory inventory;

    public AutomatoneEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
        ((IEntityAccessor)this).automatone$setType(type);
        this.setStepHeight(0.6f); // same step height as LivingEntity
        manager = new LivingEntityInteractionManager(this);
        inventory = new LivingEntityInventory(this);
        setCanPickUpLoot(true);
    }

    @Override
    public LivingEntityInventory getLivingInventory() {
        return inventory;
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    @Override
    public void tick() {
        manager.update();
        inventory.updateItems();
        //this.closeHandledScreen();
        goalSelector.clear((g)->true);
        targetSelector.clear((t)->true);
        setCanPickUpLoot(true);
        super.tick();
        //this.playerTick();
    }

    @Override
    protected void loot(ItemEntity itemEntity) {
        if (!this.getWorld().isClient) {
            ItemStack itemStack = itemEntity.getStack();
            Item item = itemStack.getItem();
            int i = itemStack.getCount();
            if (this.getLivingInventory().insertStack(itemStack)) {
                this.sendPickup(itemEntity, i);
                if (itemStack.isEmpty()) {
                    itemEntity.discard();
                    itemStack.setCount(i);
                }
            }
        }
    }

    @Override
    public void tickMovement() {
        if (this.isTouchingWater() && this.isSneaking() && this.shouldSwimInFluids()) {
            this.knockDownwards();
        }
        super.tickMovement();
    }

    @Override
    protected void sendAiDebugData() {
        super.sendAiDebugData();
        if (this.release) {
            this.clearActiveItem();
            this.release = false;
        }
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
        if (this.velocityModified) {
            super.takeKnockback(strength, x, z);
        }
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return getLivingInventory().armor;
    }

    /**
     * Controls whether this should be considered a player for ticking and tracking purposes
     *
     * <p>We want fake players to behave like regular entities, so for once we pretend they are not players.
     */
    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public Text getName() {
        return Text.literal("Automatone");
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        if (tag.contains("automatone:display_profile", NbtType.COMPOUND)) {
            this.displayProfile = NbtHelper.toGameProfile(tag.getCompound("automatone:display_profile"));
        }
        if (tag.contains("head_yaw")) {
            this.headYaw = tag.getFloat("head_yaw");
        }
        NbtList nbtList = tag.getList("Inventory", 10);
        this.inventory.readNbt(nbtList);
        this.inventory.selectedSlot = tag.getInt("SelectedItemSlot");
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        if (this.displayProfile != null) {
            tag.put("automatone:display_profile", NbtHelper.writeGameProfile(new NbtCompound(), this.displayProfile));
        }
        tag.putFloat("head_yaw", this.headYaw);
        tag.put("Inventory", this.inventory.writeNbt(new NbtList()));
        tag.putInt("SelectedItemSlot", this.inventory.selectedSlot);
    }

    @Override
    public LivingEntityInteractionManager getInteractionManager() {
        return manager;
    }

    public ItemStack getEquippedStack(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.inventory.getMainHandStack();
        } else if (slot == EquipmentSlot.OFFHAND) {
            return this.inventory.offHand.get(0);
        } else {
            return slot.getType() == EquipmentSlot.Type.ARMOR ? this.inventory.armor.get(slot.getEntitySlotId()) : ItemStack.EMPTY;
        }
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {

    }
}
