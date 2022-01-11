/*******************************************************************************
 * Copyright 2019, 2020 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.fluidity.impl.device;

import grondag.fluidity.api.device.EntityComponentContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

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
