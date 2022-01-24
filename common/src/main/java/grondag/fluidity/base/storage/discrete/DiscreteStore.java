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

package grondag.fluidity.base.storage.discrete;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.storage.Store;

@Experimental
public interface DiscreteStore extends Store {
	@Override
	default Fraction amount() {
		return Fraction.of(count());
	}

	@Override
	default Fraction volume() {
		return Fraction.of(capacity());
	}

	@Override
	default double usage() {
		final double cap = capacity();
		return cap > 0 ? count() / cap : 0;
	}

	public interface DiscreteArticleFunction extends ArticleFunction {
		@Override
		default Fraction apply(Article item, Fraction volume, boolean simulate) {
			return volume.whole() == 0 ? Fraction.ZERO : Fraction.of(apply(item, volume.whole(), simulate));
		}

		@Override
		default long apply(Article item, long numerator, long divisor, boolean simulate) {
			final long whole = numerator / divisor;
			return whole == 0 ? 0 : apply(item, whole, simulate) * divisor;
		}
	}
}
