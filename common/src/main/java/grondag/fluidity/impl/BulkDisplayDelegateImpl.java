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

package grondag.fluidity.impl;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.base.synch.BulkDisplayDelegate;

@Internal
public class BulkDisplayDelegateImpl extends AbstractDisplayDelegateImpl implements BulkDisplayDelegate {
	final MutableFraction amount = new MutableFraction();

	public BulkDisplayDelegateImpl(Article article, Fraction amount, int handle) {
		super(article, handle);
		this.amount.set(amount);
	}

	@Override
	public BulkDisplayDelegateImpl set (Article article, Fraction amount, int handle) {
		setArticleAndHandle(article, handle);
		this.amount.set(amount);
		return this;
	}

	@Override
	public BulkDisplayDelegateImpl clone() {
		return new BulkDisplayDelegateImpl(article, amount, handle);
	}

	@Override
	public Fraction getAmount() {
		return amount;
	}

	@Override
	public void setAmount(Fraction amount) {
		this.amount.set(amount);
	}

	@Override
	public long getCount() {
		return amount.whole();
	}

	@Override
	public long numerator() {
		return amount.numerator();
	}

	@Override
	public long divisor() {
		return amount.divisor();
	}

	@Override
	public boolean isEmpty() {
		return amount.isZero();
	}
}
