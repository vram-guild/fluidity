/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.impl.device;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import grondag.fluidity.api.device.EntityComponentContext;

@SuppressWarnings("rawtypes")
public final class EntityComponentContextImpl extends AbstractComponentContextImpl implements EntityComponentContext {
	private Entity entity;

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> E entity() {
		return (E) entity;
	}

	@Override
	protected Level getWorldLazily() {
		return entity.level;
	}

	@SuppressWarnings("unchecked")
	private EntityComponentContextImpl prepare(DeviceComponentTypeImpl componentType, Entity entity) {
		this.componentType = componentType;
		this.entity = entity;
		mapping = componentType.getMapping(entity);
		return this;
	}

	private static final ThreadLocal<EntityComponentContextImpl> POOL = ThreadLocal.withInitial(EntityComponentContextImpl::new);

	static EntityComponentContextImpl get(DeviceComponentTypeImpl componentType, Entity entity) {
		return POOL.get().prepare(componentType, entity);
	}
}
