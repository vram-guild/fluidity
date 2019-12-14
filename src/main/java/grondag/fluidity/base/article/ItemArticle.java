package grondag.fluidity.base.article;

import grondag.fluidity.api.article.ItemArticleView;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorage;

public class ItemArticle<S extends DiscreteStorage<ItemArticle<S>>> extends AbstractArticle<S, DiscreteItem> implements ItemArticleView {
	long count;

	@Override
	public boolean isEmpty() {
		return count == 0;
	}

	@Override
	public long count() {
		return count;
	}

	@Override
	public boolean isItem() {
		return true;
	}

	@Override
	public ItemArticleView toItemView() {
		return this;
	}

	@Override
	public void addStore(S store) {
		// TODO Auto-generated method stub

	}
}