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

import java.util.function.Consumer;
import java.util.function.Predicate;

import grondag.fluidity.api.item.ItemArticleView;
import grondag.fluidity.api.item.ItemStorage;
import grondag.fluidity.api.storage.AbstractStorage;
import grondag.fluidity.api.transact.TransactionContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;

public class SingleStackStorage extends AbstractStorage<ItemStack, Void, ItemArticleView> implements ItemStorage<Void> {
	protected ObjectArrayList<Consumer<ItemArticleView>> listeners;
	protected final ItemStackView view = new ItemStackView();

	public SingleStackStorage() {
		view.stack = ItemStack.EMPTY;
		view.slot = 0;
	}

	@Override
	public long capacity() {
		return view.stack.getMaxCount();
	}

	@Override
	public long capacityAvailable() {
		return view.stack.getMaxCount() - view.stack.getCount();
	}

	@Override
	public boolean isEmpty() {
		return view.stack.isEmpty();
	}

	@Override
	public boolean hasDynamicSlots() {
		return true;
	}

	@Override
	public int slotCount() {
		return 1;
	}

	@Override
	public ItemArticleView view(int slot) {
		return view;
	}

	@Override
	public long accept(ItemStack article, long count, boolean simulate) {
		final ItemStack stack = view.stack;
		if (!stack.isItemEqual(article)) {
			return 0;
		}

		int result = Math.min((int) count, stack.getMaxCount() - stack.getCount());

		if (!simulate) {
			stack.increment(result);
			notifyListeners();
		}
		return result;
	}

	@Override
	public long supply(ItemStack article, long count, boolean simulate) {
		final ItemStack stack = view.stack;
		if (!stack.isItemEqual(article)) {
			return 0;
		}

		int result = Math.min((int) count, stack.getCount());

		if (!simulate) {
			stack.decrement(result);
			notifyListeners();
		}
		return result;
	}

	@Override
	public void startListening(Consumer<ItemArticleView> listener, Void connection, Predicate<ItemArticleView> articleFilter) {
		if (listeners == null) {
			listeners = new ObjectArrayList<>();
		}
		listeners.add(listener);
		if (view.stack != null && !view.stack.isEmpty()) {
			listener.accept(view);
		}
	}

	@Override
	public void stopListening(Consumer<ItemArticleView> listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	protected void notifyListeners() {
		if (this.listeners != null) {
			final int limit = listeners.size();
			for (int i = 0; i < limit; i++) {
				listeners.get(i).accept(this.view);
			}
		}
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		context.setState(view.stack.copy());
		return rollackHandler;
	}

	private final Consumer<TransactionContext> rollackHandler = this::handleRollback;

	private void handleRollback(TransactionContext context) {
		if (!context.isCommited()) {
			final ItemStack state = context.getState();
			final ItemStack stack = view.stack;
			if (!stack.isItemEqual(state)) {
				stack.setTag(state.getTag());
				stack.setCount(state.getCount());
				notifyListeners();
			}
		}
	}
}
