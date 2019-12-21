package grondag.fluidity.base.article;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import grondag.fluidity.api.article.ArticleView;
import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.api.storage.Storage;

public abstract class AbstractArticle<S extends Storage<?, ?, I>, I extends StorageItem> implements ArticleView<I> {
	public I item;
	public int slot;
	public final ObjectArraySet<S> stores = new ObjectArraySet<>();

	@Override
	public I item() {
		return item;
	}

	@Override
	public int slot() {
		return slot;
	}

	public abstract void addStore(S store);
}