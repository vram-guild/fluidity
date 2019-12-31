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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.PacketByteBuf;

import grondag.fluidity.impl.ArticleImpl;

/**
 * Represents a game resource that may be an ItemStack, Fluid, XP, power or any other
 * instance that is quantifiable and can be serialized to/from NBT and packet buffers.
 */
@API(status = Status.EXPERIMENTAL)
public interface Article {
	ArticleType<?> type();

	/**
	 * The instance represented by this article.
	 * @param <T> Type of the instance, determined by {@link #type()}.
	 * @return The instance. Should return {@code null} <em>only</em> for {@link #NOTHING}.
	 */
	@Nullable <T> T resource();

	default boolean isNothing() {
		return this == NOTHING;
	}

	default boolean isBulk() {
		return type().isBulk();
	}

	default boolean isDiscrete() {
		return !isBulk();
	}

	/**
	 * True only for articles that represent in-game fluids.
	 * If true, then {@link #toFluid()} will always return a non-null value: the fluid this article represents.<p>
	 *
	 * @return {@code true} if this article represents a registered {@code Fluid}
	 */
	default boolean isFluid() {
		return type().isFluid();
	}

	default @Nullable Fluid toFluid() {
		return isFluid() ? resource() : null;
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
	 * @return {@code Item} this article is or has, if any. {@code Items.AIR} otherwise.
	 */
	default Item toItem() {
		return isItem() ? resource() : Items.AIR;
	}

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

	default ItemStack toStack() {
		return toStack(1);
	}

	default boolean matches(ItemStack stack) {
		if (isItem()) {
			return stack.getItem() == toItem() && (hasTag() ? doesTagMatch(stack.getTag()) : !stack.hasTag());
		} else {
			return stack == ItemStack.EMPTY;
		}
	}

	boolean hasTag();

	boolean doesTagMatch(CompoundTag otherTag);

	@Nullable CompoundTag copyTag();

	Tag toTag();

	void toPacket(PacketByteBuf buf);

	static Article fromTag(Tag tag) {
		return ArticleImpl.fromTag(tag);
	}

	static Article fromPacket(PacketByteBuf buf) {
		return ArticleImpl.fromPacket(buf);
	}

	static <V> Article of(ArticleType<V> type, V resource) {
		return ArticleImpl.of(type, resource, null);
	}

	static Article of(ItemStack stack) {
		return ArticleImpl.of(stack);
	}

	static Article of(Item item, CompoundTag tag) {
		return ArticleImpl.of(item, tag);
	}

	static Article of(Item item) {
		return ArticleImpl.of(item);
	}

	static Article of(Fluid fluid) {
		return ArticleImpl.of(fluid);
	}

	Article NOTHING = ArticleImpl.NOTHING;
}
