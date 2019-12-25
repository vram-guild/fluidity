package grondag.fluidity.base.article;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.item.CommonItem;

public class CommonArticle extends DiscreteArticle{
	public long count;

	public CommonArticle() {
		item = CommonItem.NOTHING;
	}

	public CommonArticle(ItemStack stack, int handle) {
		prepare(stack, handle);
	}

	public CommonArticle(CommonItem item, final long count, final int handle) {
		prepare(item, count, handle);
	}

	public CommonArticle prepare(ItemStack stack, int handle) {
		return prepare(CommonItem.of(stack), stack.getCount(), handle);
	}

	public CommonArticle prepare(CommonItem item, long count, int handle) {
		this.item = item == null ? CommonItem.NOTHING : item;
		this.handle = handle;
		this.count = count;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CommonItem item() {
		return super.item();
	}

	@Override
	public boolean isEmpty() {
		return count == 0 || item == CommonItem.NOTHING;
	}

	public static CommonArticle of(ItemStack stack) {
		return new CommonArticle().prepare(stack, 0);
	}

	public static CommonArticle of(CommonItem item, long count, int handle) {
		return new CommonArticle(item, count, handle);
	}

	@Override
	public CompoundTag toTag() {
		final CompoundTag result = new CompoundTag();
		item.writeTag(result, "item");
		result.putLong("count", count);
		return result;
	}

	@Override
	public void readTag(CompoundTag tag) {
		item = CommonItem.fromTag(tag, "item");
		count = tag.getLong("count");
	}
}