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

import java.util.Arrays;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.InventoryStore;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.discrete.FixedDiscreteStore.FixedDiscreteArticleFunction;
import grondag.fluidity.base.storage.helper.FlexibleArticleManager;
import grondag.fluidity.base.transact.TransactionHelper;
import grondag.fluidity.impl.article.ArticleImpl;
import grondag.fluidity.impl.article.StackHelper;

/**
 * The naive, copy-all-the-stacks approach used here for transaction support is
 * heavy on allocation and could be problematic for very large inventories or very
 * large transaction. A journaling approach that captures changes as they are made
 * is likely to be preferable for performant implementations.
 */
@Experimental
public class SlottedInventoryStore extends AbstractDiscreteStore<SlottedInventoryStore> implements InventoryStore {
	protected final int slotCount;
	protected final ItemStack[] stacks;
	protected final ItemStack[] cleanStacks;

	public SlottedInventoryStore(int slotCount) {
		super(slotCount, slotCount * 64, new FlexibleArticleManager<>(slotCount, StoredDiscreteArticle::new));
		this.slotCount = slotCount;
		stacks = new ItemStack[slotCount];
		cleanStacks = new ItemStack[slotCount];
		Arrays.fill(stacks, ItemStack.EMPTY);
		Arrays.fill(cleanStacks, ItemStack.EMPTY);
	}

	protected void synchCleanStack(int slot) {
		final ItemStack stack = stacks[slot];
		final ItemStack cleanStack = cleanStacks[slot];

		if (StackHelper.areStacksEqual(stack, cleanStack)) {
			cleanStack.setCount(stack.getCount());
		} else {
			cleanStacks[slot] = stack == ItemStack.EMPTY ? ItemStack.EMPTY : stack.copy();
		}
	}

	@Override
	public ItemStack getItem(int slot) {
		Preconditions.checkElementIndex(slot, slotCount, "Invalid slot");
		return stacks[slot];
	}

	@Override
	public void setItem(int slot, ItemStack newStack) {
		Preconditions.checkNotNull(newStack, "ItemStack must be non-null");
		Preconditions.checkElementIndex(slot, slotCount, "Invalid slot");

		final ItemStack currentStack = stacks[slot];
		final boolean needAcceptNotify;

		rollbackHandler.prepareIfNeeded();

		if (ItemStack.isSameItemSameTags(newStack, currentStack)) {
			if (newStack.getCount() == currentStack.getCount()) {
				return;
			} else {
				final int delta = newStack.getCount() - currentStack.getCount();
				needAcceptNotify = false;

				if (delta > 0) {
					notifyAccept(newStack, delta);
				} else {
					notifySupply(newStack, -delta);
				}
			}
		} else {
			notifySupply(currentStack, currentStack.getCount());
			needAcceptNotify = true;
		}

		stacks[slot] = newStack;
		synchCleanStack(slot);
		dirtyNotifier.run();

		if (needAcceptNotify) {
			notifyAccept(newStack, newStack.getCount());
		}
	}

	@Override
	public ItemStack removeItem(int slot, int count) {
		Preconditions.checkElementIndex(slot, slotCount, "Invalid slot");

		final ItemStack stack = stacks[slot];

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		rollbackHandler.prepareIfNeeded();
		final int n = Math.min(count, stack.getCount());
		notifySupply(stack, n);
		final ItemStack result = stack.copy();
		result.setCount(n);
		stack.shrink(n);
		synchCleanStack(slot);
		dirtyNotifier.run();

		return result;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		Preconditions.checkElementIndex(slot, slotCount, "Invalid slot");

		final ItemStack stack = stacks[slot];

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		rollbackHandler.prepareIfNeeded();
		notifySupply(stack, stack.getCount());
		stacks[slot] = ItemStack.EMPTY;
		cleanStacks[slot] = ItemStack.EMPTY;
		dirtyNotifier.run();

		return stack;
	}

	@Override
	public void clearContent() {
		if (!isEmpty()) {
			rollbackHandler.prepareIfNeeded();

			for (int i = 0; i < slotCount; i++) {
				final ItemStack stack = stacks[i];

				if (!stack.isEmpty()) {
					notifySupply(stack, stack.getCount());
					stacks[i] = ItemStack.EMPTY;
					cleanStacks[i] = ItemStack.EMPTY;
				}
			}

			articles.clear();
			notifier.setCapacity(slotCount * 64);
			dirtyNotifier.run();
		}
	}

	@Override
	protected Object createRollbackState() {
		return TransactionHelper.prepareInventoryRollbackState(SlottedInventoryStore.this);
	}

