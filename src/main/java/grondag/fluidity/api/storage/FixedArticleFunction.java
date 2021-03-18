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

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.item.ItemStack;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.impl.storage.AlwaysReturnRequestedImpl;
import grondag.fluidity.impl.storage.AlwaysReturnZeroImpl;

/**
 * Extension of {@code ArticleFunction} for stores with fixed handles.
 *
 * @see {@link FixedStore}
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
