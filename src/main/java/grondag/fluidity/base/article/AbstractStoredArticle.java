package grondag.fluidity.base.article;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.item.Article;
import grondag.fluidity.api.storage.Storage;

public abstract class AbstractStoredArticle implements StoredArticleView {
	// TODO: encapsulate
	public Article item;
	public int handle;
	public final ObjectArraySet<Storage> stores = new ObjectArraySet<>();

	@Override
	public Article item() {
		return item;
	}

	@Override
	public int handle() {
		return handle;
	}

	public abstract void addStore(Storage store);

	public abstract void zero();

}