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
package grondag.fluidity.api.item.base;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

import grondag.fluidity.api.item.ItemStorage;
import grondag.fluidity.api.item.StoredItemView;
import grondag.fluidity.api.transact.TransactionContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryListener;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeInputProvider;

public class SimpleItemStorage implements ItemStorage<Void>, Inventory, RecipeInputProvider {
	protected final int size;
	protected final ItemStack[] stacks;
	protected ObjectArrayList<Consumer<StoredItemView>> listeners;
	protected ObjectArrayList<InventoryListener> invListeners;

	public SimpleItemStorage(int size) {
		this.size = size;
		stacks = new ItemStack[size];
		Arrays.fill(stacks, ItemStack.EMPTY);
	}

	@Override
	public long capacity() {
		return size * 64;
	}

	@Override
	public long capacityAvailable() {
		int result = 0;
		for (int i = 0; i < size; i++) {
			ItemStack stack = stacks[i];
			if (stack.isEmpty()) {
				result += 64;
			} else {
				result += stack.getMaxCount() - stack.getCount();
			}
		}
		return result;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < size; i++) {
			if (!stacks[i].isEmpty())
				return false;
		}
		return true;
	}

	@Override
	public boolean fixedSlots() {
		return true;
	}

	@Override
	public int slotCount() {
		return size;
	}

	@Override
	public long accept(ItemStack stack, long count, boolean simulate) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}

		final int size = this.size;
		final ItemStack[] stacks = this.stacks;

		long result = 0;
		ItemStackView view = null;
		boolean isDirty = false;

		for (int i = 0; i < size; i++) {
			final ItemStack candidate = stacks[i];
			if (candidate.isEmpty()) {

				final int n = (int) Math.min(count - result, stack.getMaxCount());
				if (!simulate) {
					final ItemStack newStack = stack.copy();
					isDirty = true;
					newStack.setCount(n);
					stacks[i] = newStack;
					if (!listeners.isEmpty()) {
						if (view == null)
							view = new ItemStackView();
						notifyListeners(view.prepare(newStack, i));
					}
				}
				result += n;

			} else if (stack.isItemEqual(candidate)) {

				final int capacity = candidate.getMaxCount() - candidate.getCount();
				if (capacity > 0) {
					final int n = (int) Math.min(count - result, capacity);
					if (!simulate) {
						candidate.increment(n);
						if (!listeners.isEmpty()) {
							if (view == null)
								view = new ItemStackView();
							notifyListeners(view.prepare(candidate, i));
						}
					}
					result += n;
				}

			}
			if (result == count)
				break;
		}
		if (isDirty)
			notifyInvListeners();
		return result;
	}

	@Override
	public long supply(ItemStack stack, long count, boolean simulate) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}

		final int size = this.size;
		final ItemStack[] stacks = this.stacks;
		long result = 0;
		ItemStackView view = null;
		boolean isDirty = false;

		for (int i = 0; i < size; i++) {
			final ItemStack candidate = stacks[i];
			if (stack.isItemEqual(candidate)) {
				final int n = (int) Math.min(count - result, candidate.getCount());
				if (!simulate) {
					isDirty = true;
					candidate.decrement(n);
					if (candidate.isEmpty())
						stacks[i] = ItemStack.EMPTY;

					if (listeners != null && !listeners.isEmpty()) {
						if (view == null)
							view = new ItemStackView();
						notifyListeners(view.prepare(stacks[i], i));
					}
				}
				result += n;
				if (result == count)
					break;
			}
		}

		if (isDirty)
			notifyInvListeners();
		return result;
	}

	@Override
	public void forEach(Void connection, Predicate<StoredItemView> filter, Predicate<StoredItemView> consumer) {
		final ItemStackView view = new ItemStackView();
		final int size = this.size;
		for (int i = 0; i < size; i++) {
			final ItemStack stack = stacks[i];
			if (!stack.isEmpty()) {
				view.prepare(stack, i);
				if (filter.test(view)) {
					if (!consumer.test(view)) {
						break;
					}
				}
			}
		}
	}

	@Override
	public void forSlot(int slot, Consumer<StoredItemView> consumer) {
		consumer.accept(new ItemStackView(stacks[slot], slot));
	}

	@Override
	public void startListening(Consumer<StoredItemView> listener, Void connection, Predicate<StoredItemView> articleFilter) {
		if (listeners == null) {
			listeners = new ObjectArrayList<>();
		}
		listeners.add(listener);
		final ItemStackView view = new ItemStackView();
		final int size = this.size;
		final ItemStack[] stacks = this.stacks;
		for (int i = 0; i < size; i++) {
			ItemStack stack = stacks[i];
			if (!stack.isEmpty()) {
				listener.accept(view.prepare(stack, i));
			}
		}
	}

	@Override
	public void stopListening(Consumer<StoredItemView> listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	protected void notifyListeners(StoredItemView article) {
		if (this.listeners != null) {
			final int limit = listeners.size();
			for (int i = 0; i < limit; i++) {
				listeners.get(i).accept(article);
			}
		}
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		final int size = this.size;
		final ItemStack[] stacks = this.stacks;
		final ItemStack[] state = new ItemStack[size];
		for (int i = 0; i < size; i++) {
			ItemStack stack = stacks[i];
			state[i] = stack.copy();
		}
		context.setState(state);
		return rollackHandler;
	}

	private final Consumer<TransactionContext> rollackHandler = this::handleRollback;

	private void handleRollback(TransactionContext context) {
		if (!context.isCommited()) {
			final int size = this.size;
			final ItemStack[] stacks = this.stacks;
			final ItemStack[] state = context.getState();
			ItemStackView view = null;
			boolean isDirty = false;

			for (int i = 0; i < size; i++) {
				ItemStack myStack = stacks[i];
				ItemStack stateStack = state[i];
				if (!myStack.isItemEqual(stateStack)) {
					stacks[i] = stateStack;
					isDirty = true;
					if (listeners != null && !listeners.isEmpty()) {
						if (view == null)
							view = new ItemStackView();
						notifyListeners(view.prepare(stateStack, i));
					}
				}
			}
			if (isDirty)
				notifyInvListeners();
		}
	}

	@Override
	public void clear() {
		final int size = this.size;
		ItemStackView view = null;
		boolean isDirty = false;

		for (int i = 0; i < size; i++) {
			final ItemStack stack = stacks[i];
			if (stack != ItemStack.EMPTY) {
				stacks[i] = ItemStack.EMPTY;
				isDirty = true;
				if (listeners != null && !listeners.isEmpty()) {
					if (view == null)
						view = new ItemStackView();
					notifyListeners(view.prepare(ItemStack.EMPTY, i));
				}
			}
		}
		if (isDirty)
			notifyInvListeners();
	}

	@Override
	public int getInvSize() {
		return size;
	}

	@Override
	public boolean isInvEmpty() {
		return this.isEmpty();
	}

	@Override
	public ItemStack getInvStack(int slot) {
		return stacks[slot];
	}

	@Override
	public ItemStack takeInvStack(int slot, int count) {
		if (slot < 0 || slot >= size)
			return ItemStack.EMPTY;
		final ItemStack stack = stacks[slot];

		final int n = Math.min(count, stack.getCount());
		if (n == 0)
			return ItemStack.EMPTY;
		final ItemStack result = stack.copy();
		result.setCount(n);

		stack.decrement(n);
		if (stack.isEmpty())
			stacks[slot] = ItemStack.EMPTY;

		if (listeners != null && !listeners.isEmpty()) {
			notifyListeners(new ItemStackView(stacks[slot], slot));
		}
		notifyInvListeners();
		return result;
	}

	@Override
	public ItemStack removeInvStack(int slot) {
		if (slot < 0 || slot >= size)
			return ItemStack.EMPTY;
		final ItemStack stack = stacks[slot];
		if (stack.isEmpty())
			return ItemStack.EMPTY;

		stacks[slot] = ItemStack.EMPTY;
		if (listeners != null && !listeners.isEmpty()) {
			notifyListeners(new ItemStackView(ItemStack.EMPTY, slot));
		}
		return stack;
	}

	@Override
	public void setInvStack(int slot, ItemStack stackIn) {
		if (slot < 0 || slot >= size)
			return;

		if (!stackIn.isEmpty() && stackIn.getCount() > this.getInvMaxStackAmount()) {
			stackIn.setCount(this.getInvMaxStackAmount());
		}

		final ItemStack stack = stacks[slot];
		if (stack.isItemEqual(stackIn) && stack.getCount() == stackIn.getCount())
			return;

		stacks[slot] = stackIn;

		if (listeners != null && !listeners.isEmpty()) {
			notifyListeners(new ItemStackView(stackIn, slot));
		}
		notifyInvListeners();
	}

	@Override
	public void markDirty() {
		notifyInvListeners();

		if (listeners != null && !listeners.isEmpty()) {
			final ItemStackView view = new ItemStackView();
			for (int i = 0; i < size; i++) {
				notifyListeners(view.prepare(stacks[i], i));
			}
		}
	}

	protected void notifyInvListeners() {
		if (invListeners != null && !invListeners.isEmpty()) {
			final int limit = invListeners.size();
			for (int i = 0; i < limit; i++) {
				invListeners.get(i).onInvChange(this);
			}
		}
	}

	@Override
	public boolean canPlayerUseInv(PlayerEntity player) {
		return true;
	}

	@Override
	public void provideRecipeInputs(RecipeFinder finder) {
		final int size = this.size;
		final ItemStack[] stacks = this.stacks;
		for (int i = 0; i < size; i++) {
			ItemStack stack = stacks[i];
			if (!stack.isEmpty()) {
				finder.addItem(stack);
			}
		}
	}
}
