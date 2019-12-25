package grondag.fluidity.base.article;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.item.Article;
import grondag.fluidity.api.storage.Storage;

public abstract class AbstractStoredArticle<S extends Storage<?, ?>> implements StoredArticleView {
	// TODO: encapsulate
	public Article item;
	public int handle;
	public final ObjectArraySet<S> stores = new ObjectArraySet<>();

	@SuppressWarnings("unchecked")
	@Override
	public Article item() {
		return item;
	}

	@Override
	public int handle() {
		return handle;
	}

	public abstract void addStore(S store);

	public abstract void zero();

}