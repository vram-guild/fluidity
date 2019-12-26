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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.storage.InventoryStorage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.DiscreteStoredArticle;
import grondag.fluidity.base.storage.AbstractLazyRollbackStorage;
import grondag.fluidity.base.storage.component.DiscreteNotifier;
import grondag.fluidity.impl.ArticleImpl;

@API(status = Status.EXPERIMENTAL)
public class SingleStackInventoryStorage extends AbstractLazyRollbackStorage<DiscreteStoredArticle,  SingleStackInventoryStorage> implements DiscreteStorage, InventoryStorage {
	protected ItemStack stack = ItemStack.EMPTY;
	protected final DiscreteStoredArticle view = new DiscreteStoredArticle();
	protected final DiscreteNotifier notifier = new DiscreteNotifier(this);

	@Override
	public int handleCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public StoredArticleView view(int handle) {
		return view.prepare(handle == 0 ? stack : ItemStack.EMPTY, handle);
	}

	@Override
	public int getInvSize() {
		return 1;
	}

	@Override
	public boolean isInvEmpty() {
		return stack.isEmpty();
	}

	@Override
	public ItemStack getInvStack(int slot) {
		return slot == 0 ? stack : ItemStack.EMPTY;
	}

	@Override
	public ItemStack takeInvStack(int slot, int count) {
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
		markDirty();

		return result;
	}

	@Override
	public ItemStack removeInvStack(int slot) {
		if (slot != 0 || stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		rollbackHandler.prepareIfNeeded();

		if(!listeners.isEmpty()) {
			notifier.notifySupply(ArticleImpl.of(stack), 0, stack.getCount(), 0);
		}

		final ItemStack result = stack;
		stack = ItemStack.EMPTY;

		return result;
	}

	@Override
	public void setInvStack(int slot, ItemStack newStack) {
		Preconditions.checkElementIndex(slot, 1, "Invalid slot number");

		boolean needAcceptNotify = false;

		if (ItemStack.areItemsEqual(newStack, stack)) {
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
				needAcceptNotify = true;
			}
		}

		rollbackHandler.prepareIfNeeded();
		stack = newStack;
		markDirty();

		if(needAcceptNotify) {
			notifier.notifySupply(ArticleImpl.of(newStack), 0, newStack.getCount(), newStack.getCount());
		}
	}

	@Override
	public void clear() {
		if (!stack.isEmpty()) {
			rollbackHandler.prepareIfNeeded();

			if(!listeners.isEmpty()) {
				notifier.notifySupply(ArticleImpl.of(stack), 0, stack.getCount(), 0);
			}

			stack = ItemStack.EMPTY;
			markDirty();
		}
	}

	@Override
	protected Object createRollbackState() {
		return stack.copy();
	}

	@Override
	protected void applyRollbackState(Object state) {
		stack = (ItemStack) state;
	}

	@Override
	public long accept(Article article, long count, boolean simulate) {

		if(article.isNothing()) {
			return 0;
		}

		final int maxCount = article.toItem().getMaxCount();

		if(stack.isEmpty()) {
			final int n = (int) Math.min(count, maxCount);

			if(!simulate) {
				rollbackHandler.prepareIfNeeded();
				stack = article.toStack(n);

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
	public long supply(Article article, long count, boolean simulate) {
		if(article.isNothing() || stack.isEmpty() || !article.matches(stack)) {
			return 0;
		}

		final int n = (int) Math.min(count, stack.getCount());

		if(!simulate) {
			final int oldMax = stack.getMaxCount();

			rollbackHandler.prepareIfNeeded();
			stack.decrement(n);

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
	protected void sendFirstListenerUpdate(StorageListener listener) {
		listener.onCapacityChange(this, stack.getMaxCount());
		listener.onAccept(this, 0, ArticleImpl.of(stack), stack.getCount(), stack.getCount());
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
	}

	@Override
	protected void onListenersEmpty() {
		// NOOP
	}
}
