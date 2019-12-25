package grondag.fluidity.base.storage.component;

import grondag.fluidity.api.item.Article;
import grondag.fluidity.base.article.AbstractStoredArticle;

@SuppressWarnings("rawtypes")
public interface ArticleManager<K extends Article, V extends AbstractStoredArticle> {
	V findOrCreateArticle(K key);

	/** Do not call while listeners are active */
	void compact();

	int handleCount();

	V get(int handle);

	V get(K key);

	void clear();
}
