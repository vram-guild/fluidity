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

import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Controls access to device components within a component provider (a device) that has already
 * been located within a world via {@link DeviceComponentType#getAccess()}.
 *
 * @param <T> Type parameter for the {@code DeviceComponentType} to which this instance controls access.
 *
 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
 */
@Experimental
public interface DeviceComponentAccess<T> {
	/**
	 * Identifies the type of the device component to which this instance controls access.
	 *
	 * @return The type of the device component to which this instance controls access
	 */
	DeviceComponentType<T> componentType();

	/**
	 * Retrieves the device component with the given access parameters, or {@link DeviceComponentType#absent()} if the component
	 * is missing or inaccessible with the given parameters.
	 *
	 * @param auth Authorization token
	 * @param side Side from which the device component is being accessed
	 * @param id Identifier of a specific component or sub-component within the device
	 * @return The device component accessible via the given parameters
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	T get(Authorization auth, @Nullable Direction side, @Nullable ResourceLocation id);

	/**
	 * Retrieves the device component with the given access parameters, or {@link DeviceComponentType#absent()} if the component
	 * is missing or inaccessible with the given parameters.
	 *
	 * @param side Side from which the device component is being accessed
	 * @param id Identifier of a specific component or sub-component within the device
	 * @return The device component accessible via the given parameters
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default T get(@Nullable Direction side, @Nullable ResourceLocation id) {
		return get(Authorization.PUBLIC, side, id);
	}

	/**
	 * Retrieves the device component with the given access parameters, or {@link DeviceComponentType#absent()} if the component
	 * is missing or inaccessible with the given parameters.
	 *
	 * @param side Side from which the device component is being accessed
	 * @return The device component accessible via the given parameters
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default T get(@Nullable Direction side) {
		return get(Authorization.PUBLIC, side, null);
	}

	/**
	 * Retrieves the device component with the given access parameters, or {@link DeviceComponentType#absent()} if the component
	 * is missing or inaccessible with the given parameters.
	 *
	 * @param id Identifier of a specific component or sub-component within the device
	 * @return The device component accessible via the given parameters
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default T get(@Nullable ResourceLocation id) {
		return get(Authorization.PUBLIC, null, id);
	}

	/**
	 * Retrieves the device component with the given access parameters, or {@link DeviceComponentType#absent()} if the component
	 * is missing or inaccessible with the given parameters.
	 *
	 * @param auth Authorization token
	 * @return The device component accessible via the given parameters
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default T get(Authorization auth) {
		return get(auth, null, null);
	}

	/**
	 * Retrieves the device component with default access parameters, or {@link DeviceComponentType#absent()} if the component
	 * is missing or inaccessible with default parameters.
	 *
	 * @return The device component accessible via the given parameters
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default T get() {
		return get(Authorization.PUBLIC, null, null);
	}

	/**
	 * Retrieves the device component with the given access parameters and applies the
	 * given action if the value is not {@link DeviceComponentType#absent()}.
	 *
	 * @param auth Authorization token
	 * @param side Side from which the device component is being accessed
	 * @param id Identifier of a specific component or sub-component within the device
	 * @param action Action to be applied to a non-absent device component
	 * @return {@code true} if a non-absent component was successfully obtained and the action was applied to it
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default boolean acceptIfPresent(Authorization auth, @Nullable Direction side, @Nullable ResourceLocation id, Consumer<T> action) {
		final T svc = get(auth, side, id);

		if (svc != componentType().absent()) {
			action.accept(svc);
			return true;
		}

		return false;
	}

	/**
	 * Retrieves the device component with the given access parameters and applies the
	 * given action if the value is not {@link DeviceComponentType#absent()}.
	 *
	 * @param side Side from which the device component is being accessed
	 * @param id Identifier of a specific component or sub-component within the device
	 * @param action Action to be applied to a non-absent device component
	 * @return {@code true} if a non-absent component was successfully obtained and the action was applied to it
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default boolean acceptIfPresent(@Nullable Direction side, @Nullable ResourceLocation id, Consumer<T> action) {
		return acceptIfPresent(Authorization.PUBLIC, side, id, action);
	}

	/**
	 * Retrieves the device component with the given access parameters and applies the
	 * given action if the value is not {@link DeviceComponentType#absent()}.
	 *
	 * @param side Side from which the device component is being accessed
	 * @param action Action to be applied to a non-absent device component
	 * @return {@code true} if a non-absent component was successfully obtained and the action was applied to it
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default boolean acceptIfPresent(@Nullable Direction side, Consumer<T> action) {
		return acceptIfPresent(Authorization.PUBLIC, side, null, action);
	}

	/**
	 * Retrieves the device component with the given access parameters and applies the
	 * given action if the value is not {@link DeviceComponentType#absent()}.
	 *
	 * @param id Identifier of a specific component or sub-component within the device
	 * @param action Action to be applied to a non-absent device component
	 * @return {@code true} if a non-absent component was successfully obtained and the action was applied to it
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default boolean acceptIfPresent(@Nullable ResourceLocation id, Consumer<T> action) {
		return acceptIfPresent(Authorization.PUBLIC, null, id, action);
	}

	/**
	 * Retrieves the device component with the given access parameters and applies the
	 * given action if the value is not {@link DeviceComponentType#absent()}.
	 *
	 * @param auth Authorization token
	 * @param action Action to be applied to a non-absent device component
	 * @return {@code true} if a non-absent component was successfully obtained and the action was applied to it
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default boolean acceptIfPresent(Authorization auth, Consumer<T> action) {
		return acceptIfPresent(auth, null, null, action);
	}

	/**
	 * Retrieves the device component with default access parameters and applies the
	 * given action if the value is not {@link DeviceComponentType#absent()}.
	 *
	 * @param action Action to be applied to a non-absent device component
	 * @return {@code true} if a non-absent component was successfully obtained and the action was applied to it
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default boolean acceptIfPresent(Consumer<T> action) {
		return acceptIfPresent(Authorization.PUBLIC, null, null, action);
	}

	/**
	 * Retrieves the device component with the given access parameters and applies the
	 * given function if the component instance is not {@link DeviceComponentType#absent()}, returning the result.
	 *
	 * @param auth Authorization token
	 * @param side Side from which the device component is being accessed
	 * @param id Identifier of a specific component or sub-component within the device
	 * @param function Function to be applied to a non-absent device component
	 * @return Function result if a non-absent component was successfully obtained or {@code null} otherwise
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default <V> V applyIfPresent(Authorization auth, @Nullable Direction side, @Nullable ResourceLocation id, Function<T, V> function) {
		final T svc = get(auth, side, id);

		if (svc != componentType().absent()) {
			return function.apply(svc);
		}

		return null;
	}

	/**
	 * Retrieves the device component with the given access parameters and applies the
	 * given function if the component instance is not {@link DeviceComponentType#absent()}, returning the result.
	 *
	 * @param side Side from which the device component is being accessed
	 * @param id Identifier of a specific component or sub-component within the device
	 * @param function Function to be applied to a non-absent device component
	 * @return Function result if a non-absent component was successfully obtained or {@code null} otherwise
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default <V> V applyIfPresent(@Nullable Direction side, @Nullable ResourceLocation id, Function<T, V> function) {
		return applyIfPresent(Authorization.PUBLIC, side, id, function);
	}

	/**
	 * Retrieves the device component with the given access parameters and applies the
	 * given function if the component instance is not {@link DeviceComponentType#absent()}, returning the result.
	 *
	 * @param side Side from which the device component is being accessed
	 * @param function Function to be applied to a non-absent device component
	 * @return Function result if a non-absent component was successfully obtained or {@code null} otherwise
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default <V> V applyIfPresent(@Nullable Direction side, Function<T, V> function) {
		return applyIfPresent(Authorization.PUBLIC, side, null, function);
	}

	/**
	 * Retrieves the device component with the given access parameters and applies the
	 * given function if the component instance is not {@link DeviceComponentType#absent()}, returning the result.
	 *
	 * @param id Identifier of a specific component or sub-component within the device
	 * @param function Function to be applied to a non-absent device component
	 * @return Function result if a non-absent component was successfully obtained or {@code null} otherwise
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default <V> V applyIfPresent(@Nullable ResourceLocation id, Function<T, V> function) {
		return applyIfPresent(Authorization.PUBLIC, null, id, function);
	}

	/**
	 * Retrieves the device component with the given access parameters and applies the
	 * given function if the component instance is not {@link DeviceComponentType#absent()}, returning the result.
	 *
	 * @param auth Authorization token
	 * @param function Function to be applied to a non-absent device component
	 * @return Function result if a non-absent component was successfully obtained or {@code null} otherwise
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default <V> V applyIfPresent(Authorization auth, Function<T, V> function) {
		return applyIfPresent(auth, null, null, function);
	}

	/**
	 * Retrieves the device component with default access parameters and applies the
	 * given function if the component instance is not {@link DeviceComponentType#absent()}, returning the result.
	 *
	 * @param function Function to be applied to a non-absent device component
	 * @return Function result if a non-absent component was successfully obtained or {@code null} otherwise
	 *
	 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
	 */
	default <V> V applyIfPresent(Function<T, V> function) {
		return applyIfPresent(Authorization.PUBLIC, null, null, function);
	}
}
