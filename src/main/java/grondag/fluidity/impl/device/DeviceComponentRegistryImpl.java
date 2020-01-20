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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.util.Identifier;

import grondag.fluidity.api.device.DeviceComponentRegistry;
import grondag.fluidity.api.device.DeviceComponentType;

public class DeviceComponentRegistryImpl implements DeviceComponentRegistry {
	private DeviceComponentRegistryImpl() { }

	public static final DeviceComponentRegistry INSTANCE = new DeviceComponentRegistryImpl();

	private static final Object2ObjectOpenHashMap<Identifier, DeviceComponentType<?>> TYPES_BY_ID = new Object2ObjectOpenHashMap<>();

	@Override
	public <T> DeviceComponentType<T> createComponent(Identifier id, T absentValue) {
		Preconditions.checkState(!TYPES_BY_ID.containsKey(id), "Device component already registered with ID " + id.toString());
		final DeviceComponentType<T> result = new DeviceComponentTypeImpl<>(absentValue);
		TYPES_BY_ID.put(id, result);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> DeviceComponentType<T> getComponent(Identifier id) {
		return (DeviceComponentType<T>) TYPES_BY_ID.get(id);
	}
}
