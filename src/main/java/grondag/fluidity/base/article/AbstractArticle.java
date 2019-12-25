package grondag.fluidity.base.article;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import grondag.fluidity.api.article.ArticleView;
import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.api.storage.Storage;

public abstract class AbstractArticle<S extends Storage<?, ?>> implements ArticleView {
	// TODO: encapsulate
	public StorageItem item;
	public int handle;
	public final ObjectArraySet<S> stores = new ObjectArraySet<>();

	@SuppressWarnings("unchecked")
	@Override
	public <V extends StorageItem> V item() {
		return (V) item;
	}

	@Override
	public int handle() {
		return handle;
	}

	public abstract void addStore(S store);

	public abstract void zero();
}