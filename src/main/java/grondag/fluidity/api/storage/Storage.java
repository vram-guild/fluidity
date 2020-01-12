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

import com.google.common.base.Predicates;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.device.DeviceComponentRegistry;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.transact.TransactionParticipant;
import grondag.fluidity.impl.storage.CreativeStorage;
import grondag.fluidity.impl.storage.EmptyStorage;
import grondag.fluidity.impl.storage.VoidStorage;

/**
 * Flexible storage interface for tanks, containers.
 * Interface supports both discrete items and bulk resources (such as fluids.)
 */
@API(status = Status.EXPERIMENTAL)
public interface Storage extends TransactionParticipant {
	default ArticleFunction getConsumer() {
		return ArticleFunction.FULL;
	}

	default boolean hasConsumer() {
		return getConsumer().canApply();
	}

	default ArticleFunction getSupplier() {
		return ArticleFunction.EMPTY;
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

	default boolean isView() {
		return false;
	}

	default Storage viewOwner() {
		return this;
	}

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

	FractionView amount();

	default FractionView amountOf(Article item)  {
		return getSupplier().apply(item, Fraction.MAX_VALUE, true);
	}

	long capacity();

	FractionView volume();

	double usage();

	void clear();

	void startListening(StorageListener listener, boolean sendNotifications);

	void stopListening(StorageListener listener, boolean sendNotifications);

	CompoundTag writeTag();

	void readTag(CompoundTag tag);

	Predicate <? super StoredArticleView> NOT_EMPTY = a -> !a.isEmpty();

	Storage EMPTY = EmptyStorage.INSTANCE;
	Storage VOID = VoidStorage.INSTANCE;
	Storage CREATIVE = CreativeStorage.INSTANCE;

	DeviceComponentType<Storage> STORAGE_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new Identifier(Fluidity.MOD_ID, "storage"), EMPTY);

	/**
	 * Multiblock storage devices may elect to return the compound storage instance as the main storage service.
	 * This method offers an unambiguous way to reference the internal storage of the device.
	 *
	 * <p>Also used by and necessary for aggregate storage implementations for the same reason.
	 *
	 * @return Internal {@link Storage} of this device, or the regular storage if not a multiblock.
	 */
	DeviceComponentType<Storage> INTERNAL_STORAGE_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new Identifier(Fluidity.MOD_ID, "internal_storage"), EMPTY);
}
