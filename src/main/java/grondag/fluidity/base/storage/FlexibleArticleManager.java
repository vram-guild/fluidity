package grondag.fluidity.base.storage;

import java.lang.reflect.Array;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.util.math.MathHelper;

import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.base.article.AbstractArticle;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FlexibleArticleManager<K extends StorageItem, V extends AbstractArticle> {
	protected final Object2ObjectOpenHashMap<K, V> articles = new Object2ObjectOpenHashMap<>();

	protected int nextUnusedHandle = 0;
	protected int emptyHandleCount = 0;
	protected V[] handles;
	protected final Supplier<V> articleFactory;

	FlexibleArticleManager(int startingHandleCount, Supplier<V> articleFactory) {
		this.articleFactory = articleFactory;

		startingHandleCount = MathHelper.smallestEncompassingPowerOfTwo(startingHandleCount);
		final V[] handles = (V[]) Array.newInstance(articleFactory.get().getClass(), startingHandleCount);

		for(int i = 0; i < startingHandleCount; i++) {
			final V a = articleFactory.get();
			a.handle = i;
			handles[i] = a;
		}

		this.handles = handles;
		emptyHandleCount = startingHandleCount;
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
		return handles[getEmptyHandle()];
	}

	protected int getEmptyHandle() {
		// fill empties first
		if(emptyHandleCount > 0) {
			for(int i = 0; i < nextUnusedHandle; i++) {
				if(handles[i].isEmpty()) {
					return i;
				}
			}
		}

		// fill unused handle capacity
		final int handleCount = handles.length;

		if(nextUnusedHandle < handleCount) {
			return ++nextUnusedHandle;
		}

		// add slot capacity
		final int newCount = handleCount * 2;
		final V[] newHandles = (V[]) Array.newInstance(articleFactory.get().getClass(), newCount);
		System.arraycopy(handles, 0, newHandles, 0, handleCount);

		for(int i = handleCount; i < newCount; i++) {
			final V a = articleFactory.get();
			a.handle = i;
			newHandles[i] = a;
		}

		handles = newHandles;

		return ++nextUnusedHandle;
	}

	/** Do not call while listeners are active */
	protected void compact() {
		if(emptyHandleCount == 0) {
			return;
		}

		for (int i = nextUnusedHandle -  1; i > 0 && emptyHandleCount > 0; --i) {
			if(handles[i].isEmpty()) {
				final int target = nextUnusedHandle - 1;

				if (i == target) {
					// already at end
					--nextUnusedHandle;
				} else {
					// swap with last non-empty and renumber
					final V swap = handles[i];
					swap.handle = target;

					handles[i] = handles[target];
					handles[i].handle = i;

					handles[target] = swap;
				}

				--emptyHandleCount;
			}
		}
	}

	public int handleCount() {
		return nextUnusedHandle;
	}

	public boolean isEmpty() {
		return emptyHandleCount == nextUnusedHandle;
	}

	public V get(int handle) {
		return handle >= 0 && handle < nextUnusedHandle ? handles[handle] : null;
	}

	public V get(K key) {
		return articles.get(key);
	}
}
