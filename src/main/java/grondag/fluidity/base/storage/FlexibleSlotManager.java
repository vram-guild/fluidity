package grondag.fluidity.base.storage;

import java.lang.reflect.Array;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.util.math.MathHelper;

import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.base.article.AbstractArticle;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FlexibleSlotManager<K extends StorageItem, V extends AbstractArticle> {
	protected final Object2ObjectOpenHashMap<K, V> articles = new Object2ObjectOpenHashMap<>();

	protected int nextUnusedSlot = 0;
	protected int emptySlotCount = 0;
	protected V[] slots;
	protected final Supplier<V> articleFactory;

	FlexibleSlotManager(int startingSlotCount, Supplier<V> articleFactory) {
		this.articleFactory = articleFactory;

		startingSlotCount = MathHelper.smallestEncompassingPowerOfTwo(startingSlotCount);
		final V[] slots = (V[]) Array.newInstance(articleFactory.get().getClass(), startingSlotCount);

		for(int i = 0; i < startingSlotCount; i++) {
			final V a = articleFactory.get();
			a.slot = i;
			slots[i] = a;
		}

		this.slots = slots;
		emptySlotCount = startingSlotCount;
	}

	public V findOrCreateArticle(K key) {
		V candidate = articles.get(key);

		if(candidate == null) {
			candidate = getEmptyArticle();
			candidate.item = key;
		}

		return candidate;
	}

	protected V getEmptyArticle() {
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
		final V[] newSlots = (V[]) Array.newInstance(articleFactory.get().getClass(), newCount);
		System.arraycopy(slots, 0, newSlots, 0, slotCount);

		for(int i = slotCount; i < newCount; i++) {
			final V a = articleFactory.get();
			a.slot = i;
			newSlots[i] = a;
		}

		slots = newSlots;

		return ++nextUnusedSlot;
	}

	/** Do not call while listeners are active */
	protected void compactSlots() {
		if(emptySlotCount == 0) {
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
					final V swap = slots[i];
					swap.slot = target;

					slots[i] = slots[target];
					slots[i].slot = i;

					slots[target] = swap;
				}

				--emptySlotCount;
			}
		}
	}

	public int slotCount() {
		return nextUnusedSlot;
	}

	public boolean isEmpty() {
		return emptySlotCount == nextUnusedSlot;
	}

	public V get(int item) {
		return item >= 0 && item < nextUnusedSlot ? slots[item] : null;
	}

	public V get(K key) {
		return articles.get(key);
	}
}
