package grondag.fluidity.api.relics;
//package grondag.fluidity.api.article;
//
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.util.Identifier;
//import net.minecraft.util.PacketByteBuf;
//import net.minecraft.util.registry.Registry;
//import net.minecraft.util.registry.SimpleRegistry;
//
//public interface ArticleProvider<T> {
//	SimpleRegistry<ArticleProvider<?>> REGISTRY = Registry.REGISTRIES.add(new Identifier("fluidity:article_provider"), 
//            new SimpleRegistry<ArticleProvider<?>>());
//	
//	void toBuffer(T data, PacketByteBuf buf);
//
//	void toTag(T data, CompoundTag tag);
//
//	T fromTag(CompoundTag tag);
//
//	T fromBuffer(PacketByteBuf buf);
//	
//	boolean areEqual(T article1, T article2);
//	
//	int hashCode(T article);
//
//	static <V> ArticleProvider<V> forArticle(V article) {
//		return null;
//	}
//}
