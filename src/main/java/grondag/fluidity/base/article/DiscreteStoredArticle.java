package grondag.fluidity.base.article;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.item.Article;
import grondag.fluidity.api.storage.Storage;

public class DiscreteStoredArticle extends AbstractStoredArticle {
	public long count;

	public DiscreteStoredArticle() {
		item = Article.NOTHING;
	}

	public DiscreteStoredArticle(Article item, final long count, final int handle) {
		prepare(item, count, handle);
	}

	public DiscreteStoredArticle prepare(Article item, long count, int handle) {
		this.item = item == null ? Article.NOTHING : item;
		this.handle = handle;
		this.count = count;
		return this;
	}

	public DiscreteStoredArticle prepare(ItemStack stack, int handle) {
		return prepare(Article.of(stack), stack.getCount(), handle);
	}

	@Override
	public boolean isEmpty() {
		return count == 0 || item == Article.NOTHING;
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
	public void addStore(Storage store) {
		// TODO move to parent

	}

	public static DiscreteStoredArticle of(Article item, long count, int handle) {
		return new DiscreteStoredArticle(item, count, handle);
	}

	public static DiscreteStoredArticle of(ItemStack stack) {
		return of(stack, stack.getCount(), 0);
	}

	public static DiscreteStoredArticle of(ItemStack item, long count, int handle) {
		return of(Article.of(item), count, handle);
	}

	public CompoundTag toTag() {
		final CompoundTag result = new CompoundTag();
		item.writeTag(result, "item");
		result.putLong("count", count);
		return result;
	}

	public void readTag(CompoundTag tag) {
		item = Article.fromTag(tag, "item");
		count = tag.getLong("count");
	}

	@Override
	public FractionView volume() {
		// TODO Auto-generated method stub
		return null;
	}
}