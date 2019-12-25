package grondag.fluidity.base.article;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.api.storage.DiscreteStorage;

public class DiscreteArticle extends AbstractArticle<DiscreteStorage> implements DiscreteArticleView {
	public long count;

	public DiscreteArticle() {
		item = StorageItem.NOTHING;
	}

	public DiscreteArticle(StorageItem item, final long count, final int handle) {
		prepare(item, count, handle);
	}

	public DiscreteArticle prepare(StorageItem item, long count, int handle) {
		this.item = item == null ? StorageItem.NOTHING : item;
		this.handle = handle;
		this.count = count;
		return this;
	}

	@Override
	public boolean isEmpty() {
		return count == 0 || item == StorageItem.NOTHING;
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
	public void addStore(DiscreteStorage store) {
		// TODO Auto-generated method stub

	}

	public static DiscreteArticle of(StorageItem item, long count, int handle) {
		return new DiscreteArticle(item, count, handle);
	}

	public CompoundTag toTag() {
		final CompoundTag result = new CompoundTag();
		item.writeTag(result, "item");
		result.putLong("count", count);
		return result;
	}

	public void readTag(CompoundTag tag) {
		item = StorageItem.fromTag(tag, "item");
		count = tag.getLong("count");
	}
}