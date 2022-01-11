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
package grondag.fluidity.base.storage;

import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.ArticleFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Experimental
public class ForwardingArticleFunction<T extends ArticleFunction> implements ArticleFunction {
	protected T wrapped;

	public ForwardingArticleFunction(T wrapped) {
		this.wrapped = wrapped;
	}

	public T getWrapped() {
		return wrapped;
	}

	public void setWrapped(T wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public TransactionDelegate getTransactionDelegate() {
		return wrapped.getTransactionDelegate();
	}

	@Override
	public long apply(Article item, long count, boolean simulate) {
		return wrapped.apply(item, count, simulate);
	}

	@Override
	public Fraction apply(Article item, Fraction volume, boolean simulate) {
		return wrapped.apply(item, volume, simulate);
	}

	@Override
	public long apply(Article item, long numerator, long divisor, boolean simulate) {
		return wrapped.apply(item, numerator, divisor, simulate);
	}

	@Override
	public boolean canApply() {
		return wrapped.canApply();
	}

	@Override
	public long apply(Item item, CompoundTag tag, long count, boolean simulate) {
		return wrapped.apply(item, tag, count, simulate);
	}

	@Override
	public long apply(Item item, long count, boolean simulate) {
		return wrapped.apply(item, count, simulate);
	}

	@Override
	public long apply(ItemStack stack, long count, boolean simulate) {
		return wrapped.apply(stack, count, simulate);
	}

	@Override
	public long apply(ItemStack stack, boolean simulate) {
		return wrapped.apply(stack, simulate);
	}

	@Override
	public boolean isSelfEnlisting() {
		return wrapped.isSelfEnlisting();
	}

	@Override
	public Article suggestArticle(ArticleType<?> type) {
		return wrapped.suggestArticle(type);
	}
}
