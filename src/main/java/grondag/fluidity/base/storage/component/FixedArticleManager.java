package grondag.fluidity.base.storage.component;

import java.lang.reflect.Array;
import java.util.function.Supplier;

import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.base.article.AbstractArticle;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FixedArticleManager<K extends StorageItem, V extends AbstractArticle> extends AbstractArticleManager<K, V> {
	protected V[] articles;
	protected final int handleCount;

	public FixedArticleManager(int handleCount, Supplier<V> articleFactory) {
		super(articleFactory);
		this.handleCount = handleCount;
		final V[] handles = (V[]) Array.newInstance(articleFactory.get().getClass(), handleCount);

		for(int i = 0; i < handleCount; i++) {
			final V a = articleFactory.get();
			a.handle = i;
			handles[i] = a;
		}

		this.articles = handles;
	}

	@Override
	public V findOrCreateArticle(K key) {
		int firstUnused = -1;

		for(int i = 0; i < handleCount; i++) {
			final V candidate = articles[i];

			if(candidate.item.equals(key)) {
				return candidate;
			} else if (firstUnused == -1 && candidate.isEmpty()) {
				firstUnused = i;
			}
		}

		if(firstUnused > -1) {
			final V result = articles[firstUnused];
			result.item = key;
			return result;
		} else {
			return null;
		}
	}

	@Override
	public void compact() {
		// NOOP
	}

	@Override
	public int handleCount() {
		return handleCount;
	}

	@Override
	public V get(int handle) {
		return handle >= 0 && handle < handleCount ? articles[handle] : null;
	}

	@Override
	public V get(K key) {
		for(int i = 0; i < handleCount; i++) {
			final V candidate = articles[i];

			if(candidate.item.equals(key)) {
				return candidate;
			}
		}

		return null;
	}

	@Override
	public void clear() {
		for(int i = 0; i < handleCount; i++) {
			articles[i].zero();
		}
	}
}
