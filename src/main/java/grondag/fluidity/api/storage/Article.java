package grondag.fluidity.api.storage;

import grondag.fluidity.impl.ArticleImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public interface Article {

	<T> ArticleProvider<T> provider();

	CompoundTag tag();

	/**
	 * Serializes content to buffer. Recreate instance with
	 * {@link #fromBuffer(PacketByteBuf)}. Suitable only for network traffic -
	 * assumes raw fluid ID's match on both sides.
	 * 
	 * @param buf
	 */
	void writeBuffer(PacketByteBuf buf);

	void writeTag(CompoundTag tag);

	CompoundTag toTag();

	static Article fromTag(CompoundTag tag) {
		return ArticleImpl.fromTag(tag);
	}

	static Article fromBuffer(PacketByteBuf buffer) {
		// TODO Auto-generated method stub
		return null;
	}

}