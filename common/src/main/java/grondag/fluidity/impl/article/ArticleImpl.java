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

package grondag.fluidity.impl.article;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;

@Internal
public class ArticleImpl<T> implements Article {
	final ArticleTypeImpl<T> type;
	final T resource;
	final CompoundTag tag;
	final int hashCode;
	String translationKey;

	ArticleImpl(ArticleType<T> type, T resource, @Nullable CompoundTag tag) {
		this.type = (ArticleTypeImpl<T>) type;
		this.resource = resource;
		this.tag = tag;

		int hashCode = resource == null ? 0 : resource.hashCode();

		if (tag != null) {
			hashCode += tag.hashCode();
		}

		this.hashCode = hashCode;
	}

	@Override
	public final ArticleType<?> type() {
		return type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final T resource() {
		return resource;
	}

	@Override
	public final boolean hasTag() {
		return tag != null;
	}

	@Override
	@Nullable
	public final CompoundTag copyTag() {
		return tag.copy();
	}

	@Override
	public final boolean doesTagMatch(@Nullable CompoundTag otherTag) {
		return tag == null ? otherTag == null : tag.equals(otherTag);
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ArticleImpl) {
			final ArticleImpl<?> other = (ArticleImpl<?>) obj;
			return other.resource == resource && other.type == type && doesTagMatch(other.tag);
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return hashCode;
	}

	public static final ArticleImpl<Void> NOTHING = new ArticleImpl<>(ArticleType.NOTHING, null, null);

	@Override
	public Tag toTag() {
		final CompoundTag result = new CompoundTag();
		result.put("type", type.toTag());
		result.put("res", type.tagWriter.apply(resource));

		if (this.tag != null) {
			result.put("tag", tag.copy());
		}

		return result;
	}

	@Override
	public void toPacket(FriendlyByteBuf buf) {
		type.toPacket(buf);
		type.packetWriter.accept(resource, buf);
		buf.writeNbt(tag);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Article fromTag(Tag tag) {
		if (tag == null) {
			return Article.NOTHING;
		}

		final CompoundTag myTag = (CompoundTag) tag;
		final ArticleTypeImpl type = ArticleTypeImpl.fromTag(myTag.get("type"));
		final Object resource = type.tagReader.apply(myTag.get("res"));
		final CompoundTag aTag = myTag.contains("tag") ? myTag.getCompound("tag") : null;
		return of(type, resource, aTag);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Article fromPacket(FriendlyByteBuf buf) {
		final ArticleTypeImpl type = ArticleTypeImpl.fromPacket(buf);
		final Object resource = type.packetReader.apply(buf);
		final CompoundTag aTag = buf.readNbt();
		return of(type, resource, aTag);
	}

	public static <V> Article of(ArticleType<V> type, V resource, @Nullable CompoundTag tag) {
		return ArticleCache.getArticle(type, resource, tag);
	}

	public static Article of(Item item) {
		return of(item, null);
	}

	public static Article of(ItemStack stack) {
		return stack.isEmpty() ? Article.NOTHING : of(stack.getItem(), stack.getTag());
	}

	public static Article of(Item item, @Nullable CompoundTag tag) {
		if (item == Items.AIR || item == null) {
			return NOTHING;
		} else {
			return ArticleCache.getArticle(ArticleType.ITEM, item, tag);
		}
	}

	public static Article of(Fluid fluid) {
		return fluid == Fluids.EMPTY ? NOTHING : ArticleCache.getArticle(ArticleType.FLUID, fluid, null);
	}

	@Override
	public String getTranslationKey() {
		String result = translationKey;

		if (result == null) {
			result = type.keyFunction.apply(resource);
			translationKey = result;
		}

		return result;
	}
}
