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
package grondag.fluidity.api.device;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.util.Identifier;

import grondag.fluidity.impl.device.DeviceComponentRegistryImpl;

/**
 * Creates and retrieves {@code DeviceComponentType} instances.<p>
 *
 * Because component types are simple and server-side-only this is currently
 * implemented as a simple ID:instance map and not an actual {@code Registry}.
 *
 * @see <a href="https://github.com/grondag/fluidity#device-components">https://github.com/grondag/fluidity#device-components</a>
 */
@API(status = Status.EXPERIMENTAL)
public interface DeviceComponentRegistry {
	/**
	 * Creates and returns a new device component type with the given id and absent value.
	 *
	 * @param <T> Type parameter identifying the {@code Class} of the actual component instance
	 * @param id Name-spaced id for this component
	 * @param absentValue Component value to be returned when a component is not present
	 * @return A new {@code DeviceComponentType} instance
	 *
	 * @throws IllegalStateException if the given id is already in use
	 */
	<T> DeviceComponentType<T> createComponent(Identifier id, T absentValue);

	/**
	 * Returns the {@code DeviceComponentType} instance associated with the given id, or {@code null} if not found.
	 *
	 * @param <T> Type parameter identifying the {@code Class} of the actual component instance
	 * @param id Name-spaced id for the component to be found
	 * @return the {@code DeviceComponentType} instance associated with the given id
	 */
	<T> DeviceComponentType<T> getComponent(Identifier id);

	/**
	 * The singleton DeviceComponentRegistry instance
	 */
	DeviceComponentRegistry INSTANCE = DeviceComponentRegistryImpl.INSTANCE;
}
