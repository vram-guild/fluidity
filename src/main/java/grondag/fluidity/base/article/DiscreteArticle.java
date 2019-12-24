package grondag.fluidity.base.article;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorage;

public class DiscreteArticle extends AbstractArticle<DiscreteStorage, DiscreteItem> implements DiscreteArticleView {
	public long count;

	public DiscreteArticle() {
		item = DiscreteItem.NOTHING;
	}

	public DiscreteArticle(ItemStack stack, int handle) {
		prepare(stack, handle);
	}

	public DiscreteArticle(DiscreteItem item, final long count, final int handle) {
		prepare(item, count, handle);
	}

	public DiscreteArticle prepare(ItemStack stack, int handle) {
		return prepare(DiscreteItem.of(stack), stack.getCount(), handle);
	}

	public DiscreteArticle prepare(DiscreteItem item, long count, int handle) {
		this.item = item == null ? DiscreteItem.NOTHING : item;
		this.handle = handle;
		this.count = count;
		return this;
	}

	@Override
	public DiscreteItem item() {
		return super.item();
	}

	@Override
	public boolean isEmpty() {
		return count == 0 || item == DiscreteItem.NOTHING;
	}

	@Override
	public long count() {
		return count;
	}

	@Override
	public void zero() {
		count = 0;
	}

	@Override
	public boolean isItem() {
		return true;
	}

	@Override
	public DiscreteArticleView toItemView() {
		return this;
	}

	@Override
	public void addStore(DiscreteStorage store) {
		// TODO Auto-generated method stub

	}

	public static DiscreteArticle of(ItemStack stack) {
		return new  DiscreteArticle().prepare(stack, 0);
	}

	public static DiscreteArticle of(DiscreteItem item, long count, int handle) {
		return new DiscreteArticle(item, count, handle);
	}

	public CompoundTag toTag() {
		final CompoundTag result = new CompoundTag();
		item.writeTag(result, "item");
		result.putLong("count", count);
		return result;
	}

	public void readTag(CompoundTag tag) {
		item = DiscreteItem.fromTag(tag, "item");
		count = tag.getLong("count");
	}
}