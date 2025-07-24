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

package baritone;

import baritone.command.defaults.DefaultCommands;
import baritone.command.manager.BaritoneArgumentType;
import baritone.entity.CustomFishingBobberEntity;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.command.argument.SingletonArgumentInfo;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.ServerArgumentType;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@KeepName
public final class Automatone implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Automatone");
    public static final String MOD_ID = "automatone";

    public static final TagKey<Item> EMPTY_BUCKETS = TagKey.of(RegistryKeys.ITEM, id("empty_buckets"));
    public static final TagKey<Item> WATER_BUCKETS = TagKey.of(RegistryKeys.ITEM, id("water_buckets"));

    private static final ThreadPoolExecutor threadPool;

    static {
        AtomicInteger threadCounter = new AtomicInteger(0);
        threadPool = new ThreadPoolExecutor(4, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> new Thread(r, "Automatone Worker " + threadCounter.incrementAndGet()));
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static ThreadPoolExecutor getExecutor() {
        return threadPool;
    }

    public static final EntityType<CustomFishingBobberEntity> FISHING_BOBBER = FabricEntityTypeBuilder.<CustomFishingBobberEntity>create()
            .spawnGroup(SpawnGroup.MISC)
            .entityFactory(CustomFishingBobberEntity::new)
//            .defaultAttributes(ProjectileEntity::createAttributes)
            .dimensions(EntityDimensions.changing(EntityType.FISHING_BOBBER.getWidth(), EntityType.FISHING_BOBBER.getHeight()))
            .trackRangeBlocks(64)
            .trackedUpdateRate(1)
            .forceTrackedVelocityUpdates(true)
            .build();

    @Override
    public void onInitialize(ModContainer mod) {
        DefaultCommands.registerAll();
        Registry.register(Registries.ENTITY_TYPE, id("fishing_bobber"), FISHING_BOBBER);
        ServerArgumentType.register(id("command"), BaritoneArgumentType.class, SingletonArgumentInfo.contextFree(BaritoneArgumentType::baritone), t -> StringArgumentType.greedyString());
    }
}
