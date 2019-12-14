package grondag.fluidity.base.article;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import grondag.fluidity.api.article.ArticleView;
import grondag.fluidity.api.storage.Storage;

public abstract class AbstractArticle<S extends Storage> implements ArticleView {
	public int slot;
	public final ObjectArraySet<S> stores = new ObjectArraySet<>();

	@Override
	public int slot() {
		return slot;
	}
}