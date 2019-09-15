package grondag.fluidity.api.storage;

import grondag.fluidity.impl.ArticleImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public class AbstractStoredArticle implements StoredArticle {
	protected Article article = ArticleImpl.EMPTY;
	
	@Override
	public Article article() {
		return article;
	}

	public void writeBuffer(PacketByteBuf buf) {
		article.writeBuffer(buf);
	}

	public void writeTag(CompoundTag tag) {
		article.writeTag(tag);
	}
}
