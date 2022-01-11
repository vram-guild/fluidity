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

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

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

		if(tag != null) {
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
		} else if(obj instanceof ArticleImpl) {
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
		result.put("res",type.tagWriter.apply(resource));
		if(this.tag != null) {
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
		if(tag == null) {
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
		if(item == Items.AIR || item == null) {
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

		if(result == null) {
			result = type.keyFunction.apply(resource);
			translationKey =  result;
		}

		return result;
	}
}
