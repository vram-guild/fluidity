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
package grondag.fluidity.base.article;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.Storage;

@API(status = Status.EXPERIMENTAL)
public class DiscreteStoredArticle extends AbstractStoredArticle {
	public long count;

	public DiscreteStoredArticle() {
		article = Article.NOTHING;
	}

	public DiscreteStoredArticle(Article article, final long count, final int handle) {
		prepare(article, count, handle);
	}

	public DiscreteStoredArticle prepare(Article article, long count, int handle) {
		this.article = article == null ? Article.NOTHING : article;
		this.handle = handle;
		this.count = count;
		return this;
	}

	public DiscreteStoredArticle prepare(ItemStack stack, int handle) {
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

	@Override
	public void addStore(Storage store) {
		// TODO move to parent

	}

	public static DiscreteStoredArticle of(Article article, long count, int handle) {
		return new DiscreteStoredArticle(article, count, handle);
	}

	public static DiscreteStoredArticle of(ItemStack stack) {
		return of(stack, stack.getCount(), 0);
	}

	public static DiscreteStoredArticle of(ItemStack item, long count, int handle) {
		return of(Article.of(item), count, handle);
	}

	public CompoundTag toTag() {
		final CompoundTag result = new CompoundTag();
		result.put("art", article.toTag());
		result.putLong("count", count);
		return result;
	}

	public void readTag(CompoundTag tag) {
		article = Article.fromTag(tag.get("art"));
		count = tag.getLong("count");
	}

	@Override
	public FractionView volume() {
		// TODO Auto-generated method stub
		return null;
	}
}
