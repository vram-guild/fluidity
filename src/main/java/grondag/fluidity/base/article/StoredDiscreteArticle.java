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
package grondag.fluidity.base.article;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import grondag.fluidity.api.article.Article;

@Experimental
public class StoredDiscreteArticle extends AbstractStoredArticle implements StoredDiscreteArticleView {
	protected long count;

	public StoredDiscreteArticle() {
		setArticle(Article.NOTHING);
	}

	public StoredDiscreteArticle(Article article, final long count, final int handle) {
		prepare(article, count, handle);
	}

	public StoredDiscreteArticle prepare(Article article, long count, int handle) {
		setArticle(article == null ? Article.NOTHING : article);
		this.handle = handle;
		this.count = count;
		return this;
	}

	public StoredDiscreteArticle prepare(ItemStack stack, int handle) {
		return prepare(Article.of(stack), stack.getCount(), handle);
	}

	@Override
	public boolean isEmpty() {
		return count == 0 || article == Article.NOTHING;
	}

	@Override
	public long count() {
		return count;
	}

	@Override
	public void zero() {
		count = 0;
	}

	public static StoredDiscreteArticle of(Article article, long count, int handle) {
		return new StoredDiscreteArticle(article, count, handle);
	}

	public static StoredDiscreteArticle of(ItemStack stack) {
		return of(stack, stack.getCount(), 0);
	}

	public static StoredDiscreteArticle of(ItemStack item, long count, int handle) {
		return of(Article.of(item), count, handle);
	}

	public NbtCompound toTag() {
		final NbtCompound result = new NbtCompound();
		result.put("art", article.toTag());
		result.putLong("count", count);
		return result;
	}

	public void readTag(NbtCompound tag) {
		setArticle(Article.fromTag(tag.get("art")));
		count = tag.getLong("count");
	}

	public void setCount(long count) {
		this.count = count;
	}

	public void addToCount(long delta) {
		count += delta;
	}
}
