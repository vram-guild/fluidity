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

import java.util.function.Function;

import org.apache.commons.lang3.ObjectUtils;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import grondag.fluidity.api.device.Authorization;
import grondag.fluidity.api.device.ComponentContext;
import grondag.fluidity.api.device.DeviceComponentAccess;
import grondag.fluidity.api.device.DeviceComponentType;

@SuppressWarnings("rawtypes")
abstract class AbstractComponentContextImpl implements ComponentContext, DeviceComponentAccess {
	protected DeviceComponentTypeImpl componentType;
	protected Function<ComponentContext, ?> mapping;
	protected Level world;
	protected ResourceLocation id;
	protected Direction side;
	protected Authorization auth;

	@Override
	public final Object get(Authorization auth, Direction side, ResourceLocation id) {
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
	public final ResourceLocation id() {
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

	protected abstract Level getWorldLazily();

	@Override
	public final Level world() {
		Level result = world;

		if (result == null) {
			result = getWorldLazily();
			world = result;
		}

		return result;
	}
}