	@Override
	protected void applyRollbackState(Object state, boolean isCommitted) {
		if (!isCommitted) {
			TransactionHelper.applyInventoryRollbackState(state, this);
		}
	}

	@Override
	protected FixedDiscreteArticleFunction createConsumer() {
		return new SlottedInventoryStore.Consumer();
	}

	protected class Consumer extends AbstractDiscreteStore<DividedDiscreteStore>.Consumer {
		@Override
		public long apply(Article article, long count, boolean simulate) {
			Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);

			if (article.isNothing() || count == 0 || !filter.test(article)) {
				return 0;
			}

			int result = 0;
			boolean needsRollback = true;

			for (int slot = 0; slot < slotCount; slot++) {
				final ItemStack stack = stacks[slot];

				if (stack.isEmpty()) {
					final int n = (int) Math.min(count - result, article.toItem().getMaxStackSize());

					if (!simulate) {
						if (needsRollback) {
							rollbackHandler.prepareIfNeeded();
							needsRollback = false;
						}

						final ItemStack newStack = article.toStack(n);
						notifyAccept(newStack, n);
						stacks[slot] = newStack;
						synchCleanStack(slot);
						dirtyNotifier.run();
					}

					result += n;
				} else if (article.matches(stack)) {
					final int n = (int) Math.min(count - result, article.toItem().getMaxStackSize() - stack.getCount());

					if (!simulate) {
						if (needsRollback) {
							rollbackHandler.prepareIfNeeded();
							needsRollback = false;
						}

						stack.grow(n);
						synchCleanStack(slot);
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
	}

	@Override
	protected FixedDiscreteArticleFunction createSupplier() {
		return new SlottedInventoryStore.Supplier();
	}

	protected class Supplier extends AbstractDiscreteStore<DividedDiscreteStore>.Supplier {
		@Override
		public long apply(Article article, long count, boolean simulate) {
			if (article.isNothing() || count == 0) {
				return 0;
			}

			int result = 0;
			boolean needsRollback = true;

			for (int slot = 0; slot < slotCount; slot++) {
				final ItemStack stack = stacks[slot];

				if (article.matches(stack) && !stack.isEmpty()) {
					final int n = (int) Math.min(count - result, stack.getCount());

					if (!simulate) {
						if (needsRollback) {
							rollbackHandler.prepareIfNeeded();
							needsRollback = false;
						}

						notifySupply(stack, n);
						stack.shrink(n);

						if (stack.isEmpty()) {
							stacks[slot] = ItemStack.EMPTY;
						}

						synchCleanStack(slot);
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
	}

	protected void notifySupply(ItemStack stack, int count) {
		final boolean isEmpty = stack.getCount() == count;

		if (isEmpty && stack.getMaxStackSize() != 64) {
			notifier.addToCapacity(64 - stack.getMaxStackSize());
		}

		final StoredDiscreteArticle article = articles.findOrCreateArticle(ArticleImpl.of(stack));
		notifier.notifySupply(article, count);
		article.addToCount(-count);
	}

	protected void notifyAccept(ItemStack stack, int count) {
		final int newCount = stack.getCount();

		if (newCount == count && stack.getMaxStackSize() != 64) {
			notifier.addToCapacity(stack.getMaxStackSize() - 64);
		}

		final StoredDiscreteArticle article = articles.findOrCreateArticle(ArticleImpl.of(stack));
		article.addToCount(count);
		notifier.notifyAccept(article, count);
	}

	@Override
	public int getContainerSize() {
		return slotCount;
	}

	@Override
	public void setChanged() {
		for (int slot = 0; slot < slotCount; ++slot) {
			final ItemStack stack = stacks[slot];
			final ItemStack cleanStack = cleanStacks[slot];

			if (StackHelper.areItemsEqual(stack, cleanStack)) {
				if (stack.getCount() == cleanStack.getCount()) {
				} else {
					final int delta = stack.getCount() - cleanStack.getCount();

					if (delta > 0) {
						notifyAccept(stack, delta);
					} else {
						notifySupply(cleanStack, -delta);
					}

					cleanStack.setCount(stack.getCount());
				}
			} else {
				notifySupply(cleanStack, cleanStack.getCount());
				notifyAccept(stack, stack.getCount());
				cleanStacks[slot] = stack.copy();
			}
		}
	}

	@Override
	public void readTag(CompoundTag tag) {
		super.readTag(tag);

		for (int slot = 0; slot < slotCount; slot++) {
			cleanStacks[slot] = stacks[slot].copy();
		}
	}
}
