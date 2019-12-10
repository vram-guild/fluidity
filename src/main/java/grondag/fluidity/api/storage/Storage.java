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

import grondag.fluidity.api.storage.view.ArticleView;
import grondag.fluidity.api.transact.Transactor;

/**
 * Flexible storage interface for tanks, containers.
 * Interface supports both discrete items and bulk resources (such as fluids.)
 *
 * This allows for
 */
public interface Storage extends Transactor {
	boolean isEmpty();

	boolean hasDynamicSlots();

	int slotCount();

	<T extends ArticleView> T view(int slot);

	default boolean isSlotVisibleFrom(Object connection) {
		return true;
	}

	default void forEach(Object connection, Predicate<? super ArticleView> filter, Predicate<? super ArticleView> action) {
		final int limit = slotCount();

		for (int i = 0; i < limit; i++) {
			final ArticleView article = view(i);

			if (!article.isEmpty() && filter.test(article)) {
				if (!action.test(article)) {
					break;
				}
			}
		}
	}

	default void forEach(Object connection, Predicate<? super ArticleView> action) {
		forEach(connection, Predicates.alwaysTrue(), action);
	}

	default void forEach(Predicate<? super ArticleView> action) {
		forEach(null, Predicates.alwaysTrue(), action);
	}

	void startListening(Consumer<? super ArticleView> listener, Object connection, Predicate<? super ArticleView> articleFilter);

	void stopListening(Consumer<? super ArticleView> listener);
}
