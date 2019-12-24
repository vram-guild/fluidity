/*******************************************************************************
 * Copyright 2019 grondag
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

import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.FixedDiscreteStorage;
import grondag.fluidity.base.article.DiscreteArticle;
import grondag.fluidity.base.storage.component.FixedArticleManager;

@API(status = Status.EXPERIMENTAL)
public class DividedItemStorage extends AbstractItemStorage implements FixedDiscreteStorage  {
	protected final int divisionCount;
	protected final long capacityPerDivision;

	public DividedItemStorage(int divisionCount, long capacityPerDivision) {
		super(divisionCount, divisionCount * capacityPerDivision, new FixedArticleManager<>(divisionCount, DiscreteArticle::new));
		this.divisionCount = divisionCount;
		this.capacityPerDivision = capacityPerDivision;
	}

	@Override
	public long accept(DiscreteItem item, long count, boolean simulate) {
		if(notifier.articleCount() >= divisionCount) {
			final DiscreteArticle a = articles.get(item);

			if(a == null || a.isEmpty()) {
				return 0;
			}

			count = limit(a, count);
		}

		return super.accept(item, count, simulate);
	}

	protected long limit(DiscreteArticle a, long requested) {
		final long cap = capacityPerDivision - a.count;

		if (cap <= 0) {
			return 0;
		} else {
			return requested > cap ? cap : requested;
		}
	}

	@Override
	public long accept(int handle, DiscreteItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item.isEmpty() || count == 0 || !filter.test(item)) {
			return 0;
		}

		final DiscreteArticle a = articles.get(handle);

		if(a.isEmpty() || a.item.equals(item)) {
			final long result = limit(a, count);

			if(result > 0 && !simulate) {
				if(a.isEmpty()) {
					a.item = item;
					a.count = count;
				} else {
					a.count += result;
				}

				notifier.notifyAccept(a, result);
				dirtyNotifier.run();
			}

			return result;
		} else {
			return 0;
		}
	}

	@Override
	public long supply(int handle, DiscreteItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to supply null item");

		if (item.isEmpty() || isEmpty()) {
			return 0;
		}

		final DiscreteArticle a = articles.get(handle);

		if(a == null || a.isEmpty() || !a.item.equals(item)) {
			return 0;
		}

		final long result = Math.min(count, a.count);

		if(result > 0 && !simulate) {
			notifier.notifySupply(a, result);
			a.count -= result;
			dirtyNotifier.run();
		}

		return result;
	}
}
