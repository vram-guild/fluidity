package grondag.fluidity.api.article;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

public interface ArticleProvider<T> {
	SimpleRegistry<ArticleProvider<?>> REGISTRY = Registry.REGISTRIES.add(new Identifier("fluidity:article_provider"), 
            new SimpleRegistry<ArticleProvider<?>>());
	
	void writeBuffer(T data, PacketByteBuf buf);

	void writeTag(T data, CompoundTag tag);

	T fromTag(CompoundTag tag);

	T fromBuffer(PacketByteBuf buf);

	static <V> ArticleProvider<V> forArticle(V article) {
		return null;
	}
}
