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
package grondag.fluidity.base.storage.discrete;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.FixedArticleFunction;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.helper.FixedArticleManager;

@API(status = Status.EXPERIMENTAL)
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
			if(notifier.articleCount() >= divisionCount) {
				final StoredDiscreteArticle a = articles.get(item);

				if(a == null || a.isEmpty()) {
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

			if(a.isEmpty() || a.article().equals(item)) {
				final long result = limit(a, count);

				if(result > 0 && !simulate) {
					rollbackHandler.prepareIfNeeded();

					if(a.isEmpty()) {
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

			if(a == null || a.isEmpty() || !a.article().equals(item)) {
				return 0;
			}

			final long result = Math.min(count, a.count());

			if(result > 0 && !simulate) {
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
