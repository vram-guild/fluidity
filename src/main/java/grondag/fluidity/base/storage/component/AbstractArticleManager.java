package grondag.fluidity.base.storage.component;

import java.util.function.Supplier;

import grondag.fluidity.base.article.AbstractStoredArticle;

public abstract class AbstractArticleManager<V extends AbstractStoredArticle> implements ArticleManager<V> {
	protected final Supplier<V> articleFactory;

	protected AbstractArticleManager(Supplier<V> articleFactory) {
		this.articleFactory = articleFactory;
	}
}
