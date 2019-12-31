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
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import org.apache.commons.lang3.ObjectUtils;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.storage.ArticleConsumer;
import grondag.fluidity.api.storage.ArticleSupplier;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.AbstractLazyRollbackStorage;
import grondag.fluidity.base.storage.component.AbstractArticleManager;
import grondag.fluidity.base.storage.component.DiscreteTrackingJournal;
import grondag.fluidity.base.storage.component.DiscreteTrackingNotifier;
import grondag.fluidity.base.storage.discrete.DiscreteStorage.DiscreteArticleConsumer;
import grondag.fluidity.base.storage.discrete.DiscreteStorage.DiscreteArticleSupplier;
import grondag.fluidity.impl.ArticleImpl;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractDiscreteStorage<T extends AbstractDiscreteStorage<T>> extends AbstractLazyRollbackStorage<StoredDiscreteArticle, T> implements DiscreteStorage, DiscreteArticleConsumer, DiscreteArticleSupplier {
	protected final AbstractArticleManager<StoredDiscreteArticle> articles;
	protected final DiscreteTrackingNotifier notifier;

	AbstractDiscreteStorage(int startingHandleCount, long capacity, AbstractArticleManager<StoredDiscreteArticle> articles) {
		this.articles = articles;
		notifier = new DiscreteTrackingNotifier(capacity, this);
	}

	@Override
	public ArticleConsumer getConsumer() {
		return this;
	}

	@Override
	public boolean hasConsumer() {
		return true;
	}

	@Override
	public ArticleSupplier getSupplier() {
		return this;
	}

	@Override
	public boolean hasSupplier() {
		return true;
	}

	@Override
	public CompoundTag writeTag() {
		final CompoundTag result = new CompoundTag();

		if(!isEmpty()) {
			final ListTag list = new ListTag();
			final int limit = articles.handleCount();

			for (int i = 0; i < limit; i++) {
				final StoredDiscreteArticle a = articles.get(i);

				if(!a.isEmpty()) {
					list.add(a.toTag());
				}
			}

			result.put(AbstractDiscreteStorage.TAG_ITEMS, list);
		}

		return result;
	}

	@Override
	public void readTag(CompoundTag tag) {
		clear();

		if(tag.contains(AbstractDiscreteStorage.TAG_ITEMS)) {
			final ListTag list = tag.getList(AbstractDiscreteStorage.TAG_ITEMS, 10);
			final int limit = list.size();
			final StoredDiscreteArticle lookup = new StoredDiscreteArticle();

			for(int i = 0; i < limit; i++) {
				lookup.readTag(list.getCompound(i));

				if(!lookup.isEmpty()) {
					accept(lookup.article(), lookup.count(), false);
				}
			}
		}
	}

	@Override
	public int handleCount() {
		return articles.handleCount();
	}

	@Override
	public StoredArticleView view(int handle) {
		return ObjectUtils.defaultIfNull(articles.get(handle), StoredArticleView.EMPTY);
	}

	@Override
	public boolean isEmpty() {
		return notifier.count() == 0;
	}

	@Override
	public boolean isFull() {
		return notifier.count() >= notifier.capacity();
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
	protected final void sendFirstListenerUpdate(StorageListener listener) {
		notifier.sendFirstListenerUpdate(listener);
	}

	@Override
	protected final void sendLastListenerUpdate(StorageListener listener) {
		notifier.sendLastListenerUpdate(listener);
	}

	@Override
	protected void onListenersEmpty() {
		articles.compact();
	}

	@Override
	public long accept(Article item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item.isNothing() || count == 0 || !filter.test(item)) {
			return 0;
		}

		final long result = Math.min(count, notifier.capacity() - notifier.count());

		if(result > 0 && !simulate) {
			final StoredDiscreteArticle article = articles.findOrCreateArticle(item);
			article.addToCount(result);
			notifier.notifyAccept(article, result);
			dirtyNotifier.run();
		}

		return result;
	}

	@Override
	public long supply(Article item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to supply null item");

		if (item.isNothing() || isEmpty()) {
			return 0;
		}

		final StoredDiscreteArticle article = articles.get(item);

		if(article == null || article.isEmpty()) {
			return 0;
		}

		final long result = Math.min(count, article.count());

		if(result > 0 && !simulate) {
			notifier.notifySupply(article, result);
			article.addToCount(-result);
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
			final StoredDiscreteArticle a = articles.get(i);

			if(!a.isEmpty()) {
				notifier.notifySupply(a, a.count());
				a.setArticle(ArticleImpl.NOTHING);
				a.zero();
			}
		}

		articles.clear();
		dirtyNotifier.run();
	}

	@Override
	protected Object createRollbackState() {
		return notifier.beginNewJournalAndReturnPrior();
	}

	@Override
	protected void applyRollbackState(Object state, boolean isCommitted) {
		final DiscreteTrackingJournal journal = notifier.journal();

		if(!isCommitted && journal != null) {
			if(journal.capacityDelta < 0) {
				notifier.addToCapacity(-journal.capacityDelta);
			}

			for(final Entry<Article> e : journal.changes.object2LongEntrySet() ) {
				final long q = e.getLongValue();

				if(q > 0) {
					supply(e.getKey(), q, false);
				}
			}

			for(final Entry<Article> e : journal.changes.object2LongEntrySet() ) {
				final long q = e.getLongValue();

				if(q < 0) {
					accept(e.getKey(), -q, false);
				}
			}

			if(journal.capacityDelta > 0) {
				notifier.addToCapacity(-journal.capacityDelta);
			}
		}

		notifier.restoreJournal((DiscreteTrackingJournal) state);

	}

	public static final String TAG_ITEMS = "items";
}
