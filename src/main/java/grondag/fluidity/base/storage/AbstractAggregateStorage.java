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

import java.lang.reflect.Array;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.util.math.MathHelper;

import grondag.fluidity.api.article.ArticleView;
import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.Transactor;
import grondag.fluidity.base.article.AbstractArticle;

@SuppressWarnings({"rawtypes", "unchecked"})
@API(status = Status.EXPERIMENTAL)
public abstract class AbstractAggregateStorage<A extends ArticleView<I>, L extends StorageListener<L>, I extends StorageItem, K extends AbstractArticle, S extends Storage<A, L, I>> extends AbstractStorage<A, L, I> {
	protected final Consumer<TransactionContext> rollbackHandler = this::handleRollback;
	protected final Object2ObjectOpenHashMap<I, K> articles = new Object2ObjectOpenHashMap<>();
	protected final ObjectOpenHashSet<S> stores = new ObjectOpenHashSet<>();

	protected Consumer<Transactor> enlister = t -> {};
	protected int nextUnusedSlot = 0;
	protected int emptySlotCount = 0;
	protected boolean itMe = false;
	protected K[] slots;

	public AbstractAggregateStorage(int startingSlotCount) {
		startingSlotCount = MathHelper.smallestEncompassingPowerOfTwo(startingSlotCount);
		final K[] slots = (K[]) Array.newInstance(newArticle().getClass(), startingSlotCount);

		for(int i = 0; i < startingSlotCount; i++) {
			final K a = newArticle();
			a.slot = i;
			slots[i] = a;
		}

		this.slots = slots;
	}

	protected abstract K newArticle();

	protected K findOrCreateArticle(I key) {
		K candidate = articles.get(key);

		if(candidate == null) {
			candidate = getEmptyArticle();
			candidate.item = key;
		}

		return candidate;
	}

	protected abstract L listener();

	public void addStore(S store) {
		if(stores.add(store)) {
			store.forEach(Storage.NOT_EMPTY, a -> {
				findOrCreateArticle(a.item()).addStore(store);
				return true;
			});
		}

		store.startListening(listener());
	}

	public AbstractAggregateStorage() {
		this(32);
	}

	@Override
	public int slotCount() {
		return nextUnusedSlot;
	}

	@Override
	public boolean isEmpty() {
		return emptySlotCount == nextUnusedSlot;
	}

	@Override
	public boolean hasDynamicSlots() {
		return true;
	}

	protected void handleRollback(TransactionContext context) {
		enlister = context.getState();
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		context.setState(enlister);
		enlister = context.enlister();
		return rollbackHandler;
	}

	protected K getEmptyArticle() {
		return slots[getEmptySlot()];
	}

	protected int getEmptySlot() {
		// fill empties first
		if(emptySlotCount > 0) {
			for(int i = 0; i < nextUnusedSlot; i++) {
				if(slots[i].isEmpty()) {
					return i;
				}
			}
		}

		// fill unused slot capacity
		final int slotCount = slots.length;

		if(nextUnusedSlot < slotCount) {
			return ++nextUnusedSlot;
		}

		// add slot capacity
		final int newCount = slotCount * 2;
		final K[] newSlots = (K[]) Array.newInstance(newArticle().getClass(), newCount);
		System.arraycopy(slots, 0, newSlots, 0, slotCount);

		for(int i = slotCount; i < newCount; i++) {
			final K a = newArticle();
			a.slot = i;
			newSlots[i] = a;
		}

		slots = newSlots;

		return ++nextUnusedSlot;
	}

	protected void compactSlots() {
		// no renumbering while listeners are active
		if(emptySlotCount == 0 || !listeners.isEmpty()) {
			return;
		}

		for (int i = nextUnusedSlot -  1; i > 0 && emptySlotCount > 0; --i) {
			if(slots[i].isEmpty()) {
				final int target = nextUnusedSlot - 1;

				if (i == target) {
					// already at end
					--nextUnusedSlot;
				} else {
					// swap with last non-empty and renumber
					final K swap = slots[i];
					swap.slot = target;

					slots[i] = slots[target];
					slots[i].slot = i;

					slots[target] = swap;
				}

				--emptySlotCount;
			}
		}
	}

	@Override
	public A view(int slot) {
		return (A) slots[slot];
	}
}
