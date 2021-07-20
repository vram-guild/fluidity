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

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;

@Internal
public class ArticleImpl<T> implements Article {
	final ArticleTypeImpl<T> type;
	final T resource;
	final NbtCompound tag;
	final int hashCode;
	String translationKey;

	ArticleImpl(ArticleType<T> type, T resource, @Nullable NbtCompound tag) {
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
	public final NbtCompound copyTag() {
		return tag.copy();
	}

	@Override
	public final boolean doesTagMatch(@Nullable NbtCompound otherTag) {
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
	public NbtElement toTag() {
		final NbtCompound result = new NbtCompound();
		result.put("type", type.toTag());
		result.put("res",type.tagWriter.apply(resource));
		if(this.tag != null) {
			result.put("tag", tag.copy());
		}
		return result;
	}

	@Override
	public void toPacket(PacketByteBuf buf) {
		type.toPacket(buf);
		type.packetWriter.accept(resource, buf);
		buf.writeNbt(tag);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Article fromTag(NbtElement tag) {
		if(tag == null) {
			return Article.NOTHING;
		}

		final NbtCompound myTag = (NbtCompound) tag;
		final ArticleTypeImpl type = ArticleTypeImpl.fromTag(myTag.get("type"));
		final Object resource = type.tagReader.apply(myTag.get("res"));
		final NbtCompound aTag = myTag.contains("tag") ? myTag.getCompound("tag") : null;
		return of(type, resource, aTag);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Article fromPacket(PacketByteBuf buf) {
		final ArticleTypeImpl type = ArticleTypeImpl.fromPacket(buf);
		final Object resource = type.packetReader.apply(buf);
		final NbtCompound aTag = buf.readNbt();
		return of(type, resource, aTag);
	}

	public static <V> Article of(ArticleType<V> type, V resource, @Nullable NbtCompound tag) {
		return ArticleCache.getArticle(type, resource, tag);
	}

	public static Article of(Item item) {
		return of(item, null);
	}

	public static Article of(ItemStack stack) {
		return stack.isEmpty() ? Article.NOTHING : of(stack.getItem(), stack.getNbt());
	}

	public static Article of(Item item, @Nullable NbtCompound tag) {
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
