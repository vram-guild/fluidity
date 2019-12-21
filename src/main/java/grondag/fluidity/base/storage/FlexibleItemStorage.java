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

@API(status = Status.EXPERIMENTAL)
public class FlexibleItemStorage extends AbstractLazyRollbackStorage<DiscreteArticleView,  DiscreteStorageListener, DiscreteItem> implements InventoryStorage {
	protected Predicate<DiscreteItem> filter = Predicates.alwaysTrue();
	protected final FlexibleSlotManager<DiscreteItem, DiscreteArticle> slots;
	protected final DiscreteItemNotifier notifier;

	public FlexibleItemStorage(int startingSlotCount, long capacity, @Nullable Predicate<DiscreteItem> filter) {
		slots = new FlexibleSlotManager<>(startingSlotCount, DiscreteArticle::new);
		notifier = new DiscreteItemNotifier(capacity, this, slots);
		filter(filter);
	}

	public FlexibleItemStorage(int capacity) {
		this(32, capacity, null);
	}

	public void filter(Predicate<DiscreteItem> filter) {
		this.filter = filter == null ? Predicates.alwaysTrue() : filter;
	}

	@Override
	public long accept(DiscreteItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item.isEmpty()) {
			return 0;
		}

		final long result = Math.min(count, notifier.capacity - notifier.count);

		if(result > 0 && !simulate) {
			final DiscreteArticle article = slots.findOrCreateArticle(item);
			article.count += result;
			notifier.notifyAccept(article, result);
		}

		return result;
	}

	@Override
	public long supply(DiscreteItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to supply null item");

		if (item.isEmpty() || slots.isEmpty()) {
			return 0;
		}

		final DiscreteArticle article = slots.get(item);

		if(article == null || article.isEmpty()) {
			return 0;
		}

		final long result = Math.min(count, article.count);

		if(result > 0 && !simulate) {
			notifier.notifySupply(article, result);
			article.count -= result;
		}

		return result;
	}

	@Override
	public long count() {
		return notifier.count;
	}

	@Override
	public long capacity() {
		return notifier.capacity;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public int slotCount() {
		return slots.slotCount();
	}

	@Override
	public DiscreteArticleView view(int slot) {
		return slots.get(slot);
	}

	@Override
	public ItemStack getInvStack(int slot) {
		final DiscreteArticle a = slots.get(slot);
		return a == null || a.isEmpty() ? ItemStack.EMPTY : a.toStack();
	}

	@Override
	public ItemStack takeInvStack(int slot, int count) {
		final DiscreteArticle a = slots.get(slot);

		if(a == null || a.isEmpty()) {
			return ItemStack.EMPTY;
		}

		final DiscreteItem item = a.item;
		count = Math.min(count, item.getItem().getMaxCount());
		final int result = (int) supply(item, count, false);

		if(result == 0) {
			return ItemStack.EMPTY;
		} else {
			markDirty();
			return item.toStack(result);
		}
	}

	@Override
	public ItemStack removeInvStack(int slot) {
		final DiscreteArticle a = slots.get(slot);

		if(a == null || a.isEmpty()) {
			return ItemStack.EMPTY;
		}

		final DiscreteItem item = a.item;
		final int result = (int) supply(item, item.getItem().getMaxCount(), false);

		if(result == 0) {
			return ItemStack.EMPTY;
		} else {
			return item.toStack(result);
		}
	}

	@Override
	public void setInvStack(int slot, ItemStack newStack) {
		Preconditions.checkNotNull(newStack, "ItemStack must be non-null");

		if(slot < 0 || slot >= slots.slotCount()) {
			return;
		}

		final DiscreteArticle a = slots.get(slot);

		if(a == null || a.isEmpty()) {

		}

	}

	@Override
	protected Object createRollbackState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void applyRollbackState(Object state) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void sendFirstListenerUpdate(DiscreteStorageListener listener) {
		notifier.sendFirstListenerUpdate(listener);
	}
}
