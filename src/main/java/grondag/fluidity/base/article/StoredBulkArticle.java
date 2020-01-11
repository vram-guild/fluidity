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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.impl.AbstractFraction;

@API(status = Status.EXPERIMENTAL)
public class StoredBulkArticle extends AbstractStoredArticle implements StoredBulkArticleView {
	protected final MutableFraction volume = new MutableFraction();

	public StoredBulkArticle() {
		setArticle(Article.NOTHING);
	}

	public StoredBulkArticle(Article article, final long wholeUnits, final int handle) {
		prepare(article, wholeUnits, handle);
	}

	public StoredBulkArticle prepare(Article article, long wholeUnits, int handle) {
		setArticle(article == null ? Article.NOTHING : article);
		this.handle = handle;
		volume.set(wholeUnits);
		return this;
	}

	public StoredBulkArticle prepare(Article article, long numerator, long denominator, int handle) {
		setArticle(article == null ? Article.NOTHING : article);
		this.handle = handle;
		volume.set(numerator, denominator);
		return this;
	}

	public StoredBulkArticle prepare(Article article, FractionView amount, int handle) {
		setArticle(article == null ? Article.NOTHING : article);
		this.handle = handle;
		volume.set(amount);
		return this;
	}

	public StoredBulkArticle prepare(ItemStack stack, int handle) {
		return prepare(Article.of(stack), stack.getCount(), handle);
	}

	@Override
	public boolean isEmpty() {
		return volume.isZero();
	}

	@Override
	public void zero() {
		volume.set(0);
	}

	public void add(FractionView delta) {
		volume.add(delta);
	}

	public void subtract(FractionView delta) {
		volume.subtract(delta);
	}

	@Override
	public AbstractFraction volume() {
		return volume;
	}

	public static StoredBulkArticle of(ItemStack stack, int handle) {
		return new  StoredBulkArticle().prepare(stack, handle);
	}

	public static StoredBulkArticle of(Article article, long wholeUnits, int handle) {
		return new  StoredBulkArticle().prepare(article, wholeUnits, handle);
	}

	public static StoredBulkArticle of(Article article, long numerator, long denominator, int handle) {
		return new  StoredBulkArticle().prepare(article, numerator, denominator, handle);
	}

	public static StoredBulkArticle of(Article article, FractionView amount, int handle) {
		return new  StoredBulkArticle().prepare(article, amount, handle);
	}
}
