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

import java.util.function.Function;

import org.apache.commons.lang3.ObjectUtils;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import grondag.fluidity.api.device.Authorization;
import grondag.fluidity.api.device.ComponentContext;
import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;

@SuppressWarnings("rawtypes")
abstract class AbstractComponentContextImpl implements ComponentContext, DeviceComponentAccess {
	protected DeviceComponentTypeImpl componentType;
	protected Function<ComponentContext, ?> mapping;
	protected World world;
	protected Identifier id;
	protected Direction  side;
	protected Authorization auth;

	@Override
	public final Object get(Authorization auth, Direction side, Identifier id) {
		this.auth = auth;
		this.side = side;
		this.id = id;
		return ObjectUtils.defaultIfNull(mapping.apply(this), componentType.absent());
	}

	@Override
	public final DeviceComponentType componentType() {
		return componentType;
	}

	@Override
	public final Identifier id() {
		return id;
	}

	@Override
	public final Direction side() {
		return side;
	}

	@Override
	public final Authorization auth() {
		return auth;
	}

	protected abstract World getWorldLazily();

	@Override
	public final World world() {
		World result = world;

		if(result == null) {
			result = getWorldLazily();
			world = result;
		}

		return result;
	}
}
