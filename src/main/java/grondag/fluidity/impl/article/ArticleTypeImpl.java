/*******************************************************************************
 * Copyright 2019, 2020 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.fluidity.impl.article;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.article.ArticleTypeRegistry;

@API(status = Status.INTERNAL)
public class ArticleTypeImpl<T> implements ArticleType<T> {
	final Class<T> clazz;
	final boolean isBulk;
	final boolean isFluid;
	final boolean isItem;
	final BiConsumer<T, PacketByteBuf> packetWriter;
	final Function<PacketByteBuf, T> packetReader;
	final Function<Tag, T> tagReader;
	final Function<T, Tag> tagWriter;

	ArticleTypeImpl(BuilderImpl<T> builder) {
		this.clazz = builder.clazz;
		this.isBulk = builder.isBulk;
		this.packetWriter = builder.packetWriter;
		this.packetReader = builder.packetReader;
		this.tagReader = builder.tagReader;
		this.tagWriter = builder.tagWriter;
		this.isFluid = clazz == Fluid.class;
		this.isItem = clazz == Item.class;
	}

	@Override
	public T cast(Object from) {
		return clazz.cast(from);
	}

	@Override
	public boolean isFluid() {
		return isFluid;
	}

	@Override
	public boolean isItem() {
		return isItem;
	}

	@Override
	public boolean isBulk() {
		return isBulk;
	}

	@Override
	public Function<T, Tag> resourceTagWriter() {
		return tagWriter;
	}

	@Override
	public Function<Tag, T> resourceTagReader() {
		return tagReader;
	}

	@Override
	public BiConsumer<T, PacketByteBuf> resourcePacketWriter() {
		return packetWriter;
	}

	@Override
	public Function<PacketByteBuf, T> resourcePacketReader() {
		return packetReader;
	}

	@Override
	public Tag toTag() {
		return StringTag.of(ArticleTypeRegistry.instance().getId(this).toString());
	}

	@Override
	public void toPacket(PacketByteBuf buf) {
		buf.writeVarInt(ArticleTypeRegistryImpl.INSTANCE.getRawId(this));
	}

	private static class BuilderImpl<U> implements Builder<U> {
		private final Class<U> clazz;
		private boolean isBulk = false;
		private BiConsumer<U, PacketByteBuf> packetWriter;
		private Function<PacketByteBuf, U> packetReader;
		private Function<U, Tag> tagWriter;
		private Function<Tag, U> tagReader;

		BuilderImpl(Class<U> clazz) {
			this.clazz = clazz;
		}

		@Override
		public Builder<U> bulk(boolean isBulk) {
			this.isBulk = isBulk;
			return this;
		}

		@Override
		public ArticleType<U> build() {
			Preconditions.checkNotNull(packetWriter, "Packet writer required to build article types.");
			Preconditions.checkNotNull(packetReader, "Packet reader required to build article types.");
			Preconditions.checkNotNull(tagWriter, "Tag writer required to build article types.");
			Preconditions.checkNotNull(tagReader, "Tag reader required to build article types.");
			return new ArticleTypeImpl<>(this);
		}

		@Override
		public Builder<U> resourceTagWriter(Function<U, Tag> tagWriter) {
			this.tagWriter = tagWriter;
			return this;
		}

		@Override
		public Builder<U> resourceTagReader(Function<Tag, U> tagReader) {
			this.tagReader = tagReader;
			return this;
		}

		@Override
		public Builder<U> resourcePacketWriter(BiConsumer<U, PacketByteBuf> packetWriter) {
			this.packetWriter = packetWriter;
			return this;
		}

		@Override
		public Builder<U> resourcePacketReader(Function<PacketByteBuf, U> packetReader) {
			this.packetReader = packetReader;
			return this;
		}
	}

	public static final ArticleType<Item> ITEM = ArticleTypeRegistryImpl.INSTANCE.add("fluidity:item",
			builder(Item.class)
			.bulk(false)
			.resourceTagWriter(r -> StringTag.of(Registry.ITEM.getId(r).toString()))
			.resourceTagReader(t -> Registry.ITEM.get(new Identifier(t.asString())))
			.resourcePacketWriter((r, p) -> p.writeVarInt(Registry.ITEM.getRawId(r)))
			.resourcePacketReader(p -> Registry.ITEM.get(p.readVarInt()))
			.build());

	public static final ArticleType<Fluid> FLUID = ArticleTypeRegistryImpl.INSTANCE.add("fluidity:fluid", builder(Fluid.class)
			.bulk(true)
			.resourceTagWriter(r -> StringTag.of(Registry.FLUID.getId(r).toString()))
			.resourceTagReader(t -> Registry.FLUID.get(new Identifier(t.asString())))
			.resourcePacketWriter((r, p) -> p.writeVarInt(Registry.FLUID.getRawId(r)))
			.resourcePacketReader(p -> Registry.FLUID.get(p.readVarInt()))
			.build());

	public static final ArticleType<Void> NOTHING = ArticleTypeRegistryImpl.INSTANCE.add("fluidity:nothing", builder(Void.class)
			.bulk(false)
			.resourceTagWriter(r -> null)
			.resourceTagReader(t -> null)
			.resourcePacketWriter((r, p) -> {})
			.resourcePacketReader(p -> null)
			.build());

	public static <V> BuilderImpl<V> builder(Class<V> clazz) {
		return new BuilderImpl<>(clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T> ArticleType<T> fromTag(Tag tag) {
		return ArticleTypeRegistryImpl.INSTANCE.get(tag.asString());
	}

	@SuppressWarnings("unchecked")
	public static <T> ArticleType<T> fromPacket(PacketByteBuf buf) {
		return ArticleTypeRegistryImpl.INSTANCE.get(buf.readVarInt());
	}

	public static void init() {
		// force loading
	}
}
