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

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.ObjectUtils;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.storage.ArticleConsumer;
import grondag.fluidity.api.storage.ArticleSupplier;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.AggregateDiscreteStoredArticle;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.AbstractAggregateStorage;
import grondag.fluidity.base.storage.component.DiscreteTrackingNotifier;
import grondag.fluidity.base.storage.discrete.DiscreteStorage.DiscreteArticleConsumer;
import grondag.fluidity.base.storage.discrete.DiscreteStorage.DiscreteArticleSupplier;

@API(status = Status.EXPERIMENTAL)
public class AggregateDiscreteStorage extends AbstractAggregateStorage<AggregateDiscreteStoredArticle, AggregateDiscreteStorage> implements DiscreteStorage, DiscreteArticleConsumer, DiscreteArticleSupplier, DiscreteStorageListener {
	protected final DiscreteTrackingNotifier notifier;

	public AggregateDiscreteStorage(int startingSlotCount) {
		super(startingSlotCount);
		notifier = new DiscreteTrackingNotifier(0, this);
	}
	public AggregateDiscreteStorage() {
		this(32);
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

	@Nullable
	protected StoredDiscreteArticle getArticle(Item item, CompoundTag tag) {
		return articles.get(Article.of(item, tag));
	}

	protected final ObjectArrayList<Storage> searchList = new ObjectArrayList<>();

	@Override
	public long accept(Article item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item.isNothing() || stores.isEmpty()) {
			return 0;
		}

		// consolidate notifications
		itMe  = true;
		long result = 0;

		final AggregateDiscreteStoredArticle article = articles.findOrCreateArticle(item);

		// Try stores that already have article first
		final Set<Storage> existing = article.stores();

		if(!existing.isEmpty()) {
			// save non-existing stores here in case existing have insufficient capacity
			searchList.clear();

			for (final Storage store : stores) {
				if(store.hasConsumer() && !store.isFull()) {

					if(existing.contains(store)) {
						enlister.accept(store);
						result += store.getConsumer().accept(item, count - result, simulate);

						if (result == count) {
							break;
						}
					} else {
						searchList.add(store);
					}
				}
			}

			if (result < count) {
				for (final Storage store : searchList) {
					enlister.accept(store);
					final long delta = store.getConsumer().accept(item, count - result, simulate);

					if(delta != 0) {
						result += delta;

						// add new stores to per-article tracking
						if(!simulate) {
							existing.add(store);
						}

						if (result == count) {
							break;
						}
					}
				}
			}

		} else {
			for (final Storage store : stores) {
				if(store.hasConsumer() && !store.isFull()) {
					enlister.accept(store);
					final long delta = store.getConsumer().accept(item, count - result, simulate);

					if(delta != 0) {
						result += delta;

						// add new stores to per-article tracking
						if(!simulate) {
							existing.add(store);
						}

						if (result == count) {
							break;
						}
					}
				}
			}
		}

		itMe = false;

		if(result > 0 && !simulate) {
			article.addToCount(result);
			notifier.notifyAccept(article, result);
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

		final AggregateDiscreteStoredArticle article = articles.get(item);

		if(article == null || article.isEmpty()) {
			return 0;
		}

		// consolidate notifications
		itMe  = true;
		long result = 0;

		final Set<Storage> existing = article.stores();

		for (final Storage store : existing) {
			if(store.hasSupplier()) {
				enlister.accept(store);
				final long delta = store.getSupplier().supply(item, count - result, simulate);

				if(delta != 0) {
					result += delta;

					// remove from per-article tracking if store no longer contains
					if(!simulate && store.countOf(item) == 0) {
						existing.remove(store);
					}

					if (result == count) {
						break;
					}
				}
			}
		}

		itMe = false;

		if(result > 0 && !simulate) {
			notifier.notifySupply(article, result);
			article.addToCount(-result);
		}

		return result;
	}

	@Override
	protected AggregateDiscreteStoredArticle newArticle() {
		return new AggregateDiscreteStoredArticle();
	}

	@Override
	public StoredArticleView view(int slot) {
		return ObjectUtils.defaultIfNull(articles.get(slot), StoredArticleView.EMPTY);
	}

	@Override
	protected StorageListener listener() {
		return this;
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
	public boolean isFull() {
		return notifier.count() >= notifier.capacity();
	}

	@Override
	public boolean isEmpty() {
		return notifier.count() == 0;
	}

	@Override
	public void onAccept(Storage storage, int slot, Article item, long delta, long newCount) {
		if (!itMe) {
			final AggregateDiscreteStoredArticle article = articles.findOrCreateArticle(item);
			article.addToCount(delta);
			article.stores().add(storage);
			notifier.notifyAccept(article, delta);
		}
	}

	static boolean warnIgnore = true;
	static boolean warnPartialIgnore = true;

	@Override
	public void onSupply(Storage storage, int slot, Article item, long delta, long newCount) {
		if (!itMe) {
			final AggregateDiscreteStoredArticle article = articles.get(item);

			if(article == null) {
				if(warnIgnore) {
					Fluidity.LOG.warn("AggregateStorage ignored notification of supply for non-tracked article.");
					Fluidity.LOG.warn("This probably indicates a bug in a mod using Fludity. Warnings for subsequent events are suppressed.");
					warnIgnore = false;
				}

				return;
			}

			if(delta > article.count()) {
				if(warnPartialIgnore) {
					Fluidity.LOG.warn("AggregateStorage partially ignored notification of supply for article with mimatched amount.");
					Fluidity.LOG.warn("This probably indicates a bug in a mod using Fludity. Warnings for subsequent events are suppressed.");
					warnPartialIgnore = false;
				}

				delta = article.count();
			}

			if(newCount == 0) {
				article.stores().remove(storage);
			}

			article.addToCount(-delta);
			notifier.notifySupply(article, delta);
		}
	}

	@Override
	public void onCapacityChange(Storage storage, long capacityDelta) {
		notifier.addToCapacity(capacityDelta);
	}

	/** Removes all stores, not the underlying storages */
	@Override
	public void clear() {
		if(stores.isEmpty()) {
			return;
		}

		for(final Storage store : stores.toArray(new Storage[stores.size()])) {
			removeStore(store);
		}
	}

	@Override
	protected void sendFirstListenerUpdate(StorageListener listener) {
		notifier.sendFirstListenerUpdate(listener);
	}

	@Override
	protected void sendLastListenerUpdate(StorageListener listener) {
		notifier.sendLastListenerUpdate(listener);
	}

	@Override
	public CompoundTag writeTag() {
		throw new UnsupportedOperationException("Aggregate storage view do not support saving");
	}

	@Override
	public void readTag(CompoundTag tag) {
		throw new UnsupportedOperationException("Aggregate storage view do not support saving");
	}

	@Override
	protected void onListenersEmpty() {
		articles.compact();
	}
}
