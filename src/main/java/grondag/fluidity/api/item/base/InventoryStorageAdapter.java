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
import grondag.fluidity.api.storage.StorageImpl;
import grondag.fluidity.api.transact.TransactionContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class InventoryStorageAdapter extends StorageImpl<ItemStack, Void, ItemArticleView> implements ItemStorage<Void> {
	protected final Inventory inventory;

	public InventoryStorageAdapter(Inventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public long capacity() {
		return inventory.getInvMaxStackAmount() * inventory.getInvSize();
	}

	@Override
	public long capacityAvailable() {
		int result = 0;
		final int size = inventory.getInvSize();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getInvStack(i);
			if (stack.isEmpty()) {
				result += 64;
			} else {
				result += stack.getMaxCount() - stack.getCount();
			}
		}
		return result;
	}

	@Override
	public boolean hasDynamicSlots() {
		return false;
	}

	@Override
	public int slotCount() {
		return inventory.getInvSize();
	}

	@Override
	public boolean isEmpty() {
		return inventory.isInvEmpty();
	}
	
	@Override
	public long accept(ItemStack article, long count, boolean simulate) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long supply(ItemStack article, long count, boolean simulate) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void forEach(Void connection, Predicate<ItemArticleView> filter, Predicate<ItemArticleView> consumer) {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemArticleView view(int slot) {
		return new ItemStackView(inventory.getInvStack(slot), slot);
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		// TODO Auto-generated method stub
		return null;
	}
}
