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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.FixedArticleFunction;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.helper.FixedArticleManager;

@Experimental
public class DividedDiscreteStore extends AbstractDiscreteStore<DividedDiscreteStore> implements FixedDiscreteStore {
	protected final int divisionCount;
	protected final long capacityPerDivision;

	public DividedDiscreteStore(int divisionCount, long capacityPerDivision) {
		super(divisionCount, divisionCount * capacityPerDivision, new FixedArticleManager<>(divisionCount, StoredDiscreteArticle::new));
		this.divisionCount = divisionCount;
		this.capacityPerDivision = capacityPerDivision;
	}

	@Override
	public FixedArticleFunction getConsumer() {
		return consumer;
	}

	@Override
	public boolean hasConsumer() {
		return true;
	}

	@Override
	public FixedArticleFunction getSupplier() {
		return supplier;
	}

	@Override
	public boolean hasSupplier() {
		return true;
	}

	@Override
	protected FixedDiscreteArticleFunction createConsumer() {
		return new DividedDiscreteStore.Consumer();
	}

	protected class Consumer extends AbstractDiscreteStore<DividedDiscreteStore>.Consumer {
		@Override
		public long apply(Article item, long count, boolean simulate) {
			if (notifier.articleCount() >= divisionCount) {
				final StoredDiscreteArticle a = articles.get(item);

				if (a == null || a.isEmpty()) {
					return 0;
				}

				count = limit(a, count);
			}

			return super.apply(item, count, simulate);
		}

		@Override
		public long apply(int handle, Article item, long count, boolean simulate) {
			Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
			Preconditions.checkNotNull(item, "Request to accept null item");

			if (item.isNothing() || count == 0 || !filter.test(item)) {
				return 0;
			}

			final StoredDiscreteArticle a = articles.get(handle);

			if (a.isEmpty() || a.article().equals(item)) {
				final long result = limit(a, count);

				if (result > 0 && !simulate) {
					rollbackHandler.prepareIfNeeded();

					if (a.isEmpty()) {
						a.setArticle(item);
						a.setCount(count);
					} else {
						a.addToCount(result);
					}

					notifier.notifyAccept(a, result);
					dirtyNotifier.run();
				}

				return result;
			} else {
				return 0;
			}
		}
	}

	@Override
	protected FixedDiscreteArticleFunction createSupplier() {
		return new DividedDiscreteStore.Supplier();
	}

	protected class Supplier extends AbstractDiscreteStore<DividedDiscreteStore>.Supplier {
		@Override
		public long apply(int handle, Article item, long count, boolean simulate) {
			Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
			Preconditions.checkNotNull(item, "Request to supply null item");

			if (item.isNothing() || isEmpty()) {
				return 0;
			}

			final StoredDiscreteArticle a = articles.get(handle);

			if (a == null || a.isEmpty() || !a.article().equals(item)) {
				return 0;
			}

			final long result = Math.min(count, a.count());

			if (result > 0 && !simulate) {
				rollbackHandler.prepareIfNeeded();
				notifier.notifySupply(a, result);
				a.addToCount(-result);
				dirtyNotifier.run();
			}

			return result;
		}
	}

	protected long limit(StoredDiscreteArticle a, long requested) {
		final long cap = capacityPerDivision - a.count();

		if (cap <= 0) {
			return 0;
		} else {
			return requested > cap ? cap : requested;
		}
	}
}
