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
import net.minecraft.util.Identifier;

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.device.DeviceComponentRegistry;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.transact.TransactionParticipant;
import grondag.fluidity.impl.storage.AlwaysReturnRequestedImpl;
import grondag.fluidity.impl.storage.AlwaysReturnZeroImpl;

/**
 * Flexible storage interface for tanks, containers.
 * Interface supports both discrete items and bulk resources (such as fluids.)
 */
@API(status = Status.EXPERIMENTAL)
public interface ArticleFunction extends TransactionParticipant {
	/**
	 * Adds or removes items to/from this store, depending on context. May return less than requested.
	 *
	 * @param article Item to added/removed
	 * @param tag NBT if item has it, null otherwise.
	 * @param count How many to add or remove. Must be >= 0;
	 * @param simulate If true, will forecast result without making changes.
	 * @return Count added or removed, or that would be added or removed if {@code simulate} = true.
	 */
	long apply(Article article, long count, boolean simulate);

	/**
	 * Distinct from {@link #isFull()} - can be false even when store is not full.
	 * Meant for modeling machine output buffers that should never take input, but
	 * can have other, similar uses. Insert logic should ignore any store that returns false.
	 *
	 * @return {@code true} if this store may ever accept articles.
	 */
	default boolean canApply() {
		return true;
	}

	default boolean canApply(Article article) {
		if (article.type().isBulk()) {
			return !apply(article, Fraction.ONE, true).isZero();
		} else {
			return apply(article, 1, true) == 1;
		}
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
	 * Adds or removes up to  {@code volume} units of the bulk item to this store and
	 * returns the number of units added or removed.  The denominator of the result *may*
	 * be different from the denominator of the input fraction.
	 *
	 * Storage containers or pipes that only deal in certain units (for example,
	 * the vanilla cauldron) should return zero or a lesser amount for requests
	 * that would result in an invalid state.<p>
	 *
	 * @param article  The stuff to add or remove
	 * @param volume How much to add or remove
	 * @param simulate If true, forecasts the result without making any changes.
	 * @return How much stuff was added or removed
	 */
	Fraction apply(Article article, Fraction volume, boolean simulate);

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
	 * @param article The stuff to add or remove
	 * @param numerator Fractional units to add or remove. Can be zero.
	 * @param divisor Denominator of units to add or remove. Must be >= 1.
	 * @param simulate If true, forecasts the result without making any changes.
	 * @return How much was added or removed, in units of given denominator.
	 */
	long apply(Article article, long numerator, long divisor, boolean simulate);

	Article suggestArticle();

	ArticleFunction ALWAYS_RETURN_REQUESTED = AlwaysReturnRequestedImpl.INSTANCE;
	ArticleFunction ALWAYS_RETURN_ZERO = AlwaysReturnZeroImpl.INSTANCE;

	DeviceComponentType<ArticleFunction> SUPPLIER_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new Identifier(Fluidity.MOD_ID, "article_supplier"), ALWAYS_RETURN_ZERO);
	DeviceComponentType<ArticleFunction> CONSUMER_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new Identifier(Fluidity.MOD_ID, "article_consumer"), ALWAYS_RETURN_ZERO);
}
