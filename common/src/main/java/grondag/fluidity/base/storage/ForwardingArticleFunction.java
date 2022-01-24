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

package grondag.fluidity.base.storage;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.ArticleFunction;

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
