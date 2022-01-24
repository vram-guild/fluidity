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

package grondag.fluidity.base.article;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

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

	public CompoundTag toTag() {
		final CompoundTag result = new CompoundTag();
		result.put("art", article.toTag());
		result.putLong("count", count);
		return result;
	}

	public void readTag(CompoundTag tag) {
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
