/*******************************************************************************
 * Copyright 2019 grondag
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
package grondag.fluidity.api.article;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;

import grondag.fluidity.impl.ArticleTypeImpl;

@API(status = Status.EXPERIMENTAL)
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

	Set<ArticleType<?>> SET_OF_ITEMS = ImmutableSet.of(ITEM);
	Set<ArticleType<?>> SET_OF_FLUIDS = ImmutableSet.of(FLUID);

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
