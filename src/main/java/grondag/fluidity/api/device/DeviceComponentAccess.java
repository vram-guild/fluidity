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

@API(status = Status.EXPERIMENTAL)
public interface DeviceComponentAccess<T> {
	DeviceComponentType<T> componentType();

	@Nullable T get(Authorization auth, @Nullable Direction side, @Nullable Identifier id);

	default @Nullable T get(@Nullable Direction side, @Nullable Identifier id) {
		return get(Authorization.PUBLIC, side, id);
	}

	default @Nullable T get(@Nullable Direction side) {
		return get(Authorization.PUBLIC, side, null);
	}

	default @Nullable T get(@Nullable Identifier id) {
		return get(Authorization.PUBLIC, null, id);
	}

	default @Nullable T get(Authorization auth) {
		return get(auth, null, null);
	}

	default @Nullable T get() {
		return get(Authorization.PUBLIC, null, null);
	}

	default boolean acceptIfPresent(Authorization auth, @Nullable Direction side, @Nullable Identifier id, Consumer<T> action) {
		final T svc = get(auth, side, id);

		if(svc != componentType().absent()) {
			action.accept(svc);
			return true;
		}

		return false;
	}

	default boolean acceptIfPresent(@Nullable Direction side, @Nullable Identifier id, Consumer<T> action) {
		return acceptIfPresent(Authorization.PUBLIC, side, id, action);
	}

	default boolean acceptIfPresent(@Nullable Direction side, Consumer<T> action) {
		return acceptIfPresent(Authorization.PUBLIC, side, null, action);
	}

	default boolean acceptIfPresent(@Nullable Identifier id, Consumer<T> action) {
		return acceptIfPresent(Authorization.PUBLIC, null, id, action);
	}

	default boolean acceptIfPresent(Authorization auth, Consumer<T> action) {
		return acceptIfPresent(auth, null, null, action);
	}

	default boolean acceptIfPresent(Consumer<T> action) {
		return acceptIfPresent(Authorization.PUBLIC, null, null, action);
	}

	default <V> V applyIfPresent(Authorization auth, @Nullable Direction side, @Nullable Identifier id, Function<T, V> function) {
		final T svc = get(auth, side, id);

		if(svc != componentType().absent()) {
			return function.apply(svc);
		}

		return null;
	}

	default <V> V applyIfPresent(@Nullable Direction side, @Nullable Identifier id, Function<T, V> function) {
		return applyIfPresent(Authorization.PUBLIC, side, id, function);
	}

	default <V> V applyIfPresent(@Nullable Direction side, Function<T, V> function) {
		return applyIfPresent(Authorization.PUBLIC, side, null, function);
	}

	default <V> V applyIfPresent(@Nullable Identifier id, Function<T, V> function) {
		return applyIfPresent(Authorization.PUBLIC, null, id, function);
	}

	default <V> V applyIfPresent(Authorization auth, Function<T, V> function) {
		return applyIfPresent(auth, null, null, function);
	}

	default <V> V applyIfPresent(Function<T, V> function) {
		return applyIfPresent(Authorization.PUBLIC, null, null, function);
	}
}
