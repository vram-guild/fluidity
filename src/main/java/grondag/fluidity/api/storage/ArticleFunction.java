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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.transact.TransactionParticipant;
import grondag.fluidity.impl.storage.CreativeSupplier;
import grondag.fluidity.impl.storage.EmptySupplier;
import grondag.fluidity.impl.storage.FullConsumer;
import grondag.fluidity.impl.storage.VoidConsumer;

/**
 * Flexible storage interface for tanks, containers.
 * Interface supports both discrete items and bulk resources (such as fluids.)
 */
@API(status = Status.EXPERIMENTAL)
public interface ArticleFunction extends TransactionParticipant {
	/**
	 * Adds items to this storage. May return less than requested.
	 *
	 * @param item Item to add
	 * @param tag NBT if item has it, null otherwise.
	 * @param count How many to add. Must be >= 0;
	 * @param simulate If true, will forecast result without making changes.
	 * @return Count added, or that would be added if {@code simulate} = true.
	 */
	long apply(Article item, long count, boolean simulate);

	/**
	 * Distinct from {@link #isFull()} - can be false even when storage is not full.
	 * Meant for modeling machine output buffers that should never take input, but
	 * can have other, similar uses. Insert logic should ignore any storage that returns false.
	 *
	 * @return {@code true} if this storage may ever accept articles.
	 */
	default boolean canApply() {
		return true;
	}


	default long apply(Item item, @Nullable CompoundTag tag, long count, boolean simulate) {
		return apply(Article.of(item, tag), count, simulate);
	}

	default long apply(Item item, long count, boolean simulate) {
		return apply(Article.of(item), count, simulate);
	}

	default long apply(ItemStack stack, long count, boolean simulate) {
		return apply(Article.of(stack), count, simulate);
	}

	default long apply(ItemStack stack, boolean simulate) {
		return apply(Article.of(stack), stack.getCount(), simulate);
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
	FractionView apply(Article item, FractionView volume, boolean simulate);

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
	long apply(Article item, long numerator, long divisor, boolean simulate);

	ArticleFunction VOID = VoidConsumer.INSTANCE;
	ArticleFunction FULL = FullConsumer.INSTANCE;
	ArticleFunction EMPTY = EmptySupplier.INSTANCE;
	ArticleFunction CREATIVE = CreativeSupplier.INSTANCE;
}
