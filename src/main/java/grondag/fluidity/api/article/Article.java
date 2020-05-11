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
package grondag.fluidity.api.article;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketByteBuf;
import grondag.fluidity.impl.article.ArticleImpl;

/**
 * Represents a game resource that may be stored or transported.<p>
 *
 * Typically an ItemStack, Fluid, XP, or power but could by any
 * instance that is uniquely identifiable, quantifiable and serializable to/from NBT and packet buffers.
 */
@API(status = Status.EXPERIMENTAL)
public interface Article {
	/**
	 * The {@link ArticleType} for this instance.  Controls how the
	 * article is serialized, the instance class type, and houses metadata.
	 * @return the {@link ArticleType} for this instance.
	 */
	ArticleType<?> type();

	/**
	 * The instance represented by this article.
	 * @param <T> Type of the instance, determined by {@link #type()}.
	 * @return The instance. Should return {@code null} <em>only</em> for {@link #NOTHING}.
	 */
	@Nullable <T> T resource();

	/**
	 * For convenience and to promote discoverability of {@link #NOTHING}.
	 *
	 * @return {@code true} <em>only</em> if this is the {@link #NOTHING} instance.
	 */
	default boolean isNothing() {
		return this == NOTHING;
	}

	/**
	 * True only for articles that represent in-game items.
	 * If true, then {@link #toItem()} will always return a non-null value: the item this article represents.<p>
	 *
	 * @return {@code true} if this article represents a registered {@code Item}
	 */
	default boolean isItem() {
		return type().isItem();
	}

	/**
	 * If this article represents an {@code Item} or is somehow associated with an item,
	 * the item represented or associated.  Should return {@link Items#AIR} in all other cases.
	 *
	 * @return {@code Item} this article is or has, if any. {@code Items.AIR} otherwise.
	 */
	default Item toItem() {
		return isItem() ? resource() : Items.AIR;
	}

	/**
	 * True only for articles that represent in-game fluids.
	 *
	 * @return {@code true} if this article represents a registered {@code Fluid}
	 */
	default boolean isFluid() {
		return type().isFluid();
	}

	/**
	 * If this article represents an {@code Fluid} or is somehow associated with a fluid,
	 * the fluid represented or associated.  Should return {@link Fluids#EMPTY} in all other cases.
	 *
	 * @return {@code Fluid} this article is or has, if any. {@code Fluids.EMPTY} otherwise.
	 */
	default Fluid toFluid() {
		return isFluid() ? resource() : Fluids.EMPTY;
	}

	/**
	 * Convenience method for instantiating a new {@code ItemStack} with the values of {@link #toItem()}
	 * and {@link #copyTag()} (if any NBT tag applies).<p>
	 *
	 * This method allocates a new instance with every call.
	 * Changes to the returned instance will have no effect on this article.
	 *
	 * @param count  Accepts a long for convenience when used with storage implementations, but stack
	 * size will be limited {@link Item#getMaxCount()} if the given {@code count} value is higher.
	 *
	 * @return A new item stack instance with the given count (or the max possible for the item, if lower)
	 * or {@link ItemStack#EMPTY} if this article does not represent a non-empty Item.
	 */
	default ItemStack toStack(long count) {
		if(!isItem()) {
			return ItemStack.EMPTY;
		}

		final Item item = resource();
		final ItemStack result = new ItemStack(item, (int) Math.min(item.getMaxCount(), count));

		if(hasTag()) {
			result.setTag(copyTag());
		}

		return result;
	}

	/**
	 * Alias for {@code toStack(1)}
	 *
	 * @return A new item stack instance containing one item or {@link ItemStack#EMPTY}
	 * if this article does not represent a non-empty Item.
	 *
	 * @see #toStack(long)
	 */
	default ItemStack toStack() {
		return toStack(1);
	}

	/**
	 * Convenient and efficient comparison of {@code Item}-type articles, especially when
	 * the stack may contain a tag. Does not cause allocation of a new tag instance in that case.
	 *
	 * @param stack Stack for comparison.
	 * @return True when this article resource is the same {@code Item} instance and both the article
	 * and stack have no tag or both tags are equal.
	 */
	default boolean matches(ItemStack stack) {
		if (isItem()) {
			return stack.getItem() == toItem() && (hasTag() ? doesTagMatch(stack.getTag()) : !stack.hasTag());
		} else {
			return stack == ItemStack.EMPTY;
		}
	}

