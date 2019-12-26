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

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;

@API(status = Status.EXPERIMENTAL)
public class StoredBulkArticle extends AbstractStoredArticle implements StoredBulkArticleView {
	protected final MutableFraction fraction = new MutableFraction();

	public StoredBulkArticle() {
		setArticle(Article.NOTHING);
	}

	public StoredBulkArticle(Article article, final long count, final int handle) {
		prepare(article, count, handle);
	}

	public StoredBulkArticle prepare(Article article, long count, int handle) {
		setArticle(article == null ? Article.NOTHING : article);
		this.handle = handle;
		fraction.set(count);
		return this;
	}

	public StoredBulkArticle prepare(ItemStack stack, int handle) {
		return prepare(Article.of(stack), stack.getCount(), handle);
	}

	@Override
	public boolean isEmpty() {
		return fraction.isZero();
	}

	@Override
	public void zero() {
		fraction.set(0);
	}

	@Override
	public FractionView volume() {
		return fraction;
	}

	public static StoredBulkArticle of(ItemStack stack) {
		return new  StoredBulkArticle().prepare(stack, 0);
	}
}
