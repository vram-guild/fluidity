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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.storage.ItemStorage;
import grondag.fluidity.api.storage.view.ArticleView;
import grondag.fluidity.api.transact.TransactionContext;

public class SingleStackStorage implements ItemStorage  {
	protected ItemStack stack = ItemStack.EMPTY;
	protected final ItemStackView view = new ItemStackView();
	protected final List<Consumer<? super ArticleView>> listeners = new ArrayList<>();
	protected final Consumer<TransactionContext> rollbackHandler = this::handleRollback;

	@Override
	public int slotCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public boolean hasDynamicSlots() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ArticleView> T view(int slot) {
		return slot == 0 ? (T) view.prepare(stack, 0) : null;
	}

	@Override
	public void startListening(Consumer<? super ArticleView> listener, Object connection, Predicate<? super ArticleView> articleFilter) {
		listeners.add(listener);

		this.forEach(v -> {
			listener.accept(v);
			return true;
		});
	}

	@Override
	public void stopListening(Consumer<? super ArticleView> listener) {
		listeners.remove(listener);
	}

	<T extends ArticleView> void notifyListeners(T article) {
		final List<Consumer<? super ArticleView>> listeners = this.listeners;

		final int limit = listeners.size();

		for (int i = 0; i < limit; i++) {
			listeners.get(i).accept(article);
		}
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		context.setState(stack.copy());
		return rollbackHandler;
	}

	protected void handleRollback(TransactionContext context) {
		stack = context.getState();
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
	public ItemStack getInvStack(int i) {
		return i == 0 ? stack : null;
	}

	@Override
	public ItemStack takeInvStack(int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack removeInvStack(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInvStack(int i, ItemStack itemStack) {
		// TODO Auto-generated method stub

	}

	@Override
	public void markDirty() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public long accept(Item item, CompoundTag tag, long count, boolean simulate) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long supply(Item item, CompoundTag tag, long count, boolean simulate) {
		// TODO Auto-generated method stub
		return 0;
	}
}
