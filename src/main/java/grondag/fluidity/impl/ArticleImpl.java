//package grondag.fluidity.impl;
//
//import grondag.fluidity.api.storage.Article;
//import grondag.fluidity.api.storage.ArticleProvider;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.util.Identifier;
//import net.minecraft.util.PacketByteBuf;
//
//public final class ArticleImpl<T> implements Article {
//	public static Article EMPTY = null;
//			
//    private final ArticleProvider<T> provider;
//    private final T providerArticle;
//    
//    ArticleImpl(ArticleProvider<T> provider, T providerArticle) {
//    	this.provider = provider;
//    	this.providerArticle = providerArticle;
//    }
//    
//    @SuppressWarnings("unchecked")
//	ArticleImpl(CompoundTag tag) {
//    	provider = (ArticleProvider<T>) ArticleProvider.REGISTRY.get(new Identifier(tag.getString("provider")));
//    	providerArticle = provider.fromTag(tag);
//    }
//    
//    @SuppressWarnings("unchecked")
//    ArticleImpl(PacketByteBuf buf) {
//    	provider = (ArticleProvider<T>) ArticleProvider.REGISTRY.get(buf.readVarInt());
//    	providerArticle = provider.fromBuffer(buf);
//    }
//
//    @SuppressWarnings("unchecked")
//	@Override
//	public final ArticleProvider<T> provider() {
//        return (ArticleProvider<T>) provider;
//    }
//    
//	@Override
//	public final CompoundTag tag() {
//    	return provider.tag(providerArticle);
//    }
//    
//    /**
//     * Serializes content to buffer. Recreate instance with
//     * {@link #fromBuffer(PacketByteBuf)}. Suitable only for network traffic -
//     * assumes raw fluid ID's match on both sides.
//     * 
//     * @param buf
//     */
//    @Override
//	public void writeBuffer(PacketByteBuf buf) {
//        buf.writeVarInt(ArticleProvider.REGISTRY.getRawId(provider));
//        provider.writeBuffer(providerArticle, buf);
//    }
//
//	@Override
//	public void writeTag(CompoundTag tag) {
//        tag.putString("provider", ArticleProvider.REGISTRY.getId(provider).toString());
//        provider.writeTag(providerArticle, tag);
//    }
//    
//    @SuppressWarnings("rawtypes")
//	public static Article fromTag(CompoundTag tag) {
//    	return new ArticleImpl(tag);
//    }
//    
//    @SuppressWarnings("rawtypes")
//    public static Article fromBuffer(PacketByteBuf buf) {
//    	return new ArticleImpl(buf);
//    }
//}
