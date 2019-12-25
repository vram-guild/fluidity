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

import java.util.Arrays;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;

import grondag.fluidity.api.item.Article;
import grondag.fluidity.api.storage.discrete.InventoryStorage;
import grondag.fluidity.base.article.DiscreteStoredArticle;
import grondag.fluidity.base.storage.component.FlexibleArticleManager;
import grondag.fluidity.base.transact.TransactionHelper;
import grondag.fluidity.impl.CommonItem;

/**
 *
 * The naive, copy-all-the-stacks approach used here for transaction support is
 * heavy on allocation and could be problematic for very large inventories or very
 * large transaction. A journaling approach that captures changes as they are made
 * is likely to be preferable for performant implementations.
 */
@API(status = Status.EXPERIMENTAL)
public class SlottedCommonStorage extends AbstractDiscreteStorage<SlottedCommonStorage> implements InventoryStorage {
	protected final int slotCount;
	protected final ItemStack[] stacks;

	public SlottedCommonStorage(int slotCount) {
		super(slotCount, slotCount * 64, new FlexibleArticleManager<>(slotCount, DiscreteStoredArticle::new));
		this.slotCount = slotCount;
		stacks = new ItemStack[slotCount];
		Arrays.fill(stacks, ItemStack.EMPTY);

	}

	@Override
	public ItemStack getInvStack(int slot) {
		return slot >= 0 && slot < slotCount ? stacks[slot] : ItemStack.EMPTY;
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
					notifyAccept(newStack, delta);
				} else {
					notifySupply(newStack, -delta);
				}
			}
		} else {
			notifySupply(currentStack, currentStack.getCount());
			needAcceptNotify = true;
		}

		rollbackHandler.prepareIfNeeded();
		stacks[slot] = newStack;
		markDirty();
		dirtyNotifier.run();

		if(needAcceptNotify) {
			notifyAccept(newStack, newStack.getCount());
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
		notifySupply(stack, n);
		final ItemStack result = stack.copy();
		result.setCount(n);
		stack.decrement(n);
		markDirty();
		dirtyNotifier.run();

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
		notifySupply(stack, stack.getCount());
		stacks[slot] = ItemStack.EMPTY;
		dirtyNotifier.run();

		return stack;
	}

	@Override
	public void clear() {
		if(!isEmpty()) {
			rollbackHandler.prepareIfNeeded();

			for(int i = 0 ; i < slotCount; i++) {
				final ItemStack stack = stacks[i];

				if (!stack.isEmpty()) {
					notifySupply(stack, stack.getCount());
					stacks[i] = ItemStack.EMPTY;
				}
			}

			markDirty();

			articles.clear();
			notifier.setCapacity(slotCount * 64);
			dirtyNotifier.run();
		}
	}

	@Override
	protected Object createRollbackState() {
		return TransactionHelper.prepareInventoryRollbackState(SlottedCommonStorage.this);
	}

	@Override
	protected void applyRollbackState(Object state) {
		TransactionHelper.applyInventoryRollbackState(state, this);
	}

	@Override
	public long accept(Article itemIn, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		final CommonItem item = (CommonItem) itemIn;

		if(item.isEmpty() || count == 0 || !filter.test(item)) {
			return 0;
		}

		int result = 0;
		boolean needsRollback = true;

		for(int i = 0 ; i < slotCount; i++) {
			final ItemStack stack = stacks[i];

			if(stack.isEmpty()) {
				final int n = (int) Math.min(count - result, item.getItem().getMaxCount());

				if(!simulate) {
					if(needsRollback) {
						rollbackHandler.prepareIfNeeded();
						needsRollback = false;
					}

					final ItemStack newStack = item.toStack(n);
					notifyAccept(newStack, n);
					stacks[i] = newStack;
					dirtyNotifier.run();
				}

				result += n;
			} else if(item.matches(stack)) {
				final int n = (int) Math.min(count - result, item.getItem().getMaxCount() - stack.getCount());

				if(!simulate) {
					if(needsRollback) {
						rollbackHandler.prepareIfNeeded();
						needsRollback = false;
					}

					stack.increment(n);
					notifyAccept(stack, n);
					dirtyNotifier.run();
				}

				result += n;
			}

			if (result == count) {
				break;
			}
		}

		return result;
	}

	@Override
	public long supply(Article itemIn, long count, boolean simulate) {
		final CommonItem item = (CommonItem) itemIn;

		if(item.isEmpty() || count == 0) {
			return 0;
		}

		int result = 0;
		boolean needsRollback = true;

		for(int i = 0 ; i < slotCount; i++) {
			final ItemStack stack = stacks[i];

			if(item.matches(stack)) {
				final int n = (int) Math.min(count - result, stack.getCount());

				if(!simulate) {
					if(needsRollback) {
						rollbackHandler.prepareIfNeeded();
						needsRollback = false;
					}

					notifySupply(stack, n);
					stack.decrement(n);
					dirtyNotifier.run();
				}

				result += n;
			}

			if (result == count) {
				break;
			}
		}

		return result;
	}

	protected void notifySupply(ItemStack stack, int count) {
		final boolean isEmpty = stack.getCount() == count;

		if(isEmpty && stack.getMaxCount() != 64) {
			notifier.changeCapacity(64 - stack.getMaxCount());
		}

		final DiscreteStoredArticle article = articles.findOrCreateArticle(CommonItem.of(stack));
		notifier.notifySupply(article, count);
		article.count -= count;
	}

	protected void notifyAccept(ItemStack stack, int count) {
		final int newCount = stack.getCount();

		if(newCount == count && stack.getMaxCount() != 64) {
			notifier.changeCapacity(stack.getMaxCount() - 64);
		}

		final DiscreteStoredArticle article = articles.findOrCreateArticle(CommonItem.of(stack));
		article.count += count;
		notifier.notifyAccept(article, count);
	}

	@Override
	public int getInvSize() {
		return slotCount;
	}
}
