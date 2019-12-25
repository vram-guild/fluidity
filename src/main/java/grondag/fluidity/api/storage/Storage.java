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

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.ArticleView;
import grondag.fluidity.api.transact.Transactor;

/**
 * Flexible storage interface for tanks, containers.
 * Interface supports both discrete items and bulk resources (such as fluids.)
 */
@API(status = Status.EXPERIMENTAL)
public interface Storage<A extends ArticleView, L extends StorageListener<L>> extends Transactor {
	int handleCount();

	default boolean isEmpty() {
		final int size = handleCount();

		for (int i = 0; i < size; i++) {
			if (!view(i).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	default boolean isHandleValid(int handle) {
		return handle >=0  && handle < handleCount();
	}

	@Nullable A view(int handle);

	default boolean isHandleVisibleFrom(Object connection) {
		return true;
	}

	default void forEach(@Nullable Object connection, Predicate<A> filter, Predicate<A> action) {
		final int limit = handleCount();

		for (int i = 0; i < limit; i++) {
			final A article = view(i);

			if (!article.isEmpty() && filter.test(article)) {
				if (!action.test(article)) {
					break;
				}
			}
		}
	}

	default void forEach(Predicate<A> filter, Predicate<A> action) {
		forEach(null, filter, action);
	}

	default void forEach(@Nullable Object connection, Predicate<A> action) {
		forEach(connection, Predicates.alwaysTrue(), action);
	}

	default void forEach(Predicate<A> action) {
		forEach(null, Predicates.alwaysTrue(), action);
	}

	Iterable<L> listeners();

	void startListening(L listener);

	void stopListening(L listener);

	Predicate <? super ArticleView> NOT_EMPTY = a -> !a.isEmpty();

	CompoundTag writeTag();

	void readTag(CompoundTag tag);

	String TAG_ITEMS = "items";
}
