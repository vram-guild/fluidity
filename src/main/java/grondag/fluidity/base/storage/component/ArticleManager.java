package grondag.fluidity.base.storage.component;

import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.base.article.AbstractArticle;

@SuppressWarnings("rawtypes")
public interface ArticleManager<K extends StorageItem, V extends AbstractArticle> {
	V findOrCreateArticle(K key);

	/** Do not call while listeners are active */
	void compact();

	int handleCount();

	V get(int handle);

	V get(K key);

	void clear();
}
