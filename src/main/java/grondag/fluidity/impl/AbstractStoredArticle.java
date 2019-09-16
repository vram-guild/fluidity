package grondag.fluidity.impl;

import grondag.fluidity.api.article.ArticleProvider;
import grondag.fluidity.api.article.StoredArticle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public abstract class AbstractStoredArticle<T> implements StoredArticle<T> {
	private T article;
	
	private ArticleProvider<T> provider;
	
	protected AbstractStoredArticle(T article) {
		setArticle(article);
	}
	
	protected AbstractStoredArticle(PacketByteBuf buf) {
		readBuffer(buf);
	}
	
	protected AbstractStoredArticle(CompoundTag tag) {
		readTag(tag);
	}
	
	protected void setArticle(T article) {
		this.article = article;
		provider = ArticleProvider.forArticle(article);
	}
	
	@Override
	public T article() {
		return article;
	}

	public void writeBuffer(PacketByteBuf buf) {
		buf.writeVarInt(ArticleProvider.REGISTRY.getRawId(provider));
		provider.writeBuffer(article, buf);
	}

	public void writeTag(CompoundTag tag) {
		tag.putString("provider", ArticleProvider.REGISTRY.getId(provider).toString());
		provider.writeTag(article, tag);
	}

	@SuppressWarnings("unchecked")
	public void readBuffer(PacketByteBuf buf) {
		provider = (ArticleProvider<T>) ArticleProvider.REGISTRY.get(buf.readVarInt());
		article = provider.fromBuffer(buf);
	}

	@SuppressWarnings("unchecked")
	public void readTag(CompoundTag tag) {
		provider = (ArticleProvider<T>) ArticleProvider.REGISTRY.get(new Identifier(tag.getString("provider")));
		article = provider.fromTag(tag);
	}
	
	@Override
	public ArticleProvider<T> provider() {
		return null;
	}
}
