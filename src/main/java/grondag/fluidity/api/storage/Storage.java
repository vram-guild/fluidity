/*******************************************************************************
 * Copyright 2019 grondag
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
package grondag.fluidity.api.storage;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.base.Predicates;

import grondag.fluidity.api.transact.Transactor;

public interface Storage<T, V extends ArticleView> extends Transactor {
	boolean isEmpty();

	default boolean fixedSlots() {
		return slotCount() > 0;
	}

	default int slotCount() {
		return 0;
	}

	void forEach(T connection, Predicate<V> filter, Predicate<V> consumer);

	default void forEach(T connection, Predicate<V> consumer) {
		forEach(connection, Predicates.alwaysTrue(), consumer);
	}

	default void forEach(Predicate<V> consumer) {
		forEach(null, Predicates.alwaysTrue(), consumer);
	}

	void forSlot(int slot, Consumer<V> consumer);

	void startListening(Consumer<V> listener, T connection, Predicate<V> articleFilter);

	void stopListening(Consumer<V> listener);
}
