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

import java.util.Arrays;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.api.storage.InventoryStorage;
import grondag.fluidity.base.article.DiscreteArticle;
import grondag.fluidity.base.transact.TransactionHelper;

/**
 *
 * The naive, copy-all-the-stacks approach used here for transaction support is
 * heavy on allocation and could be problematic for very large inventories or very
 * large transaction. A journaling approach that captures changes as they are made
 * is likely to be preferable for performant implementations.
 */
@API(status = Status.EXPERIMENTAL)
public class SimpleItemStorage extends AbstractLazyRollbackStorage<DiscreteArticleView,  DiscreteStorageListener, DiscreteItem> implements InventoryStorage {
	protected final int slotCount;
	protected long capacity;
	protected long count;
	protected final ItemStack[] stacks;
	protected final DiscreteArticle view = new DiscreteArticle();
	protected Predicate<DiscreteItem> filter = Predicates.alwaysTrue();

	public SimpleItemStorage(int slotCount, @Nullable Predicate<DiscreteItem> filter) {
		this.slotCount = slotCount;
		capacity = slotCount * 64;
		count = 0;
		stacks = new ItemStack[slotCount];
		Arrays.fill(stacks, ItemStack.EMPTY);
		filter(filter);
	}

	public SimpleItemStorage(int slotCount) {
		this(slotCount, null);
	}

	public void filter(Predicate<DiscreteItem> filter) {
		this.filter = filter == null ? Predicates.alwaysTrue() : filter;
	}

	@Override
	public int handleCount() {
		return slotCount;
	}

	@Override
	public DiscreteArticleView view(int slot) {
		return view.prepare(isHandleValid(slot) ? stacks[slot] : ItemStack.EMPTY, slot);
	}

	@Override
	public ItemStack getInvStack(int slot) {
		return isHandleValid(slot) ? stacks[slot] : ItemStack.EMPTY;
	}

	@Override
	public void setInvStack(int slot, ItemStack newStack) {
		Preconditions.checkNotNull(newStack, "ItemStack must be non-null");

		if (!isHandleValid(slot)) {
			return;
		}

		final ItemStack currentStack = stacks[slot];
		final boolean needAcceptNotify;

		if (ItemStack.areItemsEqual(newStack, currentStack)) {
			if(newStack.getCount() == currentStack.getCount()) {
				return;
			} else {
				final int delta = newStack.getCount() - currentStack.getCount();
				needAcceptNotify = false;

				if(delta > 0) {
					notifyAccept(slot, newStack, delta);
				} else {
					notifySupply(slot, newStack, -delta);
				}
			}
		} else {
			notifySupply(slot, currentStack, currentStack.getCount());
			needAcceptNotify = true;
		}

		rollbackHandler.prepareIfNeeded();
		stacks[slot] = newStack;
		markDirty();

		if(needAcceptNotify) {
			notifyAccept(slot, newStack, newStack.getCount());
		}
	}

	@Override
	public ItemStack takeInvStack(int slot, int count) {
		if(!isHandleValid(slot) || count == 0) {
			return ItemStack.EMPTY;
		}

		final ItemStack stack = stacks[slot];

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		rollbackHandler.prepareIfNeeded();
		final int n = Math.min(count, stack.getCount());
		notifySupply(slot, stack, n);
		final ItemStack result = stack.copy();
		result.setCount(n);
		stack.decrement(n);
		markDirty();

		return result;
	}

	@Override
	public ItemStack removeInvStack(int slot) {
		if (slot != 0) {
			return ItemStack.EMPTY;
		}

		final ItemStack stack = stacks[slot];

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		rollbackHandler.prepareIfNeeded();
		notifySupply(slot, stack, stack.getCount());
		stacks[slot] = ItemStack.EMPTY;

		return stack;
	}

	@Override
	public void clear() {
		if(!isEmpty()) {
			rollbackHandler.prepareIfNeeded();

			for(int i = 0 ; i < slotCount; i++) {
				final ItemStack stack = stacks[i];

				if (!stack.isEmpty()) {
					notifySupply(i, stack, stack.getCount());
					stacks[i] = ItemStack.EMPTY;
				}
			}

			markDirty();

			count = 0;
			capacity = slotCount * 64;
		}
	}