	/**
	 * Non-allocating test for presence of a non-null tag. (The tag could still be empty.)
	 *
	 * @return {@code true} when this article has a null tag value.
	 */
	boolean hasTag();

	/**
	 * Non-allocating test for tag equality.
	 *
	 * @param otherTag Tag to be compared.
	 * @return {@code true} when both tags are null or both tags are equal.
	 */
	boolean doesTagMatch(CompoundTag otherTag);

	/**
	 * Allocates a copy of NBT tag associated with this article, or returns {@code null} if there is none.
	 * Use {@link #hasTag()} or {@link #doesTagMatch(CompoundTag)} when the intent is to test the tag value.
	 *
	 * @return A copy of the tag associated with this article, or {@code null} if there is none.
	 */
	@Nullable CompoundTag copyTag();

	/**
	 * Serializes this instance to an NBT tag that can later be used to retrieve an equivalent instance using {@link #fromTag(Tag)}.
	 * Note the similarity in naming to {@link #copyTag()}. This is very different.
	 *
	 * @return An NBT tag suitable for saving this instance in world data.
	 */
	Tag toTag();

	/**
	 * Serializes this instance to a packet buffer that can be used (typically on the other side of a client/server connection)
	 * to retrieve the instance via {@link #fromPacket(PacketByteBuf)}.
	 *
	 * @param buf Target packet buffer for serialization output.
	 */
	void toPacket(PacketByteBuf buf);

	/**
	 * Supplies a translation key for displaying a localized label to the player.
	 * Server-safe but typically used only on client.
	 *
	 * @return Translation key for displaying a localized label to the player.
	 */
	String getTranslationKey();

	/**
	 * Deserialize an instance previously serialized with {@link #toTag()}
	 *
	 * @param tag Earlier output of {@link #toTag()}
	 * @return Instance equivalent to the instance encoded in the tag, or {@link #NOTHING} if tag is null or the instance is no longer registered
	 */
	static Article fromTag(@Nullable Tag tag) {
		return ArticleImpl.fromTag(tag);
	}

	/**
	 * Read an instance previously encoded in a packet buffer via {@link #toPacket(PacketByteBuf)}.
	 *
	 * @param buf The packet buffer
	 * @return Instance encoded in the buffer, or {@link #NOTHING} if the instance cannot be read or found
	 */
	static Article fromPacket(PacketByteBuf buf) {
		return ArticleImpl.fromPacket(buf);
	}

	/**
	 * Find or create the article instance for the given article type and resource.
	 * Instances are interned and calls are non-allocating after the first call for any given resource.
	 *
	 * @param <V> The class of the game resource represented by the article
	 * @param type The article type
	 * @param resource The game resource represented by the article
	 * @return The article instance
	 */
	static <V> Article of(ArticleType<V> type, V resource) {
		return ArticleImpl.of(type, resource, null);
	}

	/**
	 * Find or create the article representing the given item stack, including NBT tag if present.
	 * Calls may return different but equivalent instances if the stack has a non-null tag.
	 * Type of the article will be {@link ArticleType#ITEM}.
	 *
	 * @param stack The stack the article will represent
	 * @return Article representing the given stack
	 */
	static Article of(ItemStack stack) {
		return ArticleImpl.of(stack);
	}

	/**
	 * Find or create the article representing the given item and tag.
	 * Calls may return different but equivalent instances if the tag is non-null.
	 * Type of the article will be {@link ArticleType#ITEM}.
	 *
	 * @param item The Item the article will represent
	 * @param tag NBT tag with additional item data
	 * @return Article representing the given item and tag
	 */
	static Article of(Item item, @Nullable CompoundTag tag) {
		return ArticleImpl.of(item, tag);
	}

	/**
	 * Find or create the article representing the given item.
	 * Type of the article will be {@link ArticleType#ITEM}.
	 *
	 * @param item The Item the article will represent
	 * @return Article representing the given item
	 */
	static Article of(Item item) {
		return ArticleImpl.of(item);
	}

	/**
	 * Find or create the article representing the given fluid.
	 * Type of the article will be {@link ArticleType#FLUID}.
	 *
	 * @param fluid The fluid the article will represent
	 * @return Article representing the given fluid
	 */
	static Article of(Fluid fluid) {
		return ArticleImpl.of(fluid);
	}

	/**
	 * Use instead of {@code null} to represent the absence of any article.
	 */
	Article NOTHING = ArticleImpl.NOTHING;
}
