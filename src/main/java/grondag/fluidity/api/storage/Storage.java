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
package grondag.fluidity.api.storage;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.base.Predicates;

import grondag.fluidity.api.bulk.BulkItem;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.transact.Transactor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public interface Storage extends Transactor {
	boolean isEmpty();

	boolean hasDynamicSlots();
	
	int slotCount();
	
	ItemView view(int slot);
	
	default boolean isSlotVisibleFrom(Object connection) {
		return true;
	}
	
	default void forEach(Object connection, Predicate<ItemView> filter, Predicate<ItemView> action) {
		final int limit = slotCount();
		
		for (int i = 0; i < limit; i++) {
			final ItemView article = view(i);
			
			if (!article.isEmpty() && filter.test(article)) {
				if (!action.test(article)) break;
			}
		}
	}

	default void forEach(Object connection, Predicate<ItemView> action) {
		forEach(connection, Predicates.alwaysTrue(), action);
	}

	default void forEach(Predicate<ItemView> action) {
		forEach(null, Predicates.alwaysTrue(), action);
	}

	void startListening(Consumer<ItemView> listener, Object connection, Predicate<ItemView> articleFilter);

	void stopListening(Consumer<ItemView> listener);
	
	long accept(Item item, CompoundTag tag, long count, boolean simulate);

	default long accept(Item item, long count, boolean simulate) {
		return accept(item, null, count, simulate);
	}

	default long accept(ItemStack stack, long count, boolean simulate) {
		return accept(stack.getItem(), stack.getTag(), count, simulate);
	}
	
	default long accept(ItemStack stack, boolean simulate) {
		return accept(stack.getItem(), stack.getTag(), stack.getCount(), simulate);
	}
	
	long supply(Item item, CompoundTag tag, long count, boolean simulate);
	
	default long supply(Item item, long count, boolean simulate) {
		return supply(item, null, count, simulate);
	}
	
	default long supply(ItemStack stack, long count, boolean simulate) {
		return supply(stack.getItem(), stack.getTag(), count, simulate);
	}
	
	default long supply(ItemStack stack, boolean simulate) {
		return supply(stack.getItem(), stack.getTag(), stack.getCount(), simulate);
	}
	
	FractionView accept(BulkItem item, FractionView volume, boolean simulate);

	FractionView supply(BulkItem item, FractionView volume, boolean simulate);
}
