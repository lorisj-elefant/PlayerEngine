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

package adris.altoclef;

import baritone.api.IBaritone;
import baritone.api.entity.AutomatoneEntity;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.util.Identifier;

public final class AltoClefComponents implements EntityComponentInitializer {

    // Ключ для доступа к нашему компоненту-контроллеру
    public static final ComponentKey<AltoClefController> CONTROLLER =
            ComponentRegistry.getOrCreate(new Identifier("altoclef", "controller"), AltoClefController.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // Регистрируем фабрику, которая будет создавать экземпляр AltoClefController
        // для каждой сущности типа AutomatoneEntity.
        // Мы передаем IBaritone этой сущности в конструктор контроллера.
        registry.registerFor(AutomatoneEntity.class, CONTROLLER, entity -> new AltoClefController(IBaritone.KEY.get(entity)));
    }
}