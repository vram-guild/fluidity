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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.CommonItem;
import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.api.storage.DiscreteStorage;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.base.article.DiscreteArticle;
import grondag.fluidity.base.storage.AbstractLazyRollbackStorage;
import grondag.fluidity.base.storage.component.AbstractArticleManager;
import grondag.fluidity.base.storage.component.TrackingItemNotifier;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractDiscreteStorage extends AbstractLazyRollbackStorage<DiscreteArticleView,  DiscreteStorageListener> implements DiscreteStorage {
	protected final AbstractArticleManager<StorageItem, DiscreteArticle> articles;
	protected final TrackingItemNotifier notifier;

	AbstractDiscreteStorage(int startingHandleCount, long capacity, AbstractArticleManager<StorageItem, DiscreteArticle> articles) {
		this.articles = articles;
		notifier = new TrackingItemNotifier(capacity, this);
	}

	@Override
	public CompoundTag writeTag() {
		final CompoundTag result = new CompoundTag();

		if(!isEmpty()) {
			final ListTag list = new ListTag();
			final int limit = articles.handleCount();

			for (int i = 0; i < limit; i++) {
				final DiscreteArticle a = articles.get(i);

				if(!a.isEmpty()) {
					list.add(a.toTag());
				}
			}

			result.put(TAG_ITEMS, list);
		}

		return result;
	}

	@Override
	public void readTag(CompoundTag tag) {
		clear();

		if(tag.contains(TAG_ITEMS)) {
			final ListTag list = tag.getList(TAG_ITEMS, 10);
			final int limit = list.size();
			final DiscreteArticle lookup = new DiscreteArticle();

			for(int i = 0; i < limit; i++) {
				lookup.readTag(list.getCompound(i));

				if(!lookup.isEmpty()) {
					accept(lookup.item(), lookup.count, false);
				}
			}
		}
	}

	@Override
	public int handleCount() {
		return articles.handleCount();
	}

	@Override
	public DiscreteArticleView view(int handle) {
		return articles.get(handle);
	}

	@Override
	public boolean isEmpty() {
		return notifier.count() == 0;
	}

	@Override
	public long count() {
		return notifier.count();
	}

	@Override
	public long capacity() {
		return notifier.capacity();
	}

	@Override
	protected final void sendFirstListenerUpdate(DiscreteStorageListener listener) {
		notifier.sendFirstListenerUpdate(listener);
	}

	@Override
	protected void onListenersEmpty() {
		articles.compact();
	}

	@Override
	public long accept(StorageItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item.isNothing() || count == 0 || !filter.test(item)) {
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
	public long supply(StorageItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to supply null item");

		if (item.isNothing() || isEmpty()) {
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
				a.item = CommonItem.NOTHING;
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
