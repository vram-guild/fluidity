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

import net.minecraft.world.item.ItemStack;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.impl.storage.AlwaysReturnRequestedImpl;
import grondag.fluidity.impl.storage.AlwaysReturnZeroImpl;

/**
 * Extension of {@code ArticleFunction} for stores with fixed handles.
 *
 * @see FixedStore
 * @see <a href="https://github.com/grondag/fluidity#store-and-its-variants">https://github.com/grondag/fluidity#store-and-its-variants</a>
 */
@Experimental
public interface FixedArticleFunction extends ArticleFunction {
	/**
	 * Adds or removes items to/from this store, depending on context. May return less than requested.
	 * Will return zero article located by given handle is already occupied and different.
	 *
	 * @param handle article must associated with this handle or operation will return zero
	 * @param article Item to added/removed
	 * @param tag NBT if item has it, null otherwise.
	 * @param count How many to add or remove. Must be >= 0;
	 * @param simulate If true, will forecast result without making changes.
	 * @return Count added or removed, or that would be added or removed if {@code simulate} = true.
	 */
	long apply(int handle, Article article, long count, boolean simulate);

	default long apply(int handle, ItemStack stack, long count, boolean simulate) {
		return apply(handle, Article.of(stack), count, simulate);
	}

	default long apply(int handle, ItemStack stack, boolean simulate) {
		return apply(handle, Article.of(stack), stack.getCount(), simulate);
	}

	Fraction apply(int handle, Article article, Fraction volume, boolean simulate);

	long apply(int handle, Article article, long numerator, long divisor, boolean simulate);

	FixedArticleFunction ALWAYS_RETURN_REQUESTED = AlwaysReturnRequestedImpl.INSTANCE;
	FixedArticleFunction ALWAYS_RETURN_ZERO = AlwaysReturnZeroImpl.INSTANCE;
}
