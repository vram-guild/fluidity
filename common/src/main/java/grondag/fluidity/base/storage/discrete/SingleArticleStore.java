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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.ApiStatus.Experimental;

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

@Experimental
public class SingleArticleStore extends AbstractLazyRollbackStore<StoredDiscreteArticle, SingleArticleStore> implements DiscreteStore {
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
			if (article.isNothing()) {
				return 0;
			}

			if (quantity == 0) {
				final int n = (int) Math.min(count, capacity);

				if (!simulate) {
					rollbackHandler.prepareIfNeeded();
					storedArticle = article;
					quantity = n;
					dirtyNotifier.run();

					if (!listeners.isEmpty()) {
						notifier.notifyAccept(article, 0, n, n);
					}
				}

				return n;
			} else if (article.equals(storedArticle)) {
				final int n = (int) Math.min(count, capacity - quantity);

				if (!simulate) {
					rollbackHandler.prepareIfNeeded();
					quantity += n;
					dirtyNotifier.run();

					if (!listeners.isEmpty()) {
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
			if (handle == 0 && article != null) {
				return apply(article, count, simulate);
			} else {
				return 0;
			}
		}

		@Override
		public Article suggestArticle(ArticleType<?> type) {
			return type == null ? getAnyArticle().article() : getAnyMatch(type.viewPredicate()).article();
		}
	}

	protected final FixedDiscreteArticleFunction supplier = createSupplier();

	protected FixedDiscreteArticleFunction createSupplier() {
		return new Supplier();
	}

	protected class Supplier implements FixedDiscreteArticleFunction {
		@Override
		public long apply(Article article, long count, boolean simulate) {
			if (article.isNothing() || quantity == 0 || !article.equals(storedArticle)) {
				return 0;
			}

			final int n = (int) Math.min(count, quantity);

			if (!simulate) {
				rollbackHandler.prepareIfNeeded();
				quantity -= n;
				dirtyNotifier.run();

				if (!listeners.isEmpty()) {
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
			if (handle == 0 && article != null && article.equals(storedArticle)) {
				return apply(article, count, simulate);
			} else {
				return 0;
			}
		}

		@Override
		public Article suggestArticle(ArticleType<?> type) {
			return type == null ? getAnyArticle().article() : getAnyMatch(type.viewPredicate()).article();
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

			if (!listeners.isEmpty()) {
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
		if (!isCommitted) {
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
