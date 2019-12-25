package grondag.fluidity.api.article;

import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;

import grondag.fluidity.impl.ArticleTypeImpl;

public interface ArticleType<T> {
	T cast(Object from);

	boolean isFluid();

	boolean isItem();

	boolean isBulk();

	Tag toTag();

	void toPacket(PacketByteBuf buf);

	Function<T, Tag> resourceTagWriter();

	Function<Tag, T> resourceTagReader();

	BiConsumer<T, PacketByteBuf> resourcePacketWriter();

	Function<PacketByteBuf, T> resourcePacketReader();

	static <T> ArticleType<T> fromTag(Tag tag) {
		return ArticleTypeImpl.fromTag(tag);
	}

	static <T> ArticleType<T> fromPacket(PacketByteBuf buf) {
		return ArticleTypeImpl.fromPacket(buf);
	}

	ArticleType<Item> ITEM = ArticleTypeImpl.ITEM;
	ArticleType<Fluid> FLUID = ArticleTypeImpl.FLUID;
	ArticleType<Void> NOTHING = ArticleTypeImpl.NOTHING;

	static <V> Builder<V> builder(Class<V> clazz) {
		return ArticleTypeImpl.builder(clazz);
	}

	interface Builder<U> {
		Builder<U> bulk(boolean isBulk);

		Builder<U> resourceTagWriter(Function<U, Tag> tagWriter);

		Builder<U> resourceTagReader(Function<Tag, U> tagReader);

		Builder<U> resourcePacketWriter(BiConsumer<U, PacketByteBuf> packetWriter);

		Builder<U> resourcePacketReader(Function<PacketByteBuf, U> packetReader);

		ArticleType<U> build();
	}
}
