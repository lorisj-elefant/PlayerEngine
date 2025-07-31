/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

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
package io.github.ladysnake.otomaton;

import adris.altoclef.AltoClefController;
import baritone.api.IBaritone;
import baritone.api.entity.IAutomatone;
import baritone.api.entity.IHungerManagerProvider;
import baritone.api.entity.IInteractionManagerProvider;
import baritone.api.entity.IInventoryProvider;
import baritone.api.entity.LivingEntityHungerManager;
import baritone.api.entity.LivingEntityInteractionManager;
import baritone.api.entity.LivingEntityInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Arm;
import net.minecraft.world.World;

public class AutomatoneEntity extends ZombieEntity implements IAutomatone, IInventoryProvider, IInteractionManagerProvider, IHungerManagerProvider {
    public LivingEntityInteractionManager manager;
    public LivingEntityInventory inventory;
    public LivingEntityHungerManager hungerManager;
    public AltoClefController controller;

    public AutomatoneEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
        this.setStepHeight(0.6f);
        manager = new LivingEntityInteractionManager(this);
        inventory = new LivingEntityInventory(this);
        hungerManager = new LivingEntityHungerManager();
        setCanPickUpLoot(true);
        if(!world.isClient)
            controller = new AltoClefController(IBaritone.KEY.get(this));
        lookControl = new LookControl(this){
            @Override
            public void tick() {

            }
        };
    }

    @Override
    public LivingEntityInventory getLivingInventory() {
        return inventory;
    }

    @Override
    public LivingEntityInteractionManager getInteractionManager() {
        return manager;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
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
        tag.putFloat("head_yaw", this.headYaw);
        tag.put("Inventory", this.inventory.writeNbt(new NbtList()));
        tag.putInt("SelectedItemSlot", this.inventory.selectedSlot);
    }

    @Override
    public void tick() {
        manager.update();
        inventory.updateItems();
        //hungerManager.update(this);
        goalSelector.clear((g)->true);
        targetSelector.clear((t)->true);
        setCanPickUpLoot(true);
        lastAttackedTicks++;
        if(!this.getWorld().isClient)
            controller.serverTick();
        super.tick();
    }

    @Override
    public void tickMovement() {
        if (this.isTouchingWater() && this.isSneaking() && this.shouldSwimInFluids()) {
            this.knockDownwards();
        }
        super.tickMovement();
    }

    @Override
    public boolean tryAttack(Entity target) {
        lastAttackedTicks = 0;
        return super.tryAttack(target);
    }

    @Override
    protected void loot(ItemEntity itemEntity) {
        if (!this.getWorld().isClient) {
            ItemStack itemStack = itemEntity.getStack();
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

    protected void swimUpward(TagKey<Fluid> tag) {
        this.setVelocity(this.getVelocity().add((double)0.0F, (double)0.04F, (double)0.0F));
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
        if (this.velocityModified) {
            super.takeKnockback(strength, x, z);
        }
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return getLivingInventory().armor;
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
        if (slot == EquipmentSlot.MAINHAND) {
            this.inventory.setStack(this.inventory.selectedSlot, stack);
        } else if (slot == EquipmentSlot.OFFHAND) {
            this.inventory.offHand.set(0, stack);
        } else if(slot.getType() == EquipmentSlot.Type.ARMOR){
            inventory.armor.set(slot.getEntitySlotId(), stack);
        }
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    @Override
    public LivingEntityHungerManager getHungerManager() {
        return hungerManager;
    }
}
