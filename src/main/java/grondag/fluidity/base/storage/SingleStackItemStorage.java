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
package grondag.fluidity.base.storage;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.api.storage.InventoryStorage;
import grondag.fluidity.base.article.DiscreteArticle;

@API(status = Status.EXPERIMENTAL)
public class SingleStackItemStorage extends AbstractLazyRollbackStorage<DiscreteArticleView,  DiscreteStorageListener, DiscreteItem> implements InventoryStorage {
	protected ItemStack stack = ItemStack.EMPTY;
	protected final DiscreteArticle view = new DiscreteArticle();

	@Override
	public int handleCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public DiscreteArticleView view(int handle) {
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
		notifySupply(n);
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
		notifySupply(stack.getCount());
		final ItemStack result = stack;
		stack = ItemStack.EMPTY;

		return result;
	}

	@Override
	public void setInvStack(int slot, ItemStack newStack) {
		Preconditions.checkElementIndex(slot, 1, "Invalid slot number");

		final boolean needAcceptNotify;

		if (ItemStack.areItemsEqual(newStack, stack)) {
			if(newStack.getCount() == stack.getCount()) {
				return;
			} else {
				final int delta = newStack.getCount() - stack.getCount();
				needAcceptNotify = false;

				if(delta > 0) {
					notifyAccept(delta);
				} else {
					notifySupply(-delta);
				}
			}
		} else {
			notifySupply(stack.getCount());
			needAcceptNotify = true;
		}

		rollbackHandler.prepareIfNeeded();
		stack = newStack;
		markDirty();

		if(needAcceptNotify) {
			notifyAccept(stack.getCount());
		}
	}

	@Override
	public void clear() {
		if (!stack.isEmpty()) {
			rollbackHandler.prepareIfNeeded();
			notifySupply(stack.getCount());
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

	protected void notifySupply(int count) {
		final int listenCount = listeners.size();

		if(listenCount > 0) {
			final DiscreteItem item = DiscreteItem.of(stack);

			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onSupply(this, 0, item, count, stack.getCount() - count);
			}
		}
	}

	protected void notifyAccept(int count) {
		final int listenCount = listeners.size();

		if(listenCount > 0) {
			final DiscreteItem item = DiscreteItem.of(stack);

			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onAccept(this, 0, item, count, stack.getCount());
			}
		}
	}

	protected void notifyCapacityChange(int capacityDelta) {
		final int listenCount = listeners.size();

		if(listenCount > 0) {
			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onCapacityChange(this, capacityDelta);
			}
		}
	}

	@Override
	public long accept(DiscreteItem item, long count, boolean simulate) {
		if(item.isEmpty()) {
			return 0;
		}

		final int maxCount = item.getItem().getMaxCount();

		if(stack.isEmpty()) {
			final int n = (int) Math.min(count, maxCount);

			if(!simulate) {
				rollbackHandler.prepareIfNeeded();
				stack = item.toStack(n);
				notifyAccept(n);

				if(maxCount != 64) {
					notifyCapacityChange(maxCount - 64);
				}
			}

			return n;
		} else if(item.matches(stack)) {
			final int n = (int) Math.min(count, item.getItem().getMaxCount() - stack.getCount());

			if(!simulate) {
				rollbackHandler.prepareIfNeeded();
				stack.increment(n);
				notifyAccept(n);
			}

			return n;
		} else {
			return 0;
		}
	}

	@Override
	public long supply(DiscreteItem item, long count, boolean simulate) {
		if(item.isEmpty() || stack.isEmpty() || !item.matches(stack)) {
			return 0;
		}

		final int n = (int) Math.min(count, stack.getCount());

		if(!simulate) {
			final int oldMax = stack.getMaxCount();

			rollbackHandler.prepareIfNeeded();
			notifySupply(n);
			stack.decrement(n);

			if(stack.isEmpty() && oldMax != 64) {
				notifyCapacityChange(64 - oldMax);
			}
		}

		return n;
	}

	@Override
	protected void sendFirstListenerUpdate(DiscreteStorageListener listener) {
		listener.onCapacityChange(this, stack.getMaxCount());
		listener.onAccept(this, 0, DiscreteItem.of(stack), stack.getCount(), stack.getCount());
	}

	@Override
	public long count() {
		return stack.getCount();
	}

	@Override
	public long capacity() {
		return stack.isEmpty() ? 64 : stack.getMaxCount();
	}
}
