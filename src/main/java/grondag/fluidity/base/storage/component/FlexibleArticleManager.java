package grondag.fluidity.base.storage.component;

import java.lang.reflect.Array;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.util.math.MathHelper;

import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.base.article.AbstractArticle;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FlexibleArticleManager<K extends StorageItem, V extends AbstractArticle> implements ArticleManager<K, V>{
	protected final Object2ObjectOpenHashMap<K, V> articles = new Object2ObjectOpenHashMap<>();

	protected int nextUnusedHandle = 0;
	protected V[] handles;
	protected final Supplier<V> articleFactory;

	public FlexibleArticleManager(int startingHandleCount, Supplier<V> articleFactory) {
		this.articleFactory = articleFactory;

		startingHandleCount = MathHelper.smallestEncompassingPowerOfTwo(startingHandleCount);
		final V[] handles = (V[]) Array.newInstance(articleFactory.get().getClass(), startingHandleCount);

		for(int i = 0; i < startingHandleCount; i++) {
			final V a = articleFactory.get();
			a.handle = i;
			handles[i] = a;
		}

		this.handles = handles;
	}

	@Override
	public V findOrCreateArticle(K key) {
		V candidate = articles.get(key);

		if(candidate == null) {
			candidate = getEmptyArticle();
			candidate.item = key;
			articles.put(key, candidate);
		}

		return candidate;
	}

	protected V getEmptyArticle() {
		final int index = getEmptyHandle();
		return handles[index];
	}

	protected int getEmptyHandle() {
		// fill unused handle capacity
		final int handleCount = handles.length;
		final int result = nextUnusedHandle++;

		if(result < handleCount) {
			return result;
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

		return result;
	}

	@Override
	public void compact() {
		for (int i = nextUnusedHandle - 1; i > 0; --i) {
			final V a = handles[i];

			if(a.isEmpty()) {
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

				articles.remove(a.item);
			}
		}
	}

	@Override
	public int handleCount() {
		return nextUnusedHandle;
	}

	@Override
	public V get(int handle) {
		return handle >= 0 && handle < handles.length ? handles[handle] : null;
	}

	@Override
	public V get(K key) {
		return articles.get(key);
	}

	@Override
	public void clear() {
		articles.clear();
		nextUnusedHandle = 0;
	}
}
