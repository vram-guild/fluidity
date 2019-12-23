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
import grondag.fluidity.api.storage.DiscreteStorage;
import grondag.fluidity.base.article.DiscreteArticle;

@API(status = Status.EXPERIMENTAL)
public class FlexibleItemStorage extends AbstractItemStorage implements DiscreteStorage {
	public FlexibleItemStorage(int startingHandleCount, long capacity) {
		super(startingHandleCount, capacity);
	}

	public FlexibleItemStorage(int capacity) {
		this(32, capacity);
	}

	@Override
	public long accept(DiscreteItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item.isEmpty() || count == 0 || !filter.test(item)) {
			return 0;
		}

		final long result = Math.min(count, notifier.capacity() - notifier.count());

		if(result > 0 && !simulate) {
			final DiscreteArticle article = articles.findOrCreateArticle(item);
			article.count += result;
			notifier.notifyAccept(article, result);
			dirtyNotifier.run();
		}

		return result;
	}

	@Override
	public long supply(DiscreteItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to supply null item");

		if (item.isEmpty() || articles.isEmpty()) {
			return 0;
		}

		final DiscreteArticle article = articles.get(item);

		if(article == null || article.isEmpty()) {
			return 0;
		}

		final long result = Math.min(count, article.count);

		if(result > 0 && !simulate) {
			notifier.notifySupply(article, result);
			article.count -= result;
			dirtyNotifier.run();
		}

		return result;
	}

	@Override
	public void clear() {
		if(isEmpty()) {
			return;
		}

		final int limit = articles.handleCount();

		for (int i = 0; i < limit; i++) {
			final DiscreteArticle a = articles.get(i);

			if(!a.isEmpty()) {
				notifier.notifySupply(a, a.count);
				a.item = DiscreteItem.EMPTY;
				a.count = 0;
			}
		}

		articles.clear();
		dirtyNotifier.run();
	}

	@Override
	protected Object createRollbackState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void applyRollbackState(Object state) {
		// TODO Auto-generated method stub

	}
}
