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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.device.Device;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.transact.Transactor;
import grondag.fluidity.impl.EmptyStorage;
import grondag.fluidity.impl.VoidStorage;

/**
 * Flexible storage interface for tanks, containers.
 * Interface supports both discrete items and bulk resources (such as fluids.)
 */
@API(status = Status.EXPERIMENTAL)
public interface Storage extends Transactor {
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

	default @Nullable Device device() {
		return null;
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
	 * Adds items to this storage. May return less than requested.
	 *
	 * @param item Item to add
	 * @param tag NBT if item has it, null otherwise.
	 * @param count How many to add. Must be >= 0;
	 * @param simulate If true, will forecast result without making changes.
	 * @return Count added, or that would be added if {@code simulate} = true.
	 */
	long accept(Article item, long count, boolean simulate);

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
	 * Distinct from {@link #isFull()} - can be false even when storage is not full.
	 * Meant for modeling machine output buffers that should never take input, but
	 * can have other, similar uses. Insert logic should ignore any storage that returns false.
	 *
	 * @return {@code true} if this storage may ever accept articles.
	 */
	default boolean canAccept() {
		return true;
	}

	/**
	 * Removes items from this storage. May return less than requested.
	 *
	 * @param item Item to remove
	 * @param tag NBT if item has it, null otherwise.
	 * @param count How many to remove. Must be >= 0;
	 * @param simulate If true, will forecast result without making changes.
	 * @return Count removed, or that would be removed if {@code simulate} = true.
	 */
	long supply(Article item, long count, boolean simulate);

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

	/**
	 * Distinct from {@link #isEmpty()} - can be false even when storage has content.
	 * Meant for modeling machine input buffers that should never give output, but
	 * can have other, similar uses. Extract logic should ignore any storage that returns false.
	 *
	 * @return {@code true} if this storage may ever accept articles.
	 */
	default boolean canSupply() {
		return true;
	}

	long count();

	default long countOf(Article item)  {
		return supply(item, Long.MAX_VALUE, true);
	}

	FractionView amount();

	default FractionView amountOf(Article item)  {
		return supply(item, Fraction.MAX_VALUE, true);
	}

	long capacity();

	FractionView volume();

	void clear();

	default long accept(Item item, @Nullable CompoundTag tag, long count, boolean simulate) {
		return accept(Article.of(item, tag), count, simulate);
	}

	default long accept(Item item, long count, boolean simulate) {
		return accept(Article.of(item), count, simulate);
	}

	default long accept(ItemStack stack, long count, boolean simulate) {
		return accept(Article.of(stack), count, simulate);
	}

	default long accept(ItemStack stack, boolean simulate) {
		return accept(Article.of(stack), stack.getCount(), simulate);
	}

	default long supply(Item item, @Nullable CompoundTag tag, long count, boolean simulate) {
		return supply(Article.of(item, tag), count, simulate);
	}

	default long supply(Item item, long count, boolean simulate) {
		return supply(Article.of(item), count, simulate);
	}

	default long supply(ItemStack stack, long count, boolean simulate) {
		return supply(Article.of(stack), count, simulate);
	}

	default long supply(ItemStack stack, boolean simulate) {
		return supply(Article.of(stack), stack.getCount(), simulate);
	}

	/**
	 * Adds up to  {@code volume} units of the bulk item to this storage and
	 * returns the number of units added.  The denominator of the result *may*
	 * be different from the denominator of the input fraction.
	 *
	 * Storage containers or pipes that only deal in certain units (for example,
	 * the vanilla cauldron) should return zero or a lesser amount for requests
	 * that would result in an invalid state.<p>
	 *
	 * @param item  The stuff to add
	 * @param volume How much to add
	 * @param simulate If true, forecasts the result without making any changes.
	 * @return How much stuff was added
	 */
	FractionView accept(Article item, FractionView volume, boolean simulate);

	/**
	 * Removes up to {@code volume} units of the bulk item to this storage and
	 * returns the number of units removed.  The denominator of the result *may*
	 * be different from the denominator of the input fraction.
	 *
	 * Storage containers or pipes that only deal in certain units (for example,
	 * the vanilla cauldron) should return zero or a lesser amount for requests
	 * that would result in an invalid state.<p>
	 *
	 * @param item  The stuff to remove
	 * @param volume How much to remove
	 * @param simulate If true, forecasts the result without making any changes.
	 * @return How much stuff was removed
	 */
	FractionView supply(Article item, FractionView volume, boolean simulate);

	/**
	 * As with {@link #accept(BulkItem, FractionView, boolean)} BUT the result
	 * will always be an even multiple of the input denominator.  So, for example,
	 * if you call with {@link FractionView#BOTTLE} as the denominator, you will
	 * only get whole bottles as the result.<p>
	 *
	 * Storage containers or pipes that only deal in certain units (for example,
	 * the vanilla cauldron) should return zero or a lesser amount for requests
	 * that would result in an invalid state.<p>
	 *
	 * @param item The stuff to add
	 * @param numerator Fractional units to add. Can be zero.
	 * @param divisor Denominator of units to add. Must be >= 1.
	 * @param simulate If true, forecasts the result without making any changes.
	 * @return How much was added, in units of given denominator.
	 */
	long accept(Article item, long numerator, long divisor, boolean simulate);

	/**
	 * As with {@link #supply(BulkItem, FractionView, boolean)} BUT the result
	 * will always be an even multiple of the input denominator.  So, for example,
	 * if you call with {@link FractionView#BOTTLE} as the denominator, you will
	 * only get whole bottles as the result.<p>
	 *
	 * Storage containers or pipes that only deal in certain units (for example,
	 * the vanilla cauldron) should return zero or a lesser amount for requests
	 * that would result in an invalid state.<p>
	 *
	 * @param item The stuff to remove
	 * @param numerator Fractional units to remove. Can be zero.
	 * @param divisor Denominator of units to add. Must be >= 1.
	 * @param simulate If true, forecasts the result without making any changes.
	 * @return How much was removed, in units of given denominator.
	 */
	long supply(Article item, long numerator, long divisor, boolean simulate);

	void startListening(StorageListener listener, boolean sendNotifications);

	void stopListening(StorageListener listener, boolean sendNotifications);

	CompoundTag writeTag();

	void readTag(CompoundTag tag);

	Predicate <? super StoredArticleView> NOT_EMPTY = a -> !a.isEmpty();

	Storage EMPTY = EmptyStorage.INSTANCE;
	Storage VOID = VoidStorage.INSTANCE;
}
