package grondag.fluidity.api.item;

import grondag.fluidity.api.article.ArticleProvider;
import grondag.fluidity.api.discrete.DiscreteArticleView;
import net.minecraft.item.ItemStack;

public interface ItemArticleView extends DiscreteArticleView<ItemStack, ItemArticleView> {
	@Override
	default ArticleProvider<ItemStack> provider() {
		return ItemArticleProvider.INSTANCE;
	}
	
	ItemArticleView EMPTY = new ItemArticleView() {
		@Override
		public ItemStack article() {
			return ItemStack.EMPTY;
		}
	};
}
