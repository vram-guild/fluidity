package grondag.fluidity.remove;
//package grondag.fluidity.base.article;
//
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.CompoundTag;
//
//import grondag.fluidity.api.item.CommonItem;
//
//public class CommonStoredArticle extends DiscreteStoredArticle{
//	public long count;
//
//	public CommonStoredArticle() {
//		item = CommonItem.NOTHING;
//	}
//
//	public CommonStoredArticle(ItemStack stack, int handle) {
//		prepare(stack, handle);
//	}
//
//	public CommonStoredArticle(CommonItem item, final long count, final int handle) {
//		prepare(item, count, handle);
//	}
//
//	public CommonStoredArticle prepare(ItemStack stack, int handle) {
//		return prepare(CommonItem.of(stack), stack.getCount(), handle);
//	}
//
//	public CommonStoredArticle prepare(CommonItem item, long count, int handle) {
//		this.item = item == null ? CommonItem.NOTHING : item;
//		this.handle = handle;
//		this.count = count;
//		return this;
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public CommonItem item() {
//		return super.item();
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return count == 0 || item == CommonItem.NOTHING;
//	}
//
//	public static CommonStoredArticle of(ItemStack stack) {
//		return new CommonStoredArticle().prepare(stack, 0);
//	}
//
//	public static CommonStoredArticle of(CommonItem item, long count, int handle) {
//		return new CommonStoredArticle(item, count, handle);
//	}
//
//	@Override
//	public CompoundTag toTag() {
//		final CompoundTag result = new CompoundTag();
//		item.writeTag(result, "item");
//		result.putLong("count", count);
//		return result;
//	}
//
//	@Override
//	public void readTag(CompoundTag tag) {
//		item = CommonItem.fromTag(tag, "item");
//		count = tag.getLong("count");
//	}
//}