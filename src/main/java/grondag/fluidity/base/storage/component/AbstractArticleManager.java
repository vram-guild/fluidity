package grondag.fluidity.base.storage.component;

import java.util.function.Supplier;

import grondag.fluidity.api.item.Article;
import grondag.fluidity.base.article.AbstractStoredArticle;

@SuppressWarnings("rawtypes")
public abstract class AbstractArticleManager<K extends Article, V extends AbstractStoredArticle> implements ArticleManager<K, V> {
	protected final Supplier<V> articleFactory;

	protected AbstractArticleManager(Supplier<V> articleFactory) {
		this.articleFactory = articleFactory;
	}
}
