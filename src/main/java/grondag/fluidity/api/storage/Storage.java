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

import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.transact.Transactor;

public interface Storage<T, U, V extends ArticleView<T>> extends Transactor {
	boolean isEmpty();

	boolean hasDynamicSlots();
	
	int slotCount();
	
	V view(int slot);
	
	default boolean isSlotVisibleFrom(U connection) {
		return true;
	}
	
	default void forEach(U connection, Predicate<V> filter, Predicate<V> action) {
		final int limit = slotCount();
		
		for (int i = 0; i < limit; i++) {
			final V article = view(i);
			
			if (!article.isEmpty() && filter.test(article)) {
				if (!action.test(article)) break;
			}
		}
	}

	default void forEach(U connection, Predicate<V> action) {
		forEach(connection, Predicates.alwaysTrue(), action);
	}

	default void forEach(Predicate<V> action) {
		forEach(null, Predicates.alwaysTrue(), action);
	}

	void startListening(Consumer<V> listener, U connection, Predicate<V> articleFilter);

	void stopListening(Consumer<V> listener);
	
	long accept(T article, long count, boolean simulate);

	long supply(T article, long count, boolean simulate);
	
	FractionView accept(T article, FractionView volume, boolean simulate);

	FractionView supply(T article, FractionView volume, boolean simulate);
}
