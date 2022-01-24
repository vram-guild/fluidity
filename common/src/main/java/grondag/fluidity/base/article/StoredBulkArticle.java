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

import net.minecraft.world.item.ItemStack;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.MutableFraction;

@Experimental
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

	public StoredBulkArticle prepare(Article article, Fraction amount, int handle) {
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

	public void add(Fraction delta) {
		volume.add(delta);
	}

	public void subtract(Fraction delta) {
		volume.subtract(delta);
	}

	@Override
	public Fraction amount() {
		return volume;
	}

	public static StoredBulkArticle of(ItemStack stack, int handle) {
		return new StoredBulkArticle().prepare(stack, handle);
	}

	public static StoredBulkArticle of(Article article, long wholeUnits, int handle) {
		return new StoredBulkArticle().prepare(article, wholeUnits, handle);
	}

	public static StoredBulkArticle of(Article article, long numerator, long denominator, int handle) {
		return new StoredBulkArticle().prepare(article, numerator, denominator, handle);
	}

	public static StoredBulkArticle of(Article article, Fraction amount, int handle) {
		return new StoredBulkArticle().prepare(article, amount, handle);
	}
}
