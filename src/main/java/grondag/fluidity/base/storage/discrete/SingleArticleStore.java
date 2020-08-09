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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.AbstractLazyRollbackStore;
import grondag.fluidity.base.storage.discrete.FixedDiscreteStore.FixedDiscreteArticleFunction;
import grondag.fluidity.base.storage.discrete.helper.DiscreteNotifier;

@API(status = Status.EXPERIMENTAL)
public class SingleArticleStore extends AbstractLazyRollbackStore<StoredDiscreteArticle,  SingleArticleStore> implements DiscreteStore {
	protected final StoredDiscreteArticle view = new StoredDiscreteArticle();
	protected final DiscreteNotifier notifier = new DiscreteNotifier(this);
	protected long capacity;
	protected long quantity;
	protected Article storedArticle = Article.NOTHING;

	public SingleArticleStore(long defaultCapacity) {
		capacity = defaultCapacity;
	}

	@Override
	public ArticleFunction getConsumer() {
		return consumer;
	}

	@Override
	public boolean hasConsumer() {
		return true;
	}

	@Override
	public ArticleFunction getSupplier() {
		return supplier;
	}

	@Override
	public boolean hasSupplier() {
		return true;
	}

	protected final FixedDiscreteArticleFunction consumer = createConsumer();

	protected FixedDiscreteArticleFunction createConsumer() {
		return new Consumer();
	}

	protected class Consumer implements FixedDiscreteArticleFunction {
		@Override
		public long apply(Article article, long count, boolean simulate) {

			if(article.isNothing()) {
				return 0;
			}

			if(quantity == 0) {
				final int n = (int) Math.min(count, capacity);

				if(!simulate) {
					rollbackHandler.prepareIfNeeded();
					storedArticle = article;
					quantity = n;
					dirtyNotifier.run();

					if(!listeners.isEmpty()) {
						notifier.notifyAccept(article, 0, n, n);
					}
				}

				return n;
			} else if(article.equals(storedArticle)) {
				final int n = (int) Math.min(count, capacity - quantity);

				if(!simulate) {
					rollbackHandler.prepareIfNeeded();
					quantity += n;
					dirtyNotifier.run();

					if(!listeners.isEmpty()) {
						notifier.notifyAccept(article, 0, n, quantity);
					}
				}

				return n;
			} else {
				return 0;
			}
		}

		@Override
		public TransactionDelegate getTransactionDelegate() {
			return SingleArticleStore.this;
		}

		@Override
		public long apply(int handle, Article article, long count, boolean simulate) {
			if(handle == 0 && article != null) {
				return apply(article, count, simulate);
			} else {
				return 0;
			}
		}

		@Override
		public Article suggestArticle(ArticleType<?> type) {
			return getAnyArticle().article();
		}
	}

	protected final FixedDiscreteArticleFunction supplier = createSupplier();

	protected FixedDiscreteArticleFunction createSupplier() {
		return new Supplier();
	}

	protected class Supplier implements FixedDiscreteArticleFunction {
		@Override
		public long apply(Article article, long count, boolean simulate) {
			if(article.isNothing() || quantity == 0 || !article.equals(storedArticle)) {
				return 0;
			}

			final int n = (int) Math.min(count, quantity);

			if(!simulate) {
				rollbackHandler.prepareIfNeeded();
				quantity -= n;
				dirtyNotifier.run();

				if(!listeners.isEmpty()) {
					notifier.notifySupply(article, 0, n, quantity);
				}
			}

			return n;
		}

		@Override
		public TransactionDelegate getTransactionDelegate() {
			return SingleArticleStore.this;
		}

		@Override
		public long apply(int handle, Article article, long count, boolean simulate) {
			if(handle == 0 && article != null && article.equals(storedArticle)) {
				return apply(article, count, simulate);
			} else {
				return 0;
			}
		}

		@Override
		public Article suggestArticle(ArticleType<?> type) {
			return getAnyArticle().article();
		}
	}

	@Override
	public int handleCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return quantity == 0;
	}

	@Override
	public StoredArticleView view(int handle) {
		return view.prepare(handle == 0 ? storedArticle : Article.NOTHING, quantity, handle);
	}

	@Override
	public boolean isFull() {
		return quantity >= capacity;
	}


	@Override
	public void clear() {
		if (quantity != 0) {
			rollbackHandler.prepareIfNeeded();

			if(!listeners.isEmpty()) {
				notifier.notifySupply(storedArticle, 0, quantity, 0);
			}

			storedArticle = Article.NOTHING;
			quantity = 0;
		}
	}

	@Override
	protected Object createRollbackState() {
		return Triple.of(storedArticle, quantity, capacity);
	}

	@Override
	protected void applyRollbackState(Object state, boolean isCommitted) {
		if(!isCommitted) {
			@SuppressWarnings("unchecked")
			final Triple<Article, Long, Long> triple = (Triple<Article, Long, Long>) state;
			storedArticle = triple.getLeft();
			quantity = triple.getMiddle();
			capacity = triple.getRight();
		}
	}

	@Override
	protected void sendFirstListenerUpdate(StorageListener listener) {
		listener.onCapacityChange(this, capacity);
		listener.onAccept(this, 0, storedArticle, quantity, quantity);
	}

	@Override
	protected void sendLastListenerUpdate(StorageListener listener) {
		listener.onSupply(this, 0, storedArticle, quantity, 0);
		listener.onCapacityChange(this, 0);
	}

	@Override
	public long count() {
		return quantity;
	}

	@Override
	public long capacity() {
		return capacity;
	}

	public void writeTag(CompoundTag tag) {
		tag.putLong("capacity", capacity);
		tag.putLong("quantity", quantity);
		tag.put("art", ObjectUtils.defaultIfNull(storedArticle, Article.NOTHING).toTag());
	}

	@Override
	public CompoundTag writeTag() {
		final CompoundTag result = new CompoundTag();
		writeTag(result);
		return result;
	}

	@Override
	public void readTag(CompoundTag tag) {
		capacity = tag.getLong("capacity");
		quantity = tag.getLong("quantity");
		storedArticle = Article.fromTag(tag.get("art"));
	}

	@Override
	protected void onListenersEmpty() {
		// NOOP
	}
}
