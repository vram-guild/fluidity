package grondag.fluidity.base.article;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.ItemArticleView;
import grondag.fluidity.api.storage.ItemStorage;
import grondag.fluidity.base.item.ItemInstance;

public class ItemArticle extends AbstractArticle<ItemStorage> implements ItemArticleView {
	ItemInstance item;
	long count;

	@Override
	public ItemStack toStack() {
		return item.toStack(count);
	}

	@Override
	public boolean isEmpty() {
		return count > 0;
	}

	@Override
	public long count() {
		return count;
	}

	@Override
	public boolean hasTag() {
		return item.getTag() != null;
	}

	@Override
	public Item item() {
		return item.getItem();
	}

	@Override
	public CompoundTag tag() {
		return item.getTag();
	}

	@Override
	public boolean isItem() {
		return true;
	}

	@Override
	public ItemArticleView toItemView() {
		return this;
	}
}