package grondag.fluidity.base.article;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.Storage;

public class DiscreteStoredArticle extends AbstractStoredArticle {
	public long count;

	public DiscreteStoredArticle() {
		article = Article.NOTHING;
	}

	public DiscreteStoredArticle(Article article, final long count, final int handle) {
		prepare(article, count, handle);
	}

	public DiscreteStoredArticle prepare(Article article, long count, int handle) {
		this.article = article == null ? Article.NOTHING : article;
		this.handle = handle;
		this.count = count;
		return this;
	}

	public DiscreteStoredArticle prepare(ItemStack stack, int handle) {
		return prepare(Article.of(stack), stack.getCount(), handle);
	}

	@Override
	public boolean isEmpty() {
		return count == 0 || article == Article.NOTHING;
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

	public static DiscreteStoredArticle of(Article article, long count, int handle) {
		return new DiscreteStoredArticle(article, count, handle);
	}

	public static DiscreteStoredArticle of(ItemStack stack) {
		return of(stack, stack.getCount(), 0);
	}

	public static DiscreteStoredArticle of(ItemStack item, long count, int handle) {
		return of(Article.of(item), count, handle);
	}

	public CompoundTag toTag() {
		final CompoundTag result = new CompoundTag();
		result.put("art", article.toTag());
		result.putLong("count", count);
		return result;
	}

	public void readTag(CompoundTag tag) {
		article = Article.fromTag(tag.get("art"));
		count = tag.getLong("count");
	}

	@Override
	public FractionView volume() {
		// TODO Auto-generated method stub
		return null;
	}
}