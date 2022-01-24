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

package grondag.fluidity.impl.storage;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.FixedArticleFunction;

@Internal
public final class AlwaysReturnZeroImpl implements FixedArticleFunction {
	private AlwaysReturnZeroImpl() { }

	public static FixedArticleFunction INSTANCE = new AlwaysReturnZeroImpl();

	@Override
	public long apply(Article item, long count, boolean simulate) {
		return 0;
	}

	@Override
	public Fraction apply(Article item, Fraction volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	public long apply(Article item, long numerator, long divisor, boolean simulate) {
		return 0;
	}

	@Override
	public TransactionDelegate getTransactionDelegate() {
		return TransactionDelegate.IGNORE;
	}

	@Override
	public long apply(int handle, Article item, long count, boolean simulate) {
		return 0;
	}

	@Override
	public Fraction apply(int handle, Article item, Fraction volume, boolean simulate) {
		return Fraction.ZERO;
	}

	@Override
	public long apply(int handle, Article item, long numerator, long divisor, boolean simulate) {
		return 0;
	}

	@Override
	public boolean canApply() {
		return false;
	}

	@Override
	public Article suggestArticle(ArticleType<?> type) {
		return Article.NOTHING;
	}
}
