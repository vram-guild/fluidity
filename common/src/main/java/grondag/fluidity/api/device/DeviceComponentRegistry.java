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

package grondag.fluidity.api.device;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.resources.ResourceLocation;

import grondag.fluidity.impl.device.DeviceComponentRegistryImpl;

/**
 * Creates and retrieves {@code DeviceComponentType} instances.
 *
 * <p>Because component types are simple and server-side-only this is currently
 * implemented as a simple ID:instance map and not an actual {@code Registry}.
 *
 * @see <a href="https://github.com/grondag/fluidity#device-components">https://github.com/grondag/fluidity#device-components</a>
 */
@Experimental
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
	<T> DeviceComponentType<T> createComponent(ResourceLocation id, T absentValue);

	/**
	 * Returns the {@code DeviceComponentType} instance associated with the given id, or {@code null} if not found.
	 *
	 * @param <T> Type parameter identifying the {@code Class} of the actual component instance
	 * @param id Name-spaced id for the component to be found
	 * @return the {@code DeviceComponentType} instance associated with the given id
	 */
	<T> DeviceComponentType<T> getComponent(ResourceLocation id);

	/**
	 * The singleton DeviceComponentRegistry instance.
	 */
	DeviceComponentRegistry INSTANCE = DeviceComponentRegistryImpl.INSTANCE;
}
