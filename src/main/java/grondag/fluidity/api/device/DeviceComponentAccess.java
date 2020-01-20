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

import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

/**
 * Controls access to device components within a component provider (a device) that has already
 * been located within a world via {@link DeviceComponentType#getAccess()}
 *
 * @param <T> Type parameter for the {@code DeviceComponentType} to which this instance controls access.
 *
 * @see <a href="https://github.com/grondag/fluidity#using-components">https://github.com/grondag/fluidity#using-components</a>
 */
@API(status = Status.EXPERIMENTAL)
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
	T get(Authorization auth, @Nullable Direction side, @Nullable Identifier id);

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
	default T get(@Nullable Direction side, @Nullable Identifier id) {
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
	default T get(@Nullable Identifier id) {
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
	default boolean acceptIfPresent(Authorization auth, @Nullable Direction side, @Nullable Identifier id, Consumer<T> action) {
		final T svc = get(auth, side, id);

		if(svc != componentType().absent()) {
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
	default boolean acceptIfPresent(@Nullable Direction side, @Nullable Identifier id, Consumer<T> action) {
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
	default boolean acceptIfPresent(@Nullable Identifier id, Consumer<T> action) {
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
	default <V> V applyIfPresent(Authorization auth, @Nullable Direction side, @Nullable Identifier id, Function<T, V> function) {
		final T svc = get(auth, side, id);

		if(svc != componentType().absent()) {
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
	default <V> V applyIfPresent(@Nullable Direction side, @Nullable Identifier id, Function<T, V> function) {
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
	default <V> V applyIfPresent(@Nullable Identifier id, Function<T, V> function) {
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
