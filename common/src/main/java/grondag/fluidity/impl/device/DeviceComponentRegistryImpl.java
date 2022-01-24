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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.resources.ResourceLocation;

import grondag.fluidity.api.device.DeviceComponentRegistry;
import grondag.fluidity.api.device.DeviceComponentType;

public class DeviceComponentRegistryImpl implements DeviceComponentRegistry {
	private DeviceComponentRegistryImpl() { }

	public static final DeviceComponentRegistry INSTANCE = new DeviceComponentRegistryImpl();

	private static final Object2ObjectOpenHashMap<ResourceLocation, DeviceComponentType<?>> TYPES_BY_ID = new Object2ObjectOpenHashMap<>();

	@Override
	public <T> DeviceComponentType<T> createComponent(ResourceLocation id, T absentValue) {
		Preconditions.checkState(!TYPES_BY_ID.containsKey(id), "Device component already registered with ID " + id.toString());
		final DeviceComponentType<T> result = new DeviceComponentTypeImpl<>(absentValue);
		TYPES_BY_ID.put(id, result);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> DeviceComponentType<T> getComponent(ResourceLocation id) {
		return (DeviceComponentType<T>) TYPES_BY_ID.get(id);
	}
}
