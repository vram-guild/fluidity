/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.api.article;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import grondag.fluidity.impl.article.ArticleTypeImpl;

@Experimental
/**
 * Describes a game resource used as an {@code Article}.
 *
 * @param <T> Class of the game resource
 */
public interface ArticleType<T> {
	/**
	 * Casts an object to the class of the game resource represented by this article type.
	 *
	 * @param from Object to be cast
	 * @return Input parameter cast to the class of the game resource represented by this article type
	 */
	T cast(Object from);

	/**
	 * Test if the game resource class associated with this instance is {@code Fluid}.
	 * Always true for {@link ArticleType#FLUID} but also true for any {@code ArticleType}
	 * having {@code Fluid} as the type parameter.
	 *
	 * @return {@code true} if the game resource class associated with this instance is {@code Fluid}
	 */
	boolean isFluid();

	/**
	 * Test if the game resource class associated with this instance is {@code Item}.
	 * Always true for {@link ArticleType#ITEM} but also true for any {@code ArticleType}
	 * having {@code Item} as the type parameter.
	 *
	 * @return {@code true} if the game resource class associated with this instance is {@code Item}
	 */
	boolean isItem();

	/**
	 * Indicates if articles of this type may have fractional units in normal usage.
	 * Such articles types are typically stored and transported with fractional accounting
	 * but this information is advisory only.
	 *
	 * @return True if articles of this type may have fractional units in normal usage.
	 */
	boolean isBulk();

	/**
	 * Serializes this instance to an NBT tag that can later be used to retrieve an equivalent instance using {@link #fromTag(Tag)}.
	 *
	 * @return An NBT tag suitable for saving this instance in world data.
	 */
	Tag toTag();

	/**
	 * Serializes this instance to a packet buffer that can be used (typically on the other side of a client/server connection)
	 * to retrieve the instance via {@link #fromPacket(FriendlyByteBuf)}.
	 *
	 * @param buf Target packet buffer for serialization output.
	 */
	void toPacket(FriendlyByteBuf buf);

	/**
	 * Convenient, non-allocating predicate to match articles of this type.
	 * @return  view predicate that matches articles of this type
	 */
	Predicate<? super StoredArticleView> viewPredicate();

	/**
	 * Convenient, non-allocating predicate to match articles of this type.
	 * @return  predicate that matches articles of this type
	 */
	Predicate<Article> articlePredicate();

	/**
	 * Convenient, non-allocating predicate to match this type.
	 * @return  predicate that matches this type
	 */
	Predicate<ArticleType<?>> typePredicate();

	/**
	 * Deserialize an instance previously serialized with {@link #toTag()}.
	 *
	 * @param tag Earlier output of {@link #toTag()}
	 * @return Instance equivalent to the instance encoded in the tag, or {@link #NOTHING} if the instance is no longer registered
	 */
	static <T> ArticleType<T> fromTag(Tag tag) {
		return ArticleTypeImpl.fromTag(tag);
	}

	/**
	 * Read an instance previously encoded in a packet buffer via {@link #toPacket(FriendlyByteBuf)}.
	 *
	 * @param buf The packet buffer
	 * @return Instance encoded in the buffer, or {@link #NOTHING} if the instance cannot be read or found
	 */
	static <T> ArticleType<T> fromPacket(FriendlyByteBuf buf) {
		return ArticleTypeImpl.fromPacket(buf);
	}

	/**
	 * Standard article type for {@code Item} game resources.
	 */
	ArticleType<Item> ITEM = ArticleTypeImpl.ITEM;

	/**
	 * Standard article type for {@code Fluid} game resources.
	 */
	ArticleType<Fluid> FLUID = ArticleTypeImpl.FLUID;

	/**
	 * Special article type to represent the absence of any article.
	 * Should have only one associated value: {@link Article#NOTHING}.
	 */
	ArticleType<Void> NOTHING = ArticleTypeImpl.NOTHING;

	/**
	 * Single-member, immutable set containing only {@link #ITEM}.
	 * Offered as a convenience for carrier definitions and other miscellaneous uses.
	 */
	Set<ArticleType<?>> SET_OF_ITEMS = ImmutableSet.of(ITEM);

	/**
	 * Single-member, immutable set containing only {@link #FLUID}.
	 * Offered as a convenience for carrier definitions and other miscellaneous uses.
	 */
	Set<ArticleType<?>> SET_OF_FLUIDS = ImmutableSet.of(FLUID);

	/**
	 * Creates a new {@code Builder} instance for the given game resource class.
	 *
	 * @param <V> Resource class as type parameter
	 * @param clazz Resource class as {@code Class} instance
	 * @return A new {@code Builder} instance for the given game resource class
	 */
	static <V> Builder<V> builder(Class<V> clazz) {
		return ArticleTypeImpl.builder(clazz);
	}

	/**
	 * Constructs new {@code ArticleType} instances.
	 *
	 * @param <U> Class of the game resource represented by articles of this type
	 */
	interface Builder<U> {
		/**
		 * Sets value of {@link ArticleType#isBulk()}.
		 *
		 * @param isBulk Desired value
		 * @return This builder instance
		 */
		Builder<U> bulk(boolean isBulk);

		/**
		 * Sets function to serialize article resources to NBT.
		 * <em>Must</em> be set before calling {@link #build()}.
		 *
		 * @param tagWriter Function to serialize an article resource to NBT
		 * @return This builder instance
		 */
		Builder<U> resourceTagWriter(Function<U, Tag> tagWriter);

		/**
		 * Sets function to deserialize article resources from NBT.
		 * <em>Must</em> be set before calling {@link #build()}.
		 *
		 * @param tagReader Function to deserialize an article resource from NBT
		 * @return This builder instance
		 */
		Builder<U> resourceTagReader(Function<Tag, U> tagReader);

		/**
		 * Sets function to serialize article resources to a packet buffer.
		 * <em>Must</em> be set before calling {@link #build()}.
		 *
		 * @param packetWriter Function to serialize an article resource to a packet buffer
		 * @return This builder instance
		 */
		Builder<U> resourcePacketWriter(BiConsumer<U, FriendlyByteBuf> packetWriter);

		/**
		 * Sets function to deserialize article resources from a packet buffer.
		 * <em>Must</em> be set before calling {@link #build()}.
		 *
		 * @param packetReader Function to deserialize an article resource from a packet buffer
		 * @return This builder instance
		 */
		Builder<U> resourcePacketReader(Function<FriendlyByteBuf, U> packetReader);

		/**
		 * Sets function to derive a translation key for an article resource of this type.
		 * <em>Must</em> be set before calling {@link #build()}.
		 *
		 * @param translationKeyFunction Function to derive a translation key for an article resource of this type
		 * @return This builder instance
		 */
		Builder<U> translationKeyFunction(Function<U, String> translationKeyFunction);

		/**
		 * Creates a new ArticleType instance as specified by this builder instance.
		 * This does not cause the article type to be registered.  Call
		 * {@link ArticleTypeRegistry#add(String, ArticleType)} to register the new instance.
		 *
		 *
		 * @return A new ArticleType instance as specified by this builder instance
		 */
		ArticleType<U> build();
	}
}
