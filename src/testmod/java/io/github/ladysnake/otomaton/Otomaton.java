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

package io.github.ladysnake.otomaton;

import io.github.ladysnake.otomaton.network.AutomatoneSpawnRequestPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class Otomaton implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Otomaton");
    public static final String MOD_ID = "otomaton";

    public static final Identifier SPAWN_PACKET_ID = new Identifier(MOD_ID, "spawn_automatone");
    public static final Identifier SPAWN_REQUEST_PACKET_ID = new Identifier(MOD_ID, "request_spawn_automatone");

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static final EntityType<AutomatoneEntity> AUTOMATONE = FabricEntityTypeBuilder.<AutomatoneEntity>createLiving()
            .spawnGroup(SpawnGroup.MISC)
            .entityFactory(AutomatoneEntity::new)
            .defaultAttributes(ZombieEntity::createAttributes)
            .dimensions(EntityDimensions.changing(EntityType.PLAYER.getWidth(), EntityType.PLAYER.getHeight()))
            .trackRangeBlocks(64)
            .trackedUpdateRate(1)
            .forceTrackedVelocityUpdates(true)
            .build();

    @Override
    public void onInitialize() {
        Registry.register(Registries.ENTITY_TYPE, id("automatone"), AUTOMATONE);

        ServerPlayNetworking.registerGlobalReceiver(SPAWN_REQUEST_PACKET_ID, AutomatoneSpawnRequestPacket::handle);
    }
}