	@Override
	protected Object createRollbackState() {
		return TransactionHelper.prepareInventoryRollbackState(SimpleItemStorage.this);
	}

	@Override
	protected void applyRollbackState(Object state) {
		TransactionHelper.applyInventoryRollbackState(state, this);
	}

	@Override
	public long accept(DiscreteItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);

		if(item.isEmpty()) {
			return 0;
		}

		int result = 0;
		boolean needsRollback = true;

		for(int i = 0 ; i < slotCount; i++) {
			final ItemStack stack = stacks[i];

			if(stack.isEmpty()) {
				final int n = (int) Math.min(count, item.getItem().getMaxCount());

				if(!simulate) {
					if(needsRollback) {
						rollbackHandler.prepareIfNeeded();
						needsRollback = false;
					}

					final ItemStack newStack = item.toStack(n);
					notifyAccept(i, newStack, n);
					stacks[i] = newStack;
				}

				return n;
			} else if(item.matches(stack)) {
				final int n = (int) Math.min(count, item.getItem().getMaxCount() - stack.getCount());

				if(!simulate) {
					if(needsRollback) {
						rollbackHandler.prepareIfNeeded();
						needsRollback = false;
					}

					stack.increment(n);
					notifyAccept(i, stack, n);
				}

				result += n;
			}
		}

		return result;
	}

	@Override
	public long supply(DiscreteItem item, long count, boolean simulate) {
		if(item.isEmpty()) {
			return 0;
		}

		int result = 0;
		boolean needsRollback = true;

		for(int i = 0 ; i < slotCount; i++) {
			final ItemStack stack = stacks[i];
			final int n = (int) Math.min(count, stack.getCount());

			if(!simulate) {
				if(needsRollback) {
					rollbackHandler.prepareIfNeeded();
					needsRollback = false;
				}

				notifySupply(i, stack, n);
				stack.decrement(n);
			}

			result += n;
		}

		return result;
	}

	protected void notifySupply(int handle, ItemStack stack, int count) {
		final boolean isEmpty = stack.getCount() == count;

		this.count -= count;

		if(isEmpty && stack.getMaxCount() != 64) {
			notifyCapacityChange(64 - stack.getMaxCount());
		}

		final int listenCount = listeners.size();

		if(listenCount > 0) {
			final DiscreteItem item = DiscreteItem.of(stack);

			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onSupply(this, handle, item, count, stack.getCount());
			}
		}
	}

	protected void notifyAccept(int handle, ItemStack stack, int count) {
		this.count += count;
		final int newCount = stack.getCount();

		if(newCount == count && stack.getMaxCount() != 64) {
			notifyCapacityChange(stack.getMaxCount() - 64);
		}

		final int listenCount = listeners.size();

		if(listenCount > 0) {
			final DiscreteItem item = DiscreteItem.of(stack);
			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onAccept(this, handle, item, count, newCount);
			}
		}
	}

	protected void notifyCapacityChange(int capacityDelta) {
		capacity += capacityDelta;

		final int listenCount = listeners.size();

		if(listenCount > 0) {
			for(int i = 0; i < listenCount; i++) {
				listeners.get(i).onCapacityChange(this, capacityDelta);
			}
		}
	}

	@Override
	protected void sendFirstListenerUpdate(DiscreteStorageListener listener) {
		listener.onCapacityChange(this, capacity);

		for(int i = 0 ; i < slotCount; i++) {
			final ItemStack stack = stacks[i];

			if (!stack.isEmpty()) {
				listener.onAccept(this, i, DiscreteItem.of(stack), stack.getCount(), stack.getCount());
			}
		}
	}

	@Override
	public long count() {
		return count;
	}

	@Override
	public long capacity() {
		return capacity;
	}

	@Override
	public int getInvSize() {
		return slotCount;
	}
}
