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
package io.github.ladysnake.otomaton.companion;

import adris.altoclef.AltoClefController;
import adris.altoclef.player2api.Character;
import adris.altoclef.player2api.utils.CharacterUtils;
import baritone.api.IBaritone;
import baritone.api.entity.IAutomatone;
import baritone.api.entity.IHungerManagerProvider;
import baritone.api.entity.IInteractionManagerProvider;
import baritone.api.entity.IInventoryProvider;
import baritone.api.entity.LivingEntityHungerManager;
import baritone.api.entity.LivingEntityInteractionManager;
import baritone.api.entity.LivingEntityInventory;
import io.github.ladysnake.otomaton.Otomaton;
import io.github.ladysnake.otomaton.network.AutomatonSpawnPacket;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class AutomatoneEntity extends LivingEntity implements IAutomatone, IInventoryProvider, IInteractionManagerProvider, IHungerManagerProvider {
    public LivingEntityInteractionManager manager;
    public LivingEntityInventory inventory;
    public LivingEntityHungerManager hungerManager;
    public AltoClefController controller;
    public Character character;
    public Identifier textureLocation;
    protected Vec3d lastVelocity;

    public AutomatoneEntity(EntityType<? extends AutomatoneEntity> type, World world) {
        super(type, world);
        init();
        setCharacter(CharacterUtils.requestFirstCharacter());
    }

    public void init(){
        this.setStepHeight(0.6f);
        setMovementSpeed(0.4f);
        manager = new LivingEntityInteractionManager(this);
        inventory = new LivingEntityInventory(this);
        hungerManager = new LivingEntityHungerManager();
        if(!getWorld().isClient) {
            controller = new AltoClefController(IBaritone.KEY.get(this));
            controller.getAiBridge().sendGreeting(character);
        }
    }

    public AutomatoneEntity(World world, Character character){
        super(Otomaton.AUTOMATONE, world);
        setCharacter(character);
        init();
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
        this.lastVelocity = this.getVelocity();
        manager.update();
        inventory.updateItems();
        //hungerManager.update(this);
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
        this.headYaw = this.getYaw();
        pickupItems();
    }

    public void pickupItems(){
        if (!this.getWorld().isClient && this.isAlive() && !this.dead && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            Vec3i vec3i  = new Vec3i(1, 0, 1);
            for(ItemEntity itemEntity : this.getWorld().getNonSpectatingEntities(ItemEntity.class, this.getBoundingBox().expand((double)vec3i.getX(), (double)vec3i.getY(), (double)vec3i.getZ()))) {
                if (!itemEntity.isRemoved() && !itemEntity.getStack().isEmpty() && !itemEntity.cannotPickup()) {
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
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        lastAttackedTicks = 0;
        float f = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float g = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        if (target instanceof LivingEntity) {
            f += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), ((LivingEntity)target).getGroup());
            g += (float)EnchantmentHelper.getKnockback(this);
        }

        int i = EnchantmentHelper.getFireAspect(this);
        if (i > 0) {
            target.setOnFireFor(i * 4);
        }

        boolean bl = target.damage(this.getDamageSources().mobAttack(this), f);
        if (bl) {
            if (g > 0.0F && target instanceof LivingEntity) {
                ((LivingEntity)target).takeKnockback((double)(g * 0.5F), (double) MathHelper.sin(this.getYaw() * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(this.getYaw() * ((float)Math.PI / 180F))));
                this.setVelocity(this.getVelocity().multiply(0.6, (double)1.0F, 0.6));
            }

//            if (target instanceof PlayerEntity) {
//                PlayerEntity playerEntity = (PlayerEntity)target;
//                this.disablePlayerShield(playerEntity, this.getMainHandStack(), playerEntity.isUsingItem() ? playerEntity.getActiveItem() : ItemStack.EMPTY);
//            }

            this.applyDamageEffects(this, target);
            this.onAttacking(target);
        }

        return bl;
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
    public LivingEntityHungerManager getHungerManager() {
        return hungerManager;
    }

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public Vec3d lerpVelocity(float delta) {
        return this.lastVelocity.lerp(this.getVelocity(), (double)delta);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return AutomatonSpawnPacket.create(this);
    }

    @Override
    public Text getDisplayName() {
        if(character==null){
            return super.getDisplayName();
        }
        return Text.literal(character.shortName);
    }
}
