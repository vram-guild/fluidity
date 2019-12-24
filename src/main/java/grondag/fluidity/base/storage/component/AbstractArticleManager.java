package grondag.fluidity.base.storage.component;

import java.util.function.Supplier;

import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.base.article.AbstractArticle;

@SuppressWarnings("rawtypes")
public abstract class AbstractArticleManager<K extends StorageItem, V extends AbstractArticle> implements ArticleManager<K, V> {
	protected final Supplier<V> articleFactory;

	protected AbstractArticleManager(Supplier<V> articleFactory) {
		this.articleFactory = articleFactory;
	}
}
