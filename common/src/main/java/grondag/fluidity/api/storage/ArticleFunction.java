/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.api.storage;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.device.DeviceComponentRegistry;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.transact.TransactionParticipant;
import grondag.fluidity.impl.Fluidity;
import grondag.fluidity.impl.storage.AlwaysReturnRequestedImpl;
import grondag.fluidity.impl.storage.AlwaysReturnZeroImpl;

/**
 * Flexible storage interface for tanks, containers.
 * Interface supports both discrete items and bulk resources (such as fluids.)
 */
@Experimental
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
	 * <p>Storage containers or pipes that only deal in certain units (for example,
	 * the vanilla cauldron) should return zero or a lesser amount for requests
	 * that would result in an invalid state.
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
	 * only get whole bottles as the result.
	 *
	 * <p>Storage containers or pipes that only deal in certain units (for example,
	 * the vanilla cauldron) should return zero or a lesser amount for requests
	 * that would result in an invalid state.
	 *
	 * @param article The stuff to add or remove
	 * @param numerator Fractional units to add or remove. Can be zero.
	 * @param divisor Denominator of units to add or remove. Must be >= 1.
	 * @param simulate If true, forecasts the result without making any changes.
	 * @return How much was added or removed, in units of given denominator.
	 */
	long apply(Article article, long numerator, long divisor, boolean simulate);

	/**
	 * Indicates a preference for type of article suggested.  System may respond
	 * with what is available if not of given type. Leave null for no preference.
	 */
	Article suggestArticle(@Nullable ArticleType<?> type);

	ArticleFunction ALWAYS_RETURN_REQUESTED = AlwaysReturnRequestedImpl.INSTANCE;
	ArticleFunction ALWAYS_RETURN_ZERO = AlwaysReturnZeroImpl.INSTANCE;

	DeviceComponentType<ArticleFunction> SUPPLIER_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new ResourceLocation(Fluidity.MOD_ID, "article_supplier"), ALWAYS_RETURN_ZERO);
	DeviceComponentType<ArticleFunction> CONSUMER_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new ResourceLocation(Fluidity.MOD_ID, "article_consumer"), ALWAYS_RETURN_ZERO);
}
