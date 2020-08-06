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
package grondag.fluidity.base.storage.bulk;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.storage.Store;

@API(status = Status.EXPERIMENTAL)
public interface BulkStore extends Store {
	@Override
	default long count() {
		return amount().whole();
	}

	@Override
	default long capacity() {
		return volume().whole();
	}

	@Override
	default double usage() {
		final Fraction cap = volume();
		return cap.isZero() ? 0 : amount().toDouble() / volume().toDouble();
	}

	public interface BulkArticleFunction extends ArticleFunction {
		@Override
		default long apply(Article item, long count, boolean simulate) {
			return apply(item, count, 1, simulate);
		}
	}

	@Override
	default boolean canSupply(Article article) {
		return !getSupplier().apply(article, Fraction.ONE, true).isZero();
	}

	@Override
	default boolean canConsume(Article article) {
		return !getConsumer().apply(article, Fraction.ONE, true).isZero();
	}
}
