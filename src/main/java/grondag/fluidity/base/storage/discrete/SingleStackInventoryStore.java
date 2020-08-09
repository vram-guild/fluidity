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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.storage.InventoryStore;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.AbstractLazyRollbackStore;
import grondag.fluidity.base.storage.discrete.FixedDiscreteStore.FixedDiscreteArticleFunction;
import grondag.fluidity.base.storage.discrete.helper.DiscreteNotifier;
import grondag.fluidity.impl.article.ArticleImpl;
import grondag.fluidity.impl.article.StackHelper;

@API(status = Status.EXPERIMENTAL)
public class SingleStackInventoryStore extends AbstractLazyRollbackStore<StoredDiscreteArticle,  SingleStackInventoryStore> implements DiscreteStore, InventoryStore {
	protected ItemStack stack = ItemStack.EMPTY;
	protected ItemStack cleanStack = ItemStack.EMPTY;
	protected final StoredDiscreteArticle view = new StoredDiscreteArticle();
	protected final DiscreteNotifier notifier = new DiscreteNotifier(this);

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

			final int maxCount = article.toItem().getMaxCount();

			if(stack.isEmpty()) {
				final int n = (int) Math.min(count, maxCount);

				if(!simulate) {
					rollbackHandler.prepareIfNeeded();
					stack = article.toStack(n);
					cleanStack = stack.copy();

					if(!listeners.isEmpty()) {
						notifier.notifyAccept(article, 0, n, n);

						if(maxCount != 64) {
							notifier.notifyCapacityChange(maxCount - 64);
						}
					}
				}

				return n;
			} else if(article.matches(stack)) {
				final int n = (int) Math.min(count, article.toItem().getMaxCount() - stack.getCount());

				if(!simulate) {
					rollbackHandler.prepareIfNeeded();
					stack.increment(n);
					cleanStack = stack.copy();

					if(!listeners.isEmpty()) {
						notifier.notifyAccept(article, 0, n, stack.getCount());
					}
				}

				return n;
			} else {
				return 0;
			}
		}

		@Override
		public TransactionDelegate getTransactionDelegate() {
			return SingleStackInventoryStore.this;
		}

		@Override
		public long apply(int handle, Article item, long count, boolean simulate) {
			// implement in subtypes
			throw new UnsupportedOperationException();
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
			if(article.isNothing() || stack.isEmpty() || !article.matches(stack)) {
				return 0;
			}

			final int n = (int) Math.min(count, stack.getCount());

			if(!simulate) {
				final int oldMax = stack.getMaxCount();

				rollbackHandler.prepareIfNeeded();
				stack.decrement(n);
				cleanStack = stack.copy();

				if(!listeners.isEmpty()) {
					notifier.notifySupply(article, 0, n, stack.getCount());

					if(stack.isEmpty() && oldMax != 64) {
						notifier.notifyCapacityChange(64 - oldMax);
					}
				}
			}

			return n;
		}

		@Override
		public TransactionDelegate getTransactionDelegate() {
			return SingleStackInventoryStore.this;
		}

		@Override
		public long apply(int handle, Article item, long count, boolean simulate) {
			// implement in subtypes
			throw new UnsupportedOperationException();
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
	public StoredArticleView view(int handle) {
		return view.prepare(handle == 0 ? stack : ItemStack.EMPTY, handle);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public boolean isFull() {
		return !stack.isEmpty() && stack.getCount() >= stack.getMaxCount();
	}

	@Override
	public ItemStack getStack(int slot) {
		return slot == 0 ? stack : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStack(int slot, int count) {
		if (slot != 0 || stack.isEmpty() || count == 0) {
			return ItemStack.EMPTY;
		}

		rollbackHandler.prepareIfNeeded();
		final int n = Math.min(count, stack.getCount());

		if(!listeners.isEmpty()) {
			notifier.notifySupply(ArticleImpl.of(stack), 0, n, stack.getCount() - n);
		}

		final ItemStack result = stack.copy();
		result.setCount(n);
		stack.decrement(n);
		cleanStack = stack.copy();

		return result;
	}

	@Override
	public ItemStack removeStack(int slot) {
		if (slot != 0 || stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		rollbackHandler.prepareIfNeeded();

		if(!listeners.isEmpty()) {
			notifier.notifySupply(ArticleImpl.of(stack), 0, stack.getCount(), 0);
		}

		final ItemStack result = stack;
		stack = ItemStack.EMPTY;
		cleanStack = ItemStack.EMPTY;

		return result;
	}

	@Override
	public void setStack(int slot, ItemStack newStack) {
		Preconditions.checkElementIndex(slot, 1, "Invalid slot number");

		if (StackHelper.areItemsEqual(newStack, stack)) {
			if(newStack.getCount() == stack.getCount()) {
				return;
			} else {
				final int delta = newStack.getCount() - stack.getCount();

				if(!listeners.isEmpty()) {
					if(delta > 0) {
						notifier.notifyAccept(ArticleImpl.of(stack), 0, delta, newStack.getCount());
					} else {
						notifier.notifySupply(ArticleImpl.of(stack), 0, -delta, newStack.getCount());
					}
				}
			}
		} else {
			if(!listeners.isEmpty()) {
				notifier.notifySupply(ArticleImpl.of(stack), 0, stack.getCount(), 0);
				notifier.notifyAccept(ArticleImpl.of(newStack), 0, newStack.getCount(), newStack.getCount());
			}
		}

		rollbackHandler.prepareIfNeeded();
		stack = newStack;
		cleanStack = stack.copy();
	}

	@Override
	public void clear() {
		if (!stack.isEmpty()) {
			rollbackHandler.prepareIfNeeded();

			if(!listeners.isEmpty()) {
				notifier.notifySupply(ArticleImpl.of(stack), 0, stack.getCount(), 0);
			}

			stack = ItemStack.EMPTY;
			cleanStack = ItemStack.EMPTY;
		}
	}

	@Override
	protected Object createRollbackState() {
		return stack.copy();
	}

	@Override
	protected void applyRollbackState(Object state, boolean isCommitted) {
		if(!isCommitted) {
			stack = (ItemStack) state;
			cleanStack = stack.copy();
		}
	}

	@Override
	protected void sendFirstListenerUpdate(StorageListener listener) {
		listener.onCapacityChange(this, stack.getMaxCount());
		listener.onAccept(this, 0, ArticleImpl.of(stack), stack.getCount(), stack.getCount());
	}

	@Override
	protected void sendLastListenerUpdate(StorageListener listener) {
		listener.onSupply(this, 0, ArticleImpl.of(stack), stack.getCount(), 0);
		listener.onCapacityChange(this, 0);
	}

	@Override
	public long count() {
		return stack.getCount();
	}

	@Override
	public long capacity() {
		return stack.isEmpty() ? 64 : stack.getMaxCount();
	}

	@Override
	public CompoundTag writeTag() {
		return stack.getTag();
	}

	@Override
	public void readTag(CompoundTag tag) {
		stack = ItemStack.fromTag(tag);
		cleanStack = stack.copy();
	}

	@Override
	protected void onListenersEmpty() {
		// NOOP
	}

	@Override
	public void markDirty() {
		if (StackHelper.areItemsEqual(stack, cleanStack)) {
			if(stack.getCount() == cleanStack.getCount()) {
				return;
			} else {
				final int delta = stack.getCount() - cleanStack.getCount();

				if(!listeners.isEmpty()) {
					if(delta > 0) {
						notifier.notifyAccept(ArticleImpl.of(stack), 0, delta, stack.getCount());
					} else {
						notifier.notifySupply(ArticleImpl.of(stack), 0, -delta, stack.getCount());
					}
				}
			}
		} else if  (!listeners.isEmpty()) {
			notifier.notifySupply(ArticleImpl.of(cleanStack), 0, cleanStack.getCount(), 0);
			notifier.notifyAccept(ArticleImpl.of(stack), 0, stack.getCount(), stack.getCount());
		}

		cleanStack = stack.copy();
	}
}
