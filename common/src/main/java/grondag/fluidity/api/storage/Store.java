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
package grondag.fluidity.api.storage;

import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import com.google.common.base.Predicates;
import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.device.DeviceComponentRegistry;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.transact.TransactionParticipant;
import grondag.fluidity.api.util.AmbiguousBoolean;
import grondag.fluidity.impl.Fluidity;
import grondag.fluidity.impl.storage.CreativeStore;
import grondag.fluidity.impl.storage.EmptyStore;
import grondag.fluidity.impl.storage.VoidStore;

/**
 * Flexible storage interface for tanks, containers.
 * Interface supports both discrete items and bulk resources (such as fluids.)
 *
 * @see <a href="https://github.com/grondag/fluidity#store-and-its-variants">https://github.com/grondag/fluidity#store-and-its-variants</a>
 */
@Experimental
public interface Store extends TransactionParticipant {
	default ArticleFunction getConsumer() {
		return ArticleFunction.ALWAYS_RETURN_ZERO;
	}

	default boolean hasConsumer() {
		return getConsumer().canApply();
	}

	default ArticleFunction getSupplier() {
		return ArticleFunction.ALWAYS_RETURN_ZERO;
	}

	default boolean hasSupplier() {
		return getSupplier().canApply();
	}

	int handleCount();

	default boolean isHandleValid(int handle) {
		return handle >=0  && handle < handleCount();
	}

	/**
	 *
	 * @param handle
	 * @return View of article store at handle if handle is valid - view may be empty.
	 * For invalid handles, storage implementations should return {@link StoredArticleView#EMPTY}.
	 */
	StoredArticleView view(int handle);

	/**
	 * True if Store is a virtual view of some other storage.
	 * Can be used by aggregate views to exclude cycles.
	 * Should generally be true for aggregate views.
	 */
	default boolean isView() {
		return false;
	}

	/**
	 * For stores that are views, this is the underlying store.
	 * Note that the underlying store may also be a view and
	 * views that cannot map to a single store - like aggregate views
	 * - should return self. (The default implementation.)
	 */
	default Store viewOwner() {
		return this;
	}

	/**
	 * True if Store is an aggregate view of potentially multiple Stores.
	 */
	default boolean isAggregate() {
		return false;
	}

	default void forEach(Predicate<? super StoredArticleView> filter, Predicate<? super StoredArticleView> action) {
		final int limit = handleCount();

		for (int i = 0; i < limit; i++) {
			final StoredArticleView article = view(i);

			if (!article.isEmpty() && filter.test(article)) {
				if (!action.test(article)) {
					break;
				}
			}
		}
	}

	default void forEach(Predicate<? super StoredArticleView> action) {
		forEach(Predicates.alwaysTrue(), action);
	}

	/**
	 * Can be used to shortcut accept requests and is useful for bulk storage to
	 * distinguish between having too small units to honor supply requests vs.
	 * being truly full.
	 *
	 * <p>For views, this reflects the state of the view and not the underlying storage.
	 *
	 * @return {@code true} When the storage constraints are reached such that
	 * any request to accept more will return zerp.
	 */
	boolean isFull();

	/**
	 * Can be used to shortcut supply requests and is useful for bulk storage to
	 * distinguish between having too little content to honor supply requests vs.
	 * being truly empty.
	 *
	 * <p>For views, this reflects the state of the view and not the underlying storage.
	 *
	 * @return {@code true} When the storage has nothing in it.
	 */
	boolean isEmpty();

	long count();

	default long countOf(Article item)  {
		return getSupplier().apply(item, Long.MAX_VALUE, true);
	}

	Fraction amount();

	default Fraction amountOf(Article item)  {
		return getSupplier().apply(item, Fraction.MAX_VALUE, true);
	}

	/**
	 * DO NOT RETAIN A REFERENCE.
	 * @return View of a single article that is in this store, or {@link StoredArticleView#EMPTY} if store is empty.
	 */
	default StoredArticleView getAnyArticle() {
		if (isEmpty()) {
			return StoredArticleView.EMPTY;
		} else {
			final int limit = handleCount();

			for (int i = 0; i < limit; ++i) {
				final StoredArticleView a = view(i);

				if (!a.isEmpty()) {
					return a;
				}
			}
		}

		return StoredArticleView.EMPTY;
	}

	/**
	 * DO NOT RETAIN A REFERENCE.
	 * @return View of a single, non-empty matching article that is in this store, or {@link StoredArticleView#EMPTY} if store is empty.
	 */
	default StoredArticleView getAnyMatch(Predicate<? super StoredArticleView> test) {
		if (isEmpty()) {
			return StoredArticleView.EMPTY;
		} else {
			final int limit = handleCount();

			for (int i = 0; i < limit; ++i) {
				final StoredArticleView a = view(i);

				if (!a.isEmpty() && test.test(a)) {
					return a;
				}
			}
		}

		return StoredArticleView.EMPTY;
	}

	long capacity();

	Fraction volume();

	double usage();

	void clear();

	default StorageEventStream eventStream() {
		return StorageEventStream.UNSUPPORTED;
	}

	default boolean hasEventStream() {
		return eventStream() != StorageEventStream.UNSUPPORTED;
	}

	/**
	 * Should become false if store is broken or destroyed, goes offline, etc.
	 * Intended use is to disconnect non-listening ScreenHandlers.
	 */
	default boolean isValid() {
		return true;
	}

	CompoundTag writeTag();

	void readTag(CompoundTag tag);

	Predicate <? super StoredArticleView> NOT_EMPTY = a -> !a.isEmpty();

	Store EMPTY = EmptyStore.INSTANCE;
	Store VOID = VoidStore.INSTANCE;
	Store CREATIVE = CreativeStore.INSTANCE;

	DeviceComponentType<Store> STORAGE_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new ResourceLocation(Fluidity.MOD_ID, "storage"), EMPTY);

	/**
	 * Multiblock storage devices may elect to return the compound storage instance as the main storage service.
	 * This method offers an unambiguous way to reference the internal storage of the device.
	 *
	 * <p>Also used by and necessary for aggregate storage implementations for the same reason.
	 *
	 * @return Internal {@link Store} of this device, or the regular storage if not a multiblock.
	 */
	DeviceComponentType<Store> INTERNAL_STORAGE_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new ResourceLocation(Fluidity.MOD_ID, "internal_storage"), EMPTY);

	/**
	 * Indicates if store could potentially supply or consume article of given type.
	 * Strongly advised to override to allow optimized usage of stores.
	 * @param type Article type of interest.
	 * @return Indicator if the store may supply or consume articles of type.
	 */
	default AmbiguousBoolean allowsType(ArticleType<?> type) {
		return AmbiguousBoolean.MAYBE;
	}
}
