package grondag.fluidity.base.storage.component;

import grondag.fluidity.api.item.Article;
import grondag.fluidity.base.article.AbstractStoredArticle;

public interface ArticleManager<V extends AbstractStoredArticle> {
	V findOrCreateArticle(Article key);

	/** Do not call while listeners are active */
	void compact();

	int handleCount();

	V get(int handle);

	V get(Article key);

	void clear();
}
